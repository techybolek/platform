package org.apache.hadoop.hive.cassandra.input;

import com.google.common.collect.*;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.TypeParser;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.hadoop.ColumnFamilyRecordReader;
import org.apache.cassandra.hadoop.ColumnFamilySplit;
import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ColumnFamilyRowRecordReader extends ColumnFamilyRecordReader {

    private static final Logger logger = LoggerFactory.getLogger(ColumnFamilyRecordReader.class);

    public static final int CASSANDRA_HADOOP_MAX_KEY_SIZE_DEFAULT = 8192;

    private ColumnFamilySplit split;
    private IncrementalColumnFamilySplit incSplit;
    private RowIterator iter;
    private Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>> currentRow;
    private SlicePredicate predicate;
    private boolean isEmptyPredicate;
    private int totalRowCount; // total number of rows to fetch
    private int batchSize; // fetch this many per batch
    private String cfName;
    private String indexCfName;
    private String keyspace;
    private String indexKeySpace;
    private TSocket socket;
    private TSocket indexClientSocket;
    private Cassandra.Client client;
    private Cassandra.Client indexClient;
    private ConsistencyLevel consistencyLevel;
    private int keyBufferSize = 8192;
    private List<IndexExpression> filter;
    private AuthenticationRequest authRequest;
    private boolean isCFIncremental;

    public ColumnFamilyRowRecordReader() {
        this(ColumnFamilyRecordReader.CASSANDRA_HADOOP_MAX_KEY_SIZE_DEFAULT);
    }

    public ColumnFamilyRowRecordReader(int keyBufferSize) {
        super();
        this.keyBufferSize = keyBufferSize;
    }

    public void close() {
        if (socket != null && socket.isOpen()) {
            socket.close();
            socket = null;
            client = null;
        }
        if (indexClientSocket != null && indexClientSocket.isOpen()) {
            indexClientSocket.close();
            indexClient = null;
            indexClientSocket = null;
        }
    }

    public ByteBuffer getCurrentKey() {
        return currentRow.left;
    }

    public SortedMap<ByteBuffer, IColumn> getCurrentValue() {
        return currentRow.right;
    }

    public float getProgress() {
        // TODO this is totally broken for wide rows
        // the progress is likely to be reported slightly off the actual but close enough
        float progress = ((float) iter.rowsRead() / totalRowCount);
        return progress > 1.0F ? 1.0F : progress;
    }

    static boolean isEmptyPredicate(SlicePredicate predicate) {
        if (predicate == null)
            return true;

        if (predicate.isSetColumn_names() && predicate.getSlice_range() == null)
            return false;

        if (predicate.getSlice_range() == null)
            return true;

        byte[] start = predicate.getSlice_range().getStart();
        if ((start != null) && (start.length > 0))
            return false;

        byte[] finish = predicate.getSlice_range().getFinish();
        if ((finish != null) && (finish.length > 0))
            return false;

        return true;
    }

    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException {
        Configuration conf = context.getConfiguration();

        cfName = ConfigHelper.getInputColumnFamily(conf);
        keyspace = ConfigHelper.getInputKeyspace(conf);

        String hiveIncrementalTableConfValue = keyspace + "::" + cfName;
        boolean isIncremental = Boolean.valueOf(conf.get(HiveConf.ConfVars.HIVE_INCREMENTAL_PROCESS_ENABLE.toString()));

        if (isIncremental) {
            String hiveIncrementalCassandraTables = conf.get(HiveConf.ConfVars.HIVE_INCREMENTAL_CASSANDRA_TABLES.toString());
            isCFIncremental = false;
            if (null != hiveIncrementalCassandraTables && !hiveIncrementalCassandraTables.equals("")) {
                String[] casssandraTables = hiveIncrementalCassandraTables.split(",");
                for (String cassandraCF : casssandraTables) {
                    if (!cassandraCF.trim().equals("")) {
                        cassandraCF.trim().equalsIgnoreCase(hiveIncrementalTableConfValue);
                        isCFIncremental = true;
                        break;
                    }
                }
            }
            if (isCFIncremental) {
                indexCfName = IncrementalStaticRowIterator.EVENT_INDEX_TABLE_PREFIX + cfName;
                indexKeySpace = conf.get(HiveConf.ConfVars.HIVE_INCREMENTAL_PROCESS_KEYSPACE.toString());
                incSplit = (IncrementalColumnFamilySplit) split;

            } else {
                this.split = (ColumnFamilySplit) split;
            }
        } else {
            this.split = (ColumnFamilySplit) split;
        }


        KeyRange jobRange = ConfigHelper.getInputKeyRange(conf);
        filter = jobRange == null ? null : jobRange.row_filter;
        predicate = ConfigHelper.getInputSlicePredicate(conf);
        boolean widerows = ConfigHelper.getInputIsWide(conf);
        isEmptyPredicate = isEmptyPredicate(predicate);
        totalRowCount = ConfigHelper.getInputSplitSize(conf);
        batchSize = ConfigHelper.getRangeBatchSize(conf);


        consistencyLevel = ConsistencyLevel.valueOf(ConfigHelper.getReadConsistencyLevel(conf));


        try {
            // only need to connect once
            if (socket != null && socket.isOpen())
                return;

            // create connection using thrift
            String location = getLocation(conf);
            socket = new TSocket(location, ConfigHelper.getInputRpcPort(conf));
            TBinaryProtocol binaryProtocol = new TBinaryProtocol(new TFramedTransport(socket));
            client = new Cassandra.Client(binaryProtocol);
            socket.open();

            // log in
            client.set_keyspace(keyspace);
            if (ConfigHelper.getInputKeyspaceUserName(conf) != null) {
                Map<String, String> creds = new HashMap<String, String>();
                creds.put(IAuthenticator.USERNAME_KEY, ConfigHelper.getInputKeyspaceUserName(conf));
                creds.put(IAuthenticator.PASSWORD_KEY, ConfigHelper.getInputKeyspacePassword(conf));
                authRequest = new AuthenticationRequest(creds);
                client.login(authRequest);
            }

            if (isCFIncremental) {
                if (indexClientSocket != null && indexClientSocket.isOpen())
                    return;

                // create connection using thrift
                indexClientSocket = new TSocket(location, ConfigHelper.getInputRpcPort(conf));
                TBinaryProtocol indexBinaryProtocol = new TBinaryProtocol(new TFramedTransport(indexClientSocket));
                indexClient = new Cassandra.Client(binaryProtocol);
                indexClientSocket.open();

                indexClient.set_keyspace(indexKeySpace);

                Map<String, String> creds = new HashMap<String, String>();
                creds.put(IAuthenticator.USERNAME_KEY, ConfigHelper.getInputKeyspaceUserName(conf));
                creds.put(IAuthenticator.PASSWORD_KEY, ConfigHelper.getInputKeyspaceUserName(conf));
                AuthenticationRequest authRequest = new AuthenticationRequest(creds);
                indexClient.login(authRequest);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (widerows) {
            iter = new WideRowIterator();
        } else {
            if (isCFIncremental) {
                IncrementalStaticRowIterator incrIter = new IncrementalStaticRowIterator();
                iter = incrIter;
            } else {
                iter = new StaticRowIterator();
            }

        }
        logger.debug("created {}", iter);
    }

    public boolean nextKeyValue() throws IOException {
        if (!iter.hasNext())
            return false;
        currentRow = iter.next();
        return true;
    }

    // we don't use endpointsnitch since we are trying to support hadoop nodes that are
    // not necessarily on Cassandra machines, too.  This should be adequate for single-DC clusters, at least.
    private String getLocation(Configuration conf) {
        ArrayList<InetAddress> localAddresses = new ArrayList<InetAddress>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements())
                localAddresses.addAll(Collections.list(nets.nextElement().getInetAddresses()));
        } catch (SocketException e) {
            throw new AssertionError(e);
        }

        for (InetAddress address : localAddresses) {
            String[] locations;
            try {
                if (isCFIncremental)
                    locations = incSplit.getLocations();
                else
                    locations = split.getLocations();

                for (String location : locations) {
                    InetAddress locationAddress = null;
                    try {
                        locationAddress = InetAddress.getByName(location);
                    } catch (UnknownHostException e) {
                        throw new AssertionError(e);
                    }
                    if (address.equals(locationAddress)) {
                        return location;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (String location : split.getLocations()) {
            try {
                TSocket socket = new TSocket(location, ConfigHelper.getInputRpcPort(conf));
                socket.open();
                return location;
            } catch (Exception ignored) {
                //ignore. move to next host in the list
                if (logger.isDebugEnabled()) {
                    logger.debug("Host " + location + " seems to be down. Trying next hosts in " +
                            "the list..", ignored);
                }
            }
        }

        return split.getLocations()[0];
    }

    private abstract class RowIterator extends AbstractIterator<Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>>> {
        protected List<KeySlice> rows;
        protected int totalRead = 0;
        protected final AbstractType<?> comparator;
        protected final AbstractType<?> subComparator;
        protected final IPartitioner partitioner;

        private RowIterator() {
            try {
                partitioner = FBUtilities.newPartitioner(client.describe_partitioner());

                // Get the Keyspace metadata, then get the specific CF metadata
                // in order to populate the sub/comparator.
                KsDef ks_def = client.describe_keyspace(keyspace);
                List<String> cfnames = new ArrayList<String>();
                for (CfDef cfd : ks_def.cf_defs)
                    cfnames.add(cfd.name);

                int idx = cfnames.indexOf(cfName);

                CfDef cf_def = ks_def.cf_defs.get(idx);
                comparator = TypeParser.parse(cf_def.comparator_type);
                subComparator = cf_def.subcomparator_type == null ? null : TypeParser.parse(cf_def.subcomparator_type);

            } catch (ConfigurationException e) {
                throw new RuntimeException("unable to load sub/comparator", e);
            } catch (TException e) {
                throw new RuntimeException("error communicating via Thrift", e);
            } catch (Exception e) {
                throw new RuntimeException("unable to load keyspace " + keyspace, e);
            }
        }


        /**
         * @return total number of rows read by this record reader
         */
        public int rowsRead() {
            return totalRead;
        }

        protected IColumn unthriftify(ColumnOrSuperColumn cosc) {
            if (cosc.counter_column != null)
                return unthriftifyCounter(cosc.counter_column);
            if (cosc.counter_super_column != null)
                return unthriftifySuperCounter(cosc.counter_super_column);
            if (cosc.super_column != null)
                return unthriftifySuper(cosc.super_column);
            assert cosc.column != null;
            return unthriftifySimple(cosc.column);
        }

        private IColumn unthriftifySuper(SuperColumn super_column) {
            org.apache.cassandra.db.SuperColumn sc = new org.apache.cassandra.db.SuperColumn(super_column.name, subComparator);
            for (Column column : super_column.columns) {
                sc.addColumn(unthriftifySimple(column));
            }
            return sc;
        }

        protected IColumn unthriftifySimple(Column column) {
            return new org.apache.cassandra.db.Column(column.name, column.value, column.timestamp);
        }

        private IColumn unthriftifyCounter(CounterColumn column) {
            //CounterColumns read the nodeID from the System table, so need the StorageService running and access
            //to cassandra.yaml. To avoid a Hadoop needing access to yaml return a regular Column.
            return new org.apache.cassandra.db.Column(column.name, ByteBufferUtil.bytes(column.value), 0);
        }

        private IColumn unthriftifySuperCounter(CounterSuperColumn superColumn) {
            org.apache.cassandra.db.SuperColumn sc = new org.apache.cassandra.db.SuperColumn(superColumn.name, subComparator);
            for (CounterColumn column : superColumn.columns)
                sc.addColumn(unthriftifyCounter(column));
            return sc;
        }
    }

    private class IncrementalStaticRowIterator extends RowIterator {
        protected int i = 0;
        private static final String EVENT_INDEX_TABLE_PREFIX = "event_index_";

        protected final AbstractType<?> indexCFComparator;
        protected final AbstractType<?> indexCFSubComparator;


        private long lastAccessedIndexColumnName = -1;
        private int numberOdRowsLoaded = 0;
        private long lastAccessedIndexRowKey = -1;
        private int maximumRowsLoaded = 100000;

        IncrementalStaticRowIterator() {
            super();
            try {
                rows = new ArrayList<KeySlice>();
                KsDef ks_def = indexClient.describe_keyspace(indexKeySpace);
                List<String> cfnames = new ArrayList<String>();
                for (CfDef cfd : ks_def.cf_defs)
                    cfnames.add(cfd.name);

                int idx = cfnames.indexOf(indexCfName);

                CfDef cf_def = ks_def.cf_defs.get(idx);
                indexCFComparator = TypeParser.parse(cf_def.comparator_type);
                indexCFSubComparator = cf_def.subcomparator_type == null ? null : TypeParser.parse(cf_def.subcomparator_type);

            } catch (ConfigurationException e) {
                throw new RuntimeException("unable to load sub/comparator", e);
            } catch (TException e) {
                throw new RuntimeException("error communicating via Thrift", e);
            } catch (Exception e) {
                throw new RuntimeException("unable to load keyspace " + indexKeySpace, e);
            }

        }

        private void maybeInit() {
            // check if we need another batch
            if (rows != null && i < rows.size())
                return;


            long iterStartTime;
            long aIndexRowKey = 0;

            rows = new ArrayList<KeySlice>();

            numberOdRowsLoaded = 0;
            boolean finishedSplit = false;
            try {
                do {
                    boolean endRow = false;
                    while (!endRow) {
                        if (totalRead == 0) {
                            // first request
                            iterStartTime = incSplit.getStartColName();
                            aIndexRowKey = incSplit.nextKey();
                        } else {
                            iterStartTime = lastAccessedIndexColumnName + 1;
                            aIndexRowKey = lastAccessedIndexRowKey;

                            if (incSplit.getEndColName() != -1 && iterStartTime >= incSplit.getEndColName()) {
                                // reached end of the split
                                i = 0;
                                rows = null;
                                return;
                            }
                        }

                        SliceRange range = new SliceRange();


                        range.setStart(ByteBufferUtil.bytes(iterStartTime));
                        if (incSplit.getEndColName() == -1) {
                            range.setFinish(new byte[0]);
                        } else {
                            range.setFinish(ByteBufferUtil.bytes(incSplit.getEndColName()));
                        }

                        range.setCount(Integer.MAX_VALUE);
                        SlicePredicate predicate = new SlicePredicate();
                        predicate.setSlice_range(range);

                        indexClient.set_keyspace(indexKeySpace);
                        indexClient.login(authRequest);

                        List<ColumnOrSuperColumn> actualKeyList = indexClient.get_slice(ByteBuffer.wrap(String.valueOf(aIndexRowKey).getBytes()),
                                new ColumnParent(indexCfName), predicate, consistencyLevel);


                        if (actualKeyList == null || actualKeyList.size() == 0) {
                            aIndexRowKey = incSplit.nextKey();
                            if (aIndexRowKey == -1) {
                                finishedSplit = true;
                            }
                            endRow = true;
                        } else {
                            ArrayList<ByteBuffer> actualCFRowKeys = new ArrayList<ByteBuffer>();
                            for (ColumnOrSuperColumn actualKeyCol : actualKeyList) {
                                if (numberOdRowsLoaded < maximumRowsLoaded) {
                                    actualCFRowKeys.add(actualKeyCol.column.bufferForValue());
                                    numberOdRowsLoaded++;
                                    lastAccessedIndexColumnName = actualKeyCol.column.bufferForName().getLong();
                                } else {
                                    endRow = true;
                                    break;
                                }
                            }

                            range = new SliceRange();
                            range.setCount(Integer.MAX_VALUE);
                            range.setStart(new byte[0]);
                            range.setFinish(new byte[0]);
                            predicate = new SlicePredicate();
                            predicate.setSlice_range(range);

                            indexClient.set_keyspace(keyspace);
                            indexClient.login(authRequest);

                            Map<ByteBuffer, List<ColumnOrSuperColumn>> actualRows = indexClient.
                                    multiget_slice(actualCFRowKeys, new ColumnParent(cfName), predicate, consistencyLevel);

                            for (ByteBuffer key : actualRows.keySet()) {
                                rows.add(new KeySlice(key, actualRows.get(key)));
                                totalRead++;
                            }
                        }
                        lastAccessedIndexRowKey = aIndexRowKey;
                    }
                } while (!finishedSplit && numberOdRowsLoaded < maximumRowsLoaded);
                //reset to iterate through new batch
                i = 0;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        }

        private long nextKey(long indexCFRowKey) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(indexCFRowKey);
            calendar.add(Calendar.HOUR, 1);
            return calendar.getTime().getTime();
        }

        private long getColumnNameFromTimeStamp(long timeStamp) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeStamp);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime().getTime();
        }

        private String getIndexCFName() {
            return EVENT_INDEX_TABLE_PREFIX + cfName;
        }

        protected Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>> computeNext() {
            maybeInit();
            if (rows == null || i >= rows.size())
                return endOfData();

            totalRead++;
            KeySlice ks = rows.get(i++);
            SortedMap<ByteBuffer, IColumn> map = new TreeMap<ByteBuffer, IColumn>(comparator);
            for (ColumnOrSuperColumn cosc : ks.columns) {
                IColumn column = unthriftify(cosc);
                map.put(column.name(), column);
            }
            return new Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>>(ks.key, map);
        }
    }


    private class StaticRowIterator extends RowIterator {
        protected int i = 0;

        private void maybeInit() {
            // check if we need another batch
            if (rows != null && i < rows.size())
                return;

            String startToken;
            if (totalRead == 0) {
                // first request
                startToken = split.getStartToken();
            } else {
                startToken = partitioner.getTokenFactory().toString(partitioner.getToken(Iterables.getLast(rows).key));
                if (startToken.equals(split.getEndToken())) {
                    // reached end of the split
                    rows = null;
                    return;
                }
            }

            KeyRange keyRange = new KeyRange(batchSize)
                    .setStart_token(startToken)
                    .setEnd_token(split.getEndToken())
                    .setRow_filter(filter);
            try {

                rows = client.get_range_slices(new ColumnParent(cfName), predicate, keyRange, consistencyLevel);

                // nothing new? reached the end
                if (rows.isEmpty()) {
                    rows = null;
                    return;
                }

                // prepare for the next slice to be read
                KeySlice lastRow = rows.get(rows.size() - 1);
                ByteBuffer rowkey = lastRow.key;
                startToken = partitioner.getTokenFactory().toString(partitioner.getToken(rowkey));

                // remove ghosts when fetching all columns
                if (isEmptyPredicate) {
                    Iterator<KeySlice> it = rows.iterator();
                    KeySlice ks;
                    do {
                        ks = it.next();
                        if (ks.getColumnsSize() == 0) {
                            it.remove();
                        }
                    } while (it.hasNext());

                    // all ghosts, spooky
                    if (rows.isEmpty()) {
                        // maybeInit assumes it can get the start-with key from the rows collection, so add back the last
                        rows.add(ks);
                        maybeInit();
                        return;
                    }
                }

                // reset to iterate through this new batch
                i = 0;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>> computeNext() {
            maybeInit();
            if (rows == null)
                return endOfData();

            totalRead++;
            KeySlice ks = rows.get(i++);
            SortedMap<ByteBuffer, IColumn> map = new TreeMap<ByteBuffer, IColumn>(comparator);
            for (ColumnOrSuperColumn cosc : ks.columns) {
                IColumn column = unthriftify(cosc);
                map.put(column.name(), column);
            }
            return new Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>>(ks.key, map);
        }
    }


    private class WideRowIterator extends RowIterator {
        private PeekingIterator<Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>>> wideColumns;
        private ByteBuffer lastColumn = ByteBufferUtil.EMPTY_BYTE_BUFFER;

        private void maybeInit() {
            if (wideColumns != null && wideColumns.hasNext())
                return;

            KeyRange keyRange;
            ByteBuffer startColumn;
            if (totalRead == 0) {
                String startToken = split.getStartToken();
                keyRange = new KeyRange(batchSize)
                        .setStart_token(startToken)
                        .setEnd_token(split.getEndToken())
                        .setRow_filter(filter);
            } else {
                KeySlice lastRow = Iterables.getLast(rows);
                logger.debug("Starting with last-seen row {}", lastRow.key);
                keyRange = new KeyRange(batchSize)
                        .setStart_key(lastRow.key)
                        .setEnd_token(split.getEndToken())
                        .setRow_filter(filter);
            }

            try {
                rows = client.get_paged_slice(cfName, keyRange, lastColumn, consistencyLevel);
                int n = 0;
                for (KeySlice row : rows)
                    n += row.columns.size();
                logger.debug("read {} columns in {} rows for {} starting with {}",
                        new Object[]{n, rows.size(), keyRange, lastColumn});

                wideColumns = Iterators.peekingIterator(new WideColumnIterator(rows));
                if (wideColumns.hasNext() && wideColumns.peek().right.keySet().iterator().next().equals(lastColumn))
                    wideColumns.next();
                if (!wideColumns.hasNext())
                    rows = null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>> computeNext() {
            maybeInit();
            if (rows == null)
                return endOfData();

            totalRead++;
            Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>> next = wideColumns.next();
            lastColumn = next.right.values().iterator().next().name();
            return next;
        }

        private class WideColumnIterator extends AbstractIterator<Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>>> {
            private final Iterator<KeySlice> rows;
            private Iterator<ColumnOrSuperColumn> columns;
            public KeySlice currentRow;

            public WideColumnIterator(List<KeySlice> rows) {
                this.rows = rows.iterator();
                if (this.rows.hasNext())
                    nextRow();
                else
                    columns = Iterators.emptyIterator();
            }

            private void nextRow() {
                currentRow = rows.next();
                columns = currentRow.columns.iterator();
            }

            protected Pair<ByteBuffer, SortedMap<ByteBuffer, IColumn>> computeNext() {
                while (true) {
                    if (columns.hasNext()) {
                        ColumnOrSuperColumn cosc = columns.next();
                        IColumn column = unthriftify(cosc);
                        ImmutableSortedMap<ByteBuffer, IColumn> map = ImmutableSortedMap.of(column.name(), column);
                        return Pair.<ByteBuffer, SortedMap<ByteBuffer, IColumn>>create(currentRow.key, map);
                    }

                    if (!rows.hasNext())
                        return endOfData();

                    nextRow();
                }
            }
        }
    }

    // Because the old Hadoop API wants us to write to the key and value
    // and the new asks for them, we need to copy the output of the new API
    // to the old. Thus, expect a small performance hit.
    // And obviously this wouldn't work for wide rows. But since ColumnFamilyInputFormat
    // and ColumnFamilyRecordReader don't support them, it should be fine for now.
    public boolean next(ByteBuffer key, SortedMap<ByteBuffer, IColumn> value) throws IOException {
        if (this.nextKeyValue()) {
            key.clear();
            key.put(this.getCurrentKey());
            key.rewind();

            value.clear();
            value.putAll(this.getCurrentValue());

            return true;
        }
        return false;
    }

    public ByteBuffer createKey() {
        return ByteBuffer.wrap(new byte[this.keyBufferSize]);
    }

    public SortedMap<ByteBuffer, IColumn> createValue() {
        return new TreeMap<ByteBuffer, IColumn>();
    }

    public long getPos() throws IOException {
        return (long) iter.rowsRead();
    }
}
