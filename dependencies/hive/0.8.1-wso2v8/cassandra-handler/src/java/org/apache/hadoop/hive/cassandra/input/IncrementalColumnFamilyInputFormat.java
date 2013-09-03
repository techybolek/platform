package org.apache.hadoop.hive.cassandra.input;

import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.hadoop.ColumnFamilyInputFormat;
import org.apache.cassandra.hadoop.ConfigHelper;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
public class IncrementalColumnFamilyInputFormat extends ColumnFamilyInputFormat {
    private static final Logger logger = LoggerFactory.getLogger(ColumnFamilyInputFormat.class);


    public static final int CASSANDRA_HADOOP_SPLIT_INDEX_CF_SIZE_DEFAULT = 100;
    public static final String INDEX_SPECIAL_ROW_KEY_NAME = "INDEX_ROW";

    private String cfName;
    private IPartitioner partitioner;
    private boolean isIncremental;


    private String indexCfName;
    private String indexKeySpace;
    private static final String EVENT_INDEX_TABLE_PREFIX = "event_index_";
    private long startTime;
    private long endTime;
    private int splitSize;
    private ConsistencyLevel consistencyLevel;
    private String[] dataNodes;

    private TSocket socket;
    private Cassandra.Client client;

    public List<InputSplit> getSplits(JobContext context) throws IOException

    {
        Configuration conf = context.getConfiguration();

        isIncremental = Boolean.parseBoolean(conf.get(HiveConf.ConfVars.HIVE_INCREMENTAL_PROCESS_ENABLE.toString()));

        if (isIncremental) {
            validateConfiguration(conf);
            init(conf);
            initializeClient(conf);
            setDataNodes();

            // cannonical ranges, split into pieces, fetching the splits in parallel
            ExecutorService executor = Executors.newCachedThreadPool();
            List<InputSplit> splits = new ArrayList<InputSplit>();

            try {
                List<Future<List<InputSplit>>> splitfutures = new ArrayList<Future<List<InputSplit>>>();

                List<IncrementalSplitCallable> incrementalSplits = loadKeys();

                for (IncrementalSplitCallable splitCallable : incrementalSplits) {
                    splitfutures.add(executor.submit(splitCallable));
                }

                // wait until we have all the results back
                for (Future<List<InputSplit>> futureInputSplits : splitfutures) {
                    try {
                        splits.addAll(futureInputSplits.get());
                    } catch (Exception e) {
                        throw new IOException("Could not get input splits", e);
                    }
                }
            } finally {
                executor.shutdownNow();
            }

            assert splits.size() > 0;
            Collections.shuffle(splits, new Random(System.nanoTime()));
            return splits;
        } else {
            throw new IOException("Incremental Processing wasn't enabled.");
        }
    }

    protected static void validateConfiguration(Configuration conf) {
        if (ConfigHelper.getInputKeyspace(conf) == null || ConfigHelper.getInputColumnFamily(conf) == null) {
            throw new UnsupportedOperationException("you must set the keyspace and columnfamily with setColumnFamily()");
        }
        if (ConfigHelper.getInputSlicePredicate(conf) == null) {
            throw new UnsupportedOperationException("you must set the predicate with setPredicate");
        }
        if (ConfigHelper.getInputInitialAddress(conf) == null)
            throw new UnsupportedOperationException("You must set the initial output address to a Cassandra node");
        if (ConfigHelper.getInputPartitioner(conf) == null)
            throw new UnsupportedOperationException("You must set the Cassandra partitioner class");
    }


    private void init(Configuration conf) {
        cfName = ConfigHelper.getInputColumnFamily(conf);
        partitioner = ConfigHelper.getInputPartitioner(conf);
        logger.debug("partitioner is " + partitioner);

        String markerName = conf.get(HiveConf.ConfVars.HIVE_INCREMENTAL_MARKER_NAME.toString());
        startTime = Long.parseLong(conf.get("hive.marker." + markerName + ".from.timestamp"));
        endTime = Long.parseLong(conf.get("hive.marker." + markerName + ".to.timestamp"));

        indexCfName = EVENT_INDEX_TABLE_PREFIX + cfName;
        indexKeySpace = conf.get(HiveConf.ConfVars.HIVE_INCREMENTAL_PROCESS_KEYSPACE.toString());

        String splitSizeStr = conf.get(HiveConf.ConfVars.HIVE_CASSANDRA_HADOOP_SPLIT_INDEX_CF_SIZE.toString());
        if (null != splitSizeStr) {
            splitSize = Integer.parseInt(splitSizeStr);
        } else {
            splitSize = CASSANDRA_HADOOP_SPLIT_INDEX_CF_SIZE_DEFAULT;
        }

        consistencyLevel = ConsistencyLevel.valueOf(ConfigHelper.getReadConsistencyLevel(conf));
    }

    private void close() {
        if (socket != null && socket.isOpen()) {
            socket.close();
            socket = null;
            client = null;
        }
    }


