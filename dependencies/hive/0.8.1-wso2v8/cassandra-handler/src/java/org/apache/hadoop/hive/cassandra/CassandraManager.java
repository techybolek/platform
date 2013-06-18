package org.apache.hadoop.hive.cassandra;

import org.apache.cassandra.thrift.*;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.hadoop.hive.cassandra.serde.AbstractColumnSerDe;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.wso2.carbon.hadoop.hive.jdbc.storage.datasource.CarbonDataSourceFetcher;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to handle the transaction to cassandra backend database.
 */
public class CassandraManager {
    final static public int DEFAULT_REPLICATION_FACTOR = 1;
    final static public String DEFAULT_STRATEGY = "org.apache.cassandra.locator.SimpleStrategy";

    final static private String CASSANDRA_BYTE_VALIDATOR_TYPE =
            "org.apache.cassandra.db.marshal.BytesType";
    final static private String CASSANDRA_UTF8_VALIDATOR_TYPE =
            "org.apache.cassandra.db.marshal.UTF8Type";

    final static public String USERNAME_PROPERTY = "username";
    final static public String PASSWORD_PROPERTY = "password";

    private static final String STREAM_NAME_KEY = "Name";

    private static final String STREAM_VERSION_KEY = "Version";
    private static final String STREAM_NICK_NAME_KEY = "Nick_Name";
    private static final String STREAM_TIMESTAMP_KEY = "Timestamp";
    private static final String STREAM_DESCRIPTION_KEY = "Description";

    private static final String STREAM_ID_KEY = "StreamId";

    final static private Map<String, String> cassandraValidatorMap = new HashMap<String, String>();

    //Cassandra Host Name
    private final String host;

    //Cassandra Host Port
    private int port;

    //Cassandra proxy client
    private CassandraProxyClient clientHolder;

    //Whether or not use framed connection
    private boolean framedConnection;

    //table property
    private final Table tbl;

    //key space name
    private String keyspace;

    //username to authenticate to keyspace
    private String username;

    //password to authenticate to keyspace
    private String password;

    //column family name
    private String columnFamilyName;

    static {
        cassandraValidatorMap.put("STRING", "org.apache.cassandra.db.marshal.UTF8Type");
        cassandraValidatorMap.put("INT", "org.apache.cassandra.db.marshal.Int32Type");
        cassandraValidatorMap.put("SMALLINT", "org.apache.cassandra.db.marshal.Int32Type");
        cassandraValidatorMap.put("TINYINT", "org.apache.cassandra.db.marshal.Int32Type");
        cassandraValidatorMap.put("BIGINT", "org.apache.cassandra.db.marshal.LongType");
        cassandraValidatorMap.put("BOOLEAN", "org.apache.cassandra.db.marshal.BooleanType");
        cassandraValidatorMap.put("FLOAT", "org.apache.cassandra.db.marshal.FloatType");
        cassandraValidatorMap.put("BINARY", "org.apache.cassandra.db.marshal.BytesType");
    }

