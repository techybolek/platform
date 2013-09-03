package org.apache.hadoop.hive.ql.udf.generic;

import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveContext;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CassandraDBHandler {
    private static final Logger log = LoggerFactory.getLogger(CassandraDBHandler.class);

    private static CassandraDBHandler instance;

    private final static Map<String, String> cassandraValidatorMap = new HashMap<String, String>();

    private Cassandra.Client client;
    private KsDef ksDef;
    private CfDef cfDef;
    private String host;
    private String keyspace;
    private String username;
    private String password;

    static {
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.STRING.name(), "org.apache.cassandra.db.marshal.UTF8Type");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.INT.name(), "org.apache.cassandra.db.marshal.Int32Type");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.SHORT.name(), "org.apache.cassandra.db.marshal.Int32Type");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.BYTE.name(), "org.apache.cassandra.db.marshal.BytesType");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.LONG.name(), "org.apache.cassandra.db.marshal.LongType");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.BOOLEAN.name(), "org.apache.cassandra.db.marshal.BooleanType");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.FLOAT.name(), "org.apache.cassandra.db.marshal.FloatType");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.BINARY.name(), "org.apache.cassandra.db.marshal.BytesType");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.DOUBLE.name(), "org.apache.cassandra.db.marshal.DoubleType");
        cassandraValidatorMap.put(PrimitiveObjectInspector.PrimitiveCategory.TIMESTAMP.name(), "org.apache.cassandra.db.marshal.DateType");
    }

    public CassandraDBHandler() {
        init();
    }


    public void login() {
        Map<String, String> credentials = new HashMap<String, String>();
        credentials = new HashMap<String, String>();
        credentials.put(IAuthenticator.USERNAME_KEY, username);
        credentials.put(IAuthenticator.PASSWORD_KEY, password);
        try {
            client.login(new AuthenticationRequest(credentials));
        } catch (AuthenticationException e) {
            log.error("Exception during authenticating user.", e);
        } catch (AuthorizationException e) {
            log.error("Exception during authenticating user.", e);
        } catch (TException e) {
            log.error("Exception during authenticating user.", e);
        }
    }


    public boolean createCFIfNotExists(String cfName, PrimitiveObjectInspector.PrimitiveCategory defaultValidationType) {
        CfDef cfDef = getColumnFamily(cfName);
        if (null == cfDef) {
            createColumnFamily(cfName, defaultValidationType);
            return true;
        }
        return false;
    }


    /**
     * Get Column family based on the configuration in the table. If nothing is found, return null.
     *
     * @param cfName
     * @return
     */
    public CfDef getColumnFamily(String cfName) {
        if (null == cfDef) {
            for (CfDef cf : ksDef.getCf_defs()) {
                if (cf.getName().equalsIgnoreCase(cfName)) {
                    cfDef = cf;
                    break;
                }
            }
        }
        return cfDef;
    }

    public void createColumnFamily(String cfName, PrimitiveObjectInspector.PrimitiveCategory defaultValidationType) {
        CfDef cf = new CfDef();
        cf.setKeyspace(keyspace);
        cf.setName(cfName);

        String keyValidationClass = cassandraValidatorMap.get(
                PrimitiveObjectInspector.PrimitiveCategory.STRING.name());
        String defaultValidationClass = cassandraValidatorMap.get(
                defaultValidationType.name());
        String comparatorClass = cassandraValidatorMap.get(
                PrimitiveObjectInspector.PrimitiveCategory.STRING.name());

        cf.setDefault_validation_class(defaultValidationClass);
        cf.setKey_validation_class(keyValidationClass);
        cf.setComparator_type(comparatorClass);

        try {
            client.system_add_column_family(cf);

            boolean cfCreated = false;

            for (int i = 0; i < 3; i++) {
                ksDef = client.describe_keyspace(keyspace);
                Iterator<CfDef> iterator = ksDef.getCf_defsIterator();

                while (iterator.hasNext()) {
                    if (iterator.next().getName().equalsIgnoreCase(cfName)) {
                        cfCreated = true;
                        break;
                    }
                }
                if (cfCreated) break;
                i++;
            }

            if (!cfCreated) {
                throw new RuntimeException("Cannot create column family " + cfName);
            }
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Error occurred while creating the column family " + cf + ". "
                    + e.getMessage(), e);
        } catch (SchemaDisagreementException e) {
            throw new RuntimeException("Error occurred while creating the column family " + cf + ". "
                    + e.getMessage(), e);
        } catch (TException e) {
            throw new RuntimeException("Error occurred while creating the column family " + cf + ". "
                    + e.getMessage(), e);
        } catch (NotFoundException e) {
            throw new RuntimeException("Error occurred while creating the column family " + cf + ". "
                    + e.getMessage(), e);
        }
    }

    private void init() {
        host = HiveContext.getCurrentContext().getConf().get(HiveConf.ConfVars.
                HIVE_INCREMENTAL_PROCESSING_INTERMEDIATE_RESULTS_CASSANDRA_HOSTS.varname);
        keyspace = HiveContext.getCurrentContext().getConf().get(HiveConf.ConfVars.
                HIVE_INCREMENTAL_PROCESSING_INTERMEDIATE_RESULTS_KEYSPACE.varname);
        username = HiveContext.getCurrentContext().getConf().get(HiveConf.ConfVars.
                HIVE_INCREMENTAL_PROCESSING_INTERMEDIATE_RESULTS_CASSANDRA_USERNAME.varname);
        password = HiveContext.getCurrentContext().getConf().get(HiveConf.ConfVars.
                HIVE_INCREMENTAL_PROCESSING_INTERMEDIATE_RESULTS_CASSANDRA_PASSWORD.varname);

        Map<String, String> credentials = null;
        if (username != null) {
            credentials = new HashMap<String, String>();
            credentials.put(IAuthenticator.USERNAME_KEY, username);
            credentials.put(IAuthenticator.PASSWORD_KEY, password);
        }

        String[] url = host.split(":");
        TSocket socket = new TSocket(url[0].trim(), Integer.parseInt(url[1].trim()));
        TTransport transport = new TFramedTransport(socket);

        try {
            transport.open();
        } catch (TTransportException e) {
            throw new RuntimeException("Error while trying to connect the incremental " +
                    " intermediate cassandra database. " + e.getMessage(), e);
        }

        client = new Cassandra.Client(new org.apache.thrift.protocol.TBinaryProtocol(transport));

        if (credentials != null) {
            try {
                client.login(new AuthenticationRequest(credentials));
                createKSIfNotExists(client, keyspace);
                client.set_keyspace(keyspace);
            } catch (AuthenticationException e) {
                throw new RuntimeException("Unable to authenticate \n" +
                        e.getMessage(), e);
            } catch (AuthorizationException e) {
                throw new RuntimeException("Unable to authrorize \n" +
                        e.getMessage(), e);
            } catch (TException e) {
                throw new RuntimeException("Unable to connect to keyspace " + keyspace + " \n" +
                        e.getMessage(), e);
            } catch (InvalidRequestException e) {
                throw new RuntimeException("Unable to connect to keyspace " + keyspace + " \n" +
                        e.getMessage(), e);
            }
        }
    }

    public void createKSIfNotExists(Cassandra.Client client, String keyspaceName) {
        try {
            ksDef = client.describe_keyspace(keyspaceName);
            if (null == ksDef) {
                ksDef = createKS(client, keyspaceName);
            }
        } catch (NotFoundException e) {
            throw new RuntimeException("Unable to find keyspace " + keyspaceName + " \n" +
                    e.getMessage(), e);
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Unable to find keyspace " + keyspaceName + " \n" +
                    e.getMessage(), e);
        } catch (TException e) {
            throw new RuntimeException("Unable to find keyspace " + keyspaceName + " \n" +
                    e.getMessage(), e);
        }
    }

    public ColumnOrSuperColumn getColumn(String cfName, String rowkey, String columnKey) {
        SliceRange sliceRange = new SliceRange();
        sliceRange.setStart(ByteBufferUtil.bytes(columnKey));
        sliceRange.setFinish(ByteBufferUtil.bytes(columnKey));
        SlicePredicate predicate = new SlicePredicate();
        predicate.setSlice_range(sliceRange);

        try {
            List<ColumnOrSuperColumn> result = client.get_slice(ByteBufferUtil.bytes(rowkey), new ColumnParent(cfName),
                    predicate, ConsistencyLevel.QUORUM);
            if (result == null || result.size() == 0) {
                return null;
            } else {
                return result.get(0);
            }
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Error while fetching the results for row key=" + rowkey + ", columnKey="
                    + columnKey + " in columnFamily = " + cfName + "\n" +
                    e.getMessage(), e);
        } catch (UnavailableException e) {
            throw new RuntimeException("Error while fetching the results for row key=" + rowkey + ", columnKey="
                    + columnKey + " in columnFamily = " + cfName + "\n" +
                    e.getMessage(), e);
        } catch (TimedOutException e) {
            throw new RuntimeException("Error while fetching the results for row key=" + rowkey + ", columnKey="
                    + columnKey + " in columnFamily = " + cfName + "\n" +
                    e.getMessage(), e);
        } catch (TException e) {
            throw new RuntimeException("Error while fetching the results for row key=" + rowkey + ", columnKey="
                    + columnKey + " in columnFamily = " + cfName + "\n" +
                    e.getMessage(), e);
        }
    }

    public KsDef createKS(Cassandra.Client client, String keyspaceName) {
        KsDef ksDef = new KsDef();
        ksDef.name = keyspaceName;
        ksDef.replication_factor = 1;
        ksDef.strategy_class = "org.apache.cassandra.locator.SimpleStrategy";
        try {
            client.system_add_keyspace(ksDef);
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Unable to create to keyspace " + keyspaceName + " \n" +
                    e.getMessage(), e);
        } catch (SchemaDisagreementException e) {
            throw new RuntimeException("Unable to create to keyspace " + keyspaceName + " \n" +
                    e.getMessage(), e);
        } catch (TException e) {
            throw new RuntimeException("Unable to create to keyspace " + keyspaceName + " \n" +
                    e.getMessage(), e);
        }
        return ksDef;
    }

    public void addCFMetaData(String cfName, ByteBuffer columnName,
                              PrimitiveObjectInspector.PrimitiveCategory validationType) {
        CfDef cfDef = getColumnFamily(cfName);
        if (!isMetaDataSet(cfDef, columnName, validationType)) {
            cfDef.addToColumn_metadata(new ColumnDef(columnName
                    , cassandraValidatorMap.get(validationType.name())));
            try {
                client.system_update_column_family(cfDef);
            } catch (InvalidRequestException e) {
                throw new RuntimeException("Error occurred while updating the column family " + cfName + ". "
                        + e.getMessage(), e);
            } catch (SchemaDisagreementException e) {
                throw new RuntimeException("Error occurred while updating the column family " + cfName + ". "
                        + e.getMessage(), e);
            } catch (TException e) {
                throw new RuntimeException("Error occurred while updating the column family " + cfName + ". "
                        + e.getMessage(), e);
            }
        }
    }

    private boolean isMetaDataSet(CfDef cfDef, ByteBuffer columnName, PrimitiveObjectInspector.PrimitiveCategory
            validationType) {
        Iterator<ColumnDef> iterator = cfDef.getColumn_metadataIterator();
        while (iterator.hasNext()) {
            ColumnDef columnDef = iterator.next();
            if (columnDef.bufferForName().equals(columnName)) {
                return columnDef.getValidation_class().equalsIgnoreCase(cassandraValidatorMap.get(validationType.name()));
            }
        }
        return false;
    }


    public void insertData(String cfName, ByteBuffer rowKey, ByteBuffer columnKey, byte[] value) {
        Column col = new Column();
        col.setName(columnKey);
        col.setValue(value);
        col.setTimestamp(System.currentTimeMillis());
        login();
        try {
            client.insert(rowKey, new ColumnParent(cfName), col, ConsistencyLevel.QUORUM);
        } catch (InvalidRequestException e) {
            throw new RuntimeException("Error while updating the value in incremental processing intermediate table. "
                    + cfName, e);
        } catch (UnavailableException e) {
            throw new RuntimeException("Error while updating the value in incremental processing intermediate table. "
                    + cfName, e);
        } catch (TException e) {
            throw new RuntimeException("Error while updating the value in incremental processing intermediate table. "
                    + cfName, e);
        } catch (TimedOutException e) {
            throw new RuntimeException("Error while updating the value in incremental processing intermediate table. "
                    + cfName, e);
        }
    }


}