    private void initializeClient(Configuration conf) {
        try {
            client = ConfigHelper.getClientFromInputAddressList(conf);
            client.set_keyspace(indexKeySpace);
            login(client, conf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TException e) {
            throw new RuntimeException(e);
        } catch (InvalidRequestException e) {
            throw new RuntimeException(e);
        }
    }

    private void login(Cassandra.Client client, Configuration conf) {
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", ConfigHelper.getInputKeyspaceUserName(conf));
        credentials.put("password", ConfigHelper.getInputKeyspacePassword(conf));
        try {
            client.login(new AuthenticationRequest(credentials));
        } catch (AuthenticationException e) {
            logger.error("Exception during authenticating user.", e);
        } catch (AuthorizationException e) {
            logger.error("Exception during authenticating user.", e);
        } catch (TException e) {
            logger.error("Exception during authenticating user.", e);
        }
    }

    private List<TokenRange> getRangeMap(Configuration conf) throws IOException {
        Cassandra.Client client = ConfigHelper.getClientFromInputAddressList(conf);

        login(client, conf);
        List<TokenRange> map;
        try {
            map = client.describe_ring(ConfigHelper.getInputKeyspace(conf));
        } catch (TException e) {
            throw new RuntimeException(e);
        } catch (InvalidRequestException e) {
            throw new RuntimeException(e);
        }
        return map;
    }


    private List<IncrementalSplitCallable> loadKeys() {
        long startColumnName = getColumnNameFromTimeStamp(startTime);
        long endColumnName = getColumnNameFromTimeStamp(endTime);

        long lastAccessedColName = -1;

        List<IncrementalSplitCallable> splits = new ArrayList<IncrementalSplitCallable>();

        int iteration = 0;

        while (true) {
            SlicePredicate predicate = new SlicePredicate();
            SliceRange sliceRange = new SliceRange();
            long splitStartColumnName;
            long splitEndColumnName;
            if (lastAccessedColName == -1) {
                splitStartColumnName = startColumnName;

            } else {
                splitStartColumnName = lastAccessedColName + 1;
            }
            splitEndColumnName = endColumnName;

            if (splitStartColumnName > splitEndColumnName) {
                break;
            }
            sliceRange.setStart(ByteBufferUtil.bytes(splitStartColumnName));
            sliceRange.setFinish(ByteBufferUtil.bytes(splitEndColumnName));
            sliceRange.setCount(splitSize);
            predicate.setSlice_range(sliceRange);

            List<ColumnOrSuperColumn> indexKeyList = null;

            try {
                indexKeyList = client.get_slice(ByteBuffer.wrap(INDEX_SPECIAL_ROW_KEY_NAME.getBytes()),
                        new ColumnParent(indexCfName), predicate, consistencyLevel);

                if (indexKeyList.size() > 0) {
                    long endKey = -1;

                    IncrementalSplitCallable splitCallable = new IncrementalSplitCallable();
                    for (int i = 0; i < indexKeyList.size(); i++) {
                        long key = indexKeyList.get(i).getColumn().bufferForName().getLong();
                        splitCallable.keys.add(key);

                        if (i == indexKeyList.size() - 1) {
                            endKey = key;
                        }
                    }

                    if (iteration == 0) {
                        splitCallable.startColName = Long.parseLong(String.valueOf(startTime) + "0000");
                    }
                    splits.add(splitCallable);
                    lastAccessedColName = endKey;
                    iteration++;
                } else {
                    break;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (splits.size() > 0) {
            splits.get(splits.size() - 1).endColName = Long.parseLong(String.valueOf(endTime) + "0000");
        }
        return splits;

    }

    private void setDataNodes() {
        ArrayList<String> nodes = new ArrayList<String>();
        try {
            List<TokenRange> ranges = client.describe_ring(indexKeySpace);
            for (TokenRange range : ranges) {
                String[] endPoints = range.endpoints.toArray(new String[range.endpoints.size()]);
                for (String endPoint : endPoints) {
                    if (!nodes.contains(endPoint))
                        nodes.add(endPoint);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        dataNodes = nodes.toArray(new String[nodes.size()]);
    }


    private long getColumnNameFromTimeStamp(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }


    public class IncrementalSplitCallable implements Callable<List<InputSplit>> {

        private long startColName;
        private long endColName;

        private ArrayList<Long> keys;

        IncrementalSplitCallable() {
            this.keys = new ArrayList<Long>();
            this.startColName = -1;
            this.endColName = -1;
        }

        public long getStartColName() {
            return startColName;
        }

        public long getEndColName() {
            return endColName;
        }

        public ArrayList<Long> getKeys() {
            return keys;
        }

        public List<InputSplit> call() throws Exception {
            ArrayList<InputSplit> splits = new ArrayList<InputSplit>();
            IncrementalColumnFamilySplit split = new IncrementalColumnFamilySplit(
                    getKeyArray(), startColName, endColName, dataNodes);
            splits.add(split);
            return splits;
        }

        private long[] getKeyArray() {
            long[] keyArray = new long[keys.size()];
            for (int i = 0; i < keys.size(); i++) {
                keyArray[i] = keys.get(i);
            }
            return keyArray;
        }


    }


}