    /**
     * Construct a cassandra manager object from meta table object.
     */
    public CassandraManager(Table tbl) throws MetaException {
        Map<String, String> serdeParam = tbl.getSd().getSerdeInfo().getParameters();

        String cassandraDataSource = serdeParam.get(AbstractColumnSerDe.CASSANDRA_DATA_SOURCE_NAME);
        String cassandraPortStr = serdeParam.get(AbstractColumnSerDe.CASSANDRA_PORT);
        if (cassandraDataSource != null) {
            CarbonDataSourceFetcher carbonDataSourceFetcher = new CarbonDataSourceFetcher();
            Map<String, String> dataSource = carbonDataSourceFetcher.getCarbonDataSource(
                    serdeParam.get(AbstractColumnSerDe.CASSANDRA_DATA_SOURCE_NAME));

            String urlString = dataSource.get(AbstractColumnSerDe.CASSANDRA_JDBC_URL);
            this.username = dataSource.get(AbstractColumnSerDe.CASSANDRA_JDBC_USERNAME);
            this.password = dataSource.get(AbstractColumnSerDe.CASSANDRA_JDBC_PASSWORD);

            // Format of the URL - jdbc:cassandra://[host-1]:[port]/[keyspace],..,jdbc:cassandra://[host-n]:[port]/[keyspace]
            if (urlString != null) {
                String urls[] = urlString.split(",");

                StringBuffer hosts = new StringBuffer();
                this.port = Integer.parseInt(AbstractColumnSerDe.DEFAULT_CASSANDRA_PORT);

                boolean firstIteration = true;
                for (String url : urls) {
                    URI connectionURI = URI.create(url.substring(5));
                    String host = connectionURI.getHost();
                    hosts.append(host);
                    hosts.append(",");

                    if (firstIteration) {
                        this.port = connectionURI.getPort();
                        keyspace = connectionURI.getPath().replace("/", "");
                    }

                    firstIteration = false;
                }

                this.host = hosts.substring(0, hosts.length() - 1);
            } else {
                this.host = AbstractColumnSerDe.DEFAULT_CASSANDRA_HOST;
                this.port = Integer.parseInt(AbstractColumnSerDe.DEFAULT_CASSANDRA_PORT);
            }
        } else {
            String cassandraHost = serdeParam.get(AbstractColumnSerDe.CASSANDRA_HOST);
            if (cassandraHost == null) {
                cassandraHost = AbstractColumnSerDe.DEFAULT_CASSANDRA_HOST;
            }

            this.host = cassandraHost;

            if (cassandraPortStr == null) {
                cassandraPortStr = AbstractColumnSerDe.DEFAULT_CASSANDRA_PORT;
            }

            String userName = serdeParam.get(AbstractColumnSerDe.CASSANDRA_KEYSPACE_USERNAME);
            if (userName != null) {
                this.username = userName;
            }

            String password = serdeParam.get(AbstractColumnSerDe.CASSANDRA_KEYSPACE_PASSWORD);
            if (password != null) {
                this.password = password;
            }


            try {
                port = Integer.parseInt(cassandraPortStr);
            } catch (NumberFormatException e) {
                throw new MetaException(AbstractColumnSerDe.CASSANDRA_PORT + " must be a number");
            }
        }


        this.tbl = tbl;
        init();
    }

    private void init() {
        if (keyspace == null) {
            this.keyspace = getCassandraKeyspace();
        }
        this.columnFamilyName = getCassandraColumnFamily();
        this.framedConnection = true;
    }

    /**
     * Open connection to the cassandra server.
     *
     * @throws MetaException
     */
    public void openConnection() throws MetaException {

        Map<String, String> credentials = null;
        if (username != null) {
            credentials = new HashMap<String, String>();
            credentials.put(USERNAME_PROPERTY, this.username);
            credentials.put(PASSWORD_PROPERTY, this.password);
        }

        try {
            if (username != null) {
                clientHolder = new CassandraProxyClient(host, port, keyspace, credentials, framedConnection, true);
            } else {
                clientHolder = new CassandraProxyClient(host, port, keyspace, framedConnection, true);
            }
        } catch (CassandraException e) {
            throw new MetaException("Unable to connect to the server " + e.getMessage());
        }
    }

    /**
     * Close connection to the cassandra server.
     */
    public void closeConnection() {
        if (clientHolder != null) {
            clientHolder.close();
        }
    }

    /**
     * Return a keyspace description for the given keyspace name from the cassandra host.
     *
     * @return keyspace description
     */
    public KsDef getKeyspaceDesc() throws NotFoundException, MetaException {
        try {
            return clientHolder.getProxyConnection().describe_keyspace(keyspace);
        } catch (TException e) {
            throw new MetaException("An internal exception prevented this action from taking place."
                                    + e.getMessage());
        } catch (InvalidRequestException e) {
            throw new MetaException("An internal exception prevented this action from taking place."
                                    + e.getMessage());
        }
    }

