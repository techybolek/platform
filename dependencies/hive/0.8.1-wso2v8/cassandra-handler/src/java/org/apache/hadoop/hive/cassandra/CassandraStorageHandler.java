package org.apache.hadoop.hive.cassandra;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.cassandra.input.HiveCassandraStandardColumnInputFormat;
import org.apache.hadoop.hive.cassandra.output.HiveCassandraOutputFormat;
import org.apache.hadoop.hive.cassandra.serde.AbstractColumnSerDe;
import org.apache.hadoop.hive.cassandra.serde.CassandraColumnSerDe;
import org.apache.hadoop.hive.metastore.HiveMetaHook;
import org.apache.hadoop.hive.metastore.MetaStoreUtils;
import org.apache.hadoop.hive.metastore.api.Constants;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.ql.metadata.HiveStorageHandler;
import org.apache.hadoop.hive.ql.plan.TableDesc;
import org.apache.hadoop.hive.serde2.SerDe;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.OutputFormat;
import org.wso2.carbon.hadoop.hive.jdbc.storage.datasource.CarbonDataSourceFetcher;

public class CassandraStorageHandler
  implements HiveStorageHandler, HiveMetaHook {

  private Configuration configuration;

  @Override
  public void configureTableJobProperties(TableDesc tableDesc, Map<String, String> jobProperties) {
    Properties tableProperties = tableDesc.getProperties();

    // Try parsing the keyspace.columnFamily
    String tableName = tableProperties.getProperty(Constants.META_TABLE_NAME);
    String dbName = tableProperties.getProperty(Constants.META_TABLE_DB);

    String keyspace = tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_KEYSPACE_NAME);
    String columnFamily = tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_CF_NAME);

    String cassandraDataSource = tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_DATA_SOURCE_NAME);
    //Identify Keyspace
    if (keyspace == null && cassandraDataSource == null) {
        keyspace = dbName;
    }

    if (cassandraDataSource != null) {
        CarbonDataSourceFetcher carbonDataSourceFetcher = new CarbonDataSourceFetcher();
        Map<String, String> dataSource = carbonDataSourceFetcher.getCarbonDataSource(
                        tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_DATA_SOURCE_NAME));

        String urlString = dataSource.get(AbstractColumnSerDe.CASSANDRA_JDBC_URL);
        String username = dataSource.get(AbstractColumnSerDe.CASSANDRA_JDBC_USERNAME);
        String password = dataSource.get(AbstractColumnSerDe.CASSANDRA_JDBC_PASSWORD);

        // Format of the URL - jdbc:cassandra://[host-1]:[port]/[keyspace],..,jdbc:cassandra://[host-n]:[port]/[keyspace]
        if (urlString != null) {
            String urls[] = urlString.split(",");

            StringBuffer hosts = new StringBuffer();
            String port = AbstractColumnSerDe.DEFAULT_CASSANDRA_PORT;

            boolean firstIteration = true;
            for (String url : urls) {
                URI connectionURI = URI.create(url.substring(5));
                String host = connectionURI.getHost();
                hosts.append(host);
                hosts.append(",");

                if (firstIteration) {
                  port = Integer.toString(connectionURI.getPort());
                  keyspace = connectionURI.getPath().replace("/", "");
                }

                firstIteration = false;
            }

            String hostEntry = hosts.substring(0, hosts.length() - 1);

            jobProperties.put(AbstractColumnSerDe.CASSANDRA_KEYSPACE_NAME, keyspace);

            jobProperties.put(AbstractColumnSerDe.CASSANDRA_HOST, hostEntry);

            jobProperties.put(AbstractColumnSerDe.CASSANDRA_PORT, port);

            jobProperties.put(AbstractColumnSerDe.CASSANDRA_INPUT_KEYSPACE_USERNAME_CONFIG,
                              username);

            jobProperties.put(AbstractColumnSerDe.CASSANDRA_INPUT_KEYSPACE_PASSWD_CONFIG,
                              password);
        }
    } else {
        jobProperties.put(AbstractColumnSerDe.CASSANDRA_KEYSPACE_NAME, keyspace);

        jobProperties.put(AbstractColumnSerDe.CASSANDRA_HOST,
          tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_HOST, AbstractColumnSerDe.DEFAULT_CASSANDRA_HOST));

        jobProperties.put(AbstractColumnSerDe.CASSANDRA_PORT,
          tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_PORT, AbstractColumnSerDe.DEFAULT_CASSANDRA_PORT));

        jobProperties.put(AbstractColumnSerDe.CASSANDRA_INPUT_KEYSPACE_USERNAME_CONFIG,
        tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_KEYSPACE_USERNAME, ""));

        jobProperties.put(AbstractColumnSerDe.CASSANDRA_INPUT_KEYSPACE_PASSWD_CONFIG,
                tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_KEYSPACE_PASSWORD, ""));
    }


    //Identify ColumnFamily
    if (columnFamily == null) {
        columnFamily = tableName;
    }

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_CF_NAME, columnFamily);

    //If no column mapping has been configured, we should create the default column mapping.
    String columnInfo = tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_COL_MAPPING);
    if(columnInfo == null) {
      columnInfo = AbstractColumnSerDe.createColumnMappingString(
        tableProperties.getProperty(org.apache.hadoop.hive.serde.Constants.LIST_COLUMNS));
    }

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_COL_MAPPING, columnInfo);


    jobProperties.put(AbstractColumnSerDe.CASSANDRA_PARTITIONER,
      tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_PARTITIONER,
        "org.apache.cassandra.dht.RandomPartitioner"));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_THRIFT_MODE,
      tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_THRIFT_MODE, "framed"));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_CONSISTENCY_LEVEL,
      tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_CONSISTENCY_LEVEL,
          AbstractColumnSerDe.DEFAULT_CONSISTENCY_LEVEL));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_RANGE_BATCH_SIZE,
      tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_RANGE_BATCH_SIZE,
        Integer.toString(AbstractColumnSerDe.DEFAULT_RANGE_BATCH_SIZE)));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_SIZE,
      tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_SIZE,
        Integer.toString(AbstractColumnSerDe.DEFAULT_SLICE_PREDICATE_SIZE)));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_SPLIT_SIZE,
      tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_SPLIT_SIZE,
          Integer.toString(AbstractColumnSerDe.DEFAULT_SPLIT_SIZE)));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_BATCH_MUTATION_SIZE,
        tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_BATCH_MUTATION_SIZE,
            Integer.toString(AbstractColumnSerDe.DEFAULT_BATCH_MUTATION_SIZE)));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_RANGE_FINISH,
        tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_RANGE_FINISH, ""));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_RANGE_COMPARATOR,
        tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_RANGE_COMPARATOR, ""));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_RANGE_REVERSED,
        tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_RANGE_REVERSED, "false"));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_RANGE_COUNT,
        tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_RANGE_COUNT, "false"));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_COLUMN_NAMES,
        tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_SLICE_PREDICATE_COLUMN_NAMES, ""));

    jobProperties.put(AbstractColumnSerDe.CASSANDRA_CF_COUNTERS,
            tableProperties.getProperty(AbstractColumnSerDe.CASSANDRA_CF_COUNTERS, "false"));

  }

  @Override
  public Class<? extends InputFormat> getInputFormatClass() {
    return HiveCassandraStandardColumnInputFormat.class;
  }

  @Override
  public HiveMetaHook getMetaHook() {
    return this;
  }

  @Override
  public Class<? extends OutputFormat> getOutputFormatClass() {
    return HiveCassandraOutputFormat.class;
  }

  @Override
  public Class<? extends SerDe> getSerDeClass() {
    return CassandraColumnSerDe.class;
  }

  @Override
  public Configuration getConf() {
    return this.configuration;
  }

  @Override
  public void setConf(Configuration arg0) {
    this.configuration = arg0;
  }

  @Override
  public void preCreateTable(Table table) throws MetaException {
    boolean isExternal = MetaStoreUtils.isExternalTable(table);

    if (!isExternal) {
      throw new MetaException("Cassandra tables must be external.");
    }

    if (table.getSd().getLocation() != null) {
      throw new MetaException("LOCATION may not be specified for Cassandra.");
    }

    CassandraManager manager = new CassandraManager(table);

    try {
      //open connection to cassandra
      manager.openConnection();
      KsDef ks = manager.getKeyspaceDesc();
      //create the column family if it doesn't exist.
      manager.createCFIfNotFound(ks);
    } catch(NotFoundException e) {
      manager.createKeyspaceWithColumns();
    } finally {
      manager.closeConnection();
    }
  }

  @Override
  public void commitCreateTable(Table table) throws MetaException {
    // No work needed
  }

  @Override
  public void commitDropTable(Table table, boolean deleteData) throws MetaException {
    //TODO: Should this be implemented to drop the table and its data from cassandra
    boolean isExternal = MetaStoreUtils.isExternalTable(table);
    if (deleteData && !isExternal) {
      CassandraManager manager = new CassandraManager(table);

      try {
        //open connection to cassandra
        manager.openConnection();
        //drop the table
        manager.dropTable();
      } finally {
        manager.closeConnection();
      }
    }
  }

  @Override
  public void preDropTable(Table table) throws MetaException {
    // nothing to do
  }

  @Override
  public void rollbackCreateTable(Table table) throws MetaException {
    // No work needed
  }

  @Override
  public void rollbackDropTable(Table table) throws MetaException {
    // nothing to do
  }
}