    /**
     * Get Column family based on the configuration in the table. If nothing is found, return null.
     */
    private CfDef getColumnFamily(KsDef ks) {
        for (CfDef cf : ks.getCf_defs()) {
            if (cf.getName().equalsIgnoreCase(columnFamilyName)) {
                return cf;
            }
        }

        return null;
    }

    /**
     * Get CfDef based on the configuration in the table.
     */
    private CfDef getCfDef() throws MetaException {
        CfDef cf = new CfDef();
        cf.setKeyspace(keyspace);
        cf.setName(columnFamilyName);

        cf.setColumn_type(getColumnType());

        Map<String, String> columnValidators = getCassandraColumnValidators();
        String keyValidationClass = getKeyValidationClass();
        String defaultValidationClass = getDefaultValidationClass();
        String comparatorClass = getComparatorClass();

        for (Map.Entry<String, String> column : columnValidators.entrySet()) {
            if (column.getValue() != null) {
                cf.addToColumn_metadata(new ColumnDef(ByteBufferUtil.bytes(column.getKey()),
                                                      column.getValue()));
            }
        }

        cf.setDefault_validation_class(defaultValidationClass);
        cf.setKey_validation_class(keyValidationClass);
        cf.setComparator_type(comparatorClass);

        return cf;
    }

    /**
     * Create a keyspace with columns defined in the table.
     */
    public KsDef createKeyspaceWithColumns() throws MetaException {
        try {
            KsDef ks = new KsDef();
            ks.setName(getCassandraKeyspace());
            ks.setReplication_factor(getReplicationFactor());
            ks.setStrategy_class(getStrategy());

            ks.addToCf_defs(getCfDef());

            clientHolder.getProxyConnection().system_add_keyspace(ks);
            clientHolder.getProxyConnection().set_keyspace(keyspace);
            return ks;
        } catch (TException e) {
            throw new MetaException("Unable to create key space '" + keyspace + "'. Error:"
                                    + e.getMessage());
        } catch (InvalidRequestException e) {
            throw new MetaException("Unable to create key space '" + keyspace + "'. Error:"
                                    + e.getMessage());
        } catch (SchemaDisagreementException e) {
            throw new MetaException("Unable to create key space '" + keyspace + "'. Error:"
                                    + e.getMessage());
        }

    }

    /**
     * Create the column family if it doesn't exist.
     *
     * @param ks
     * @return
     * @throws MetaException
     */
    public CfDef createCFIfNotFound(KsDef ks) throws MetaException {
        CfDef cf = getColumnFamily(ks);
        if (cf == null) {
            return createColumnFamily();
        } else {
            return cf;
        }
    }

    /**
     * Create column family based on the configuration in the table.
     */
    public CfDef createColumnFamily() throws MetaException {
        CfDef cf = getCfDef();
        try {
            clientHolder.getProxyConnection().set_keyspace(keyspace);
            clientHolder.getProxyConnection().system_add_column_family(cf);
            return cf;
        } catch (TException e) {
            throw new MetaException("Unable to create column family '" + columnFamilyName + "'. Error:"
                                    + e.getMessage());
        } catch (InvalidRequestException e) {
            throw new MetaException("Unable to create column family '" + columnFamilyName + "'. Error:"
                                    + e.getMessage());
        } catch (SchemaDisagreementException e) {
            throw new MetaException("Unable to create column family '" + columnFamilyName + "'. Error:"
                                    + e.getMessage());
        }

    }

    private Map<String, String> getCassandraColumnValidators() {
        String prop = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_COL_MAPPING);
        List<String> mappings;
        Map<String, String> validatorMappings = new HashMap<String, String>();
        List<FieldSchema> schemas = tbl.getSd().getCols();
        if (prop != null && schemas != null) {
            mappings = AbstractColumnSerDe.parseColumnMapping(prop);

            for (int i = 0;i < mappings.size(); i++) {
                String cassandraColumn = mappings.get(i);
                if (!cassandraColumn.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_KEY_COLUMN) &&
                    !cassandraColumn.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_COLUMN_COLUMN) &&
                    !cassandraColumn.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_SUBCOLUMN_COLUMN) &&
                    !cassandraColumn.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_VALUE_COLUMN) &&
                    !cassandraColumn.endsWith(":")) {

                    FieldSchema schema = schemas.get(i);
                    String columnType = schema.getType();
                    validatorMappings.put(cassandraColumn, cassandraValidatorMap.get(
                            columnType.trim().toUpperCase()));
                }
            }

            // Adding definitions for default Cassandra meta columns added by BAM 
            validatorMappings.put(STREAM_ID_KEY, cassandraValidatorMap.get("STRING"));
            validatorMappings.put(STREAM_NAME_KEY, cassandraValidatorMap.get("STRING"));
            validatorMappings.put(STREAM_VERSION_KEY, cassandraValidatorMap.get("STRING"));
            validatorMappings.put(STREAM_DESCRIPTION_KEY, cassandraValidatorMap.get("STRING"));
            validatorMappings.put(STREAM_NICK_NAME_KEY, cassandraValidatorMap.get("STRING"));
            validatorMappings.put(STREAM_TIMESTAMP_KEY, cassandraValidatorMap.get("BIGINT"));

        }

        return validatorMappings;
    }

    private String getKeyValidationClass() {
        String prop = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_COL_MAPPING);
        List<String> mappings;
        List<FieldSchema> schemas = tbl.getSd().getCols();
        if (prop != null && schemas != null) {
            mappings = AbstractColumnSerDe.parseColumnMapping(prop);

            for (int i = 0;i < mappings.size(); i++) {
                String cassandraColumn = mappings.get(i);
                if (cassandraColumn.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_KEY_COLUMN)) {

                    FieldSchema schema = schemas.get(i);
                    String columnType = schema.getType();

                    return cassandraValidatorMap.get(columnType.trim().toUpperCase());
                }
            }
        }

        return CASSANDRA_BYTE_VALIDATOR_TYPE;
    }

    private String getDefaultValidationClass() {
        String prop = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_COL_MAPPING);
        List<String> mappings;
        List<FieldSchema> schemas = tbl.getSd().getCols();
        if (prop != null && schemas != null) {
            mappings = AbstractColumnSerDe.parseColumnMapping(prop);

            for (int i = 0;i < mappings.size(); i++) {
                String cassandraColumn = mappings.get(i);
                if (cassandraColumn.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_VALUE_COLUMN)) {

                    FieldSchema schema = schemas.get(i);
                    String columnType = schema.getType();

                    return cassandraValidatorMap.get(columnType.trim().toUpperCase());
                }
            }
        }

        return CASSANDRA_BYTE_VALIDATOR_TYPE;
    }

    private String getComparatorClass() {
        String prop = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_COL_MAPPING);
        List<String> mappings;
        List<FieldSchema> schemas = tbl.getSd().getCols();
        if (prop != null && schemas != null) {
            mappings = AbstractColumnSerDe.parseColumnMapping(prop);

            for (int i = 0;i < mappings.size(); i++) {
                String cassandraColumn = mappings.get(i);
                if (cassandraColumn.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_COLUMN_COLUMN)) {

                    FieldSchema schema = schemas.get(i);
                    String columnType = schema.getType();

                    return cassandraValidatorMap.get(columnType.trim().toUpperCase());
                }
            }
        }

        return CASSANDRA_UTF8_VALIDATOR_TYPE;
    }

    private String getColumnType() throws MetaException {
        String prop = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_COL_MAPPING);
        List<String> mapping;
        if (prop != null) {
            mapping = AbstractColumnSerDe.parseColumnMapping(prop);
        } else {
            List<FieldSchema> schema = tbl.getSd().getCols();
            if (schema.size() == 0) {
                throw new MetaException("Can't find table column definitions");
            }

            String[] colNames = new String[schema.size()];
            for (int i = 0; i < schema.size(); i++) {
                colNames[i] = schema.get(i).getName();
            }

            String mappingStr = AbstractColumnSerDe.createColumnMappingString(colNames);
            mapping = Arrays.asList(mappingStr.split(","));
        }

        boolean hasKey = false;
        boolean hasColumn = false;
        boolean hasValue = false;
        boolean hasSubColumn = false;

        for (String column : mapping) {
            if (column.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_KEY_COLUMN)) {
                hasKey = true;
            } else if (column.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_COLUMN_COLUMN)) {
                hasColumn = true;
            } else if (column.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_SUBCOLUMN_COLUMN)) {
                hasSubColumn = true;
            } else if (column.equalsIgnoreCase(AbstractColumnSerDe.CASSANDRA_VALUE_COLUMN)) {
                hasValue = true;
            } else {
                return "Standard";
            }
        }

        if (hasKey && hasColumn && hasValue) {
            if (hasSubColumn) {
                return "Super";
            } else {
                return "Standard";
            }
        } else {
            return "Standard";
        }
    }

    /**
     * Get replication factor from the table property.
     *
     * @return replication factor
     * @throws MetaException error
     */
    private int getReplicationFactor() throws MetaException {
        String prop = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_KEYSPACE_REPFACTOR);
        if (prop == null) {
            return DEFAULT_REPLICATION_FACTOR;
        } else {
            try {
                return Integer.parseInt(prop);
            } catch (NumberFormatException e) {
                throw new MetaException(AbstractColumnSerDe.CASSANDRA_KEYSPACE_REPFACTOR + " must be a number");
            }
        }
    }

    /**
     * Get replication strategy from the table property.
     *
     * @return strategy
     */
    private String getStrategy() {
        String prop = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_KEYSPACE_STRATEGY);
        if (prop == null) {
            return DEFAULT_STRATEGY;
        } else {
            return prop;
        }
    }

    /**
     * Get keyspace name from the table property.
     *
     * @return keyspace name
     */
    private String getCassandraKeyspace() {
        String tableName = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_KEYSPACE_NAME);

        if (tableName == null) {
            tableName = tbl.getDbName();
        }

        tbl.getParameters().put(AbstractColumnSerDe.CASSANDRA_KEYSPACE_NAME, tableName);

        return tableName;
    }

    /**
     * Get cassandra column family from table property.
     *
     * @return cassandra column family name
     */
    private String getCassandraColumnFamily() {
        String tableName = getPropertyFromTable(AbstractColumnSerDe.CASSANDRA_CF_NAME);

        if (tableName == null) {
            tableName = tbl.getTableName();
        }

        tbl.getParameters().put(AbstractColumnSerDe.CASSANDRA_CF_NAME, tableName);

        return tableName;
    }

    /**
     * Get the value for a given name from the table.
     * It first checks the table property. If it is not there, it checks the serde properties.
     *
     * @param columnName given name
     * @return value
     */
    private String getPropertyFromTable(String columnName) {
        String prop = tbl.getParameters().get(columnName);
        if (prop == null) {
            prop = tbl.getSd().getSerdeInfo().getParameters().get(columnName);
        }

        return prop;
    }

    /**
     * Drop the table defined in the query.
     */
    public void dropTable() throws MetaException {
        try {
            clientHolder.getProxyConnection().system_drop_column_family(columnFamilyName);
        } catch (TException e) {
            throw new MetaException("Unable to drop column family '" + columnFamilyName + "'. Error:"
                                    + e.getMessage());
        } catch (InvalidRequestException e) {
            throw new MetaException("Unable to drop column family '" + columnFamilyName + "'. Error:"
                                    + e.getMessage());
        } catch (SchemaDisagreementException e) {
            throw new MetaException("Unable to drop column family '" + columnFamilyName + "'. Error:"
                                    + e.getMessage());
        }
    }

}
