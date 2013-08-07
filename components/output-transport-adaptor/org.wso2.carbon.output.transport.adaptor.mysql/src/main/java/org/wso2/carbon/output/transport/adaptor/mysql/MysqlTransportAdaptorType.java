/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.output.transport.adaptor.mysql;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.output.transport.adaptor.core.AbstractOutputTransportAdaptor;
import org.wso2.carbon.output.transport.adaptor.core.OutputTransportAdaptorDto;
import org.wso2.carbon.output.transport.adaptor.core.Property;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;
import org.wso2.carbon.output.transport.adaptor.mysql.internal.PooledDataSource;
import org.wso2.carbon.output.transport.adaptor.mysql.internal.TableInfo;
import org.wso2.carbon.output.transport.adaptor.mysql.internal.util.MysqlTransportAdaptorConstants;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MysqlTransportAdaptorType extends AbstractOutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(MysqlTransportAdaptorType.class);

    private static MysqlTransportAdaptorType mysqlTransportAdaptor = new MysqlTransportAdaptorType();
    private ResourceBundle resourceBundle;

    private ConcurrentHashMap<OutputTransportAdaptorConfiguration, ConcurrentHashMap<String, TableInfo>> tables;
    private ConcurrentHashMap<OutputTransportAdaptorConfiguration, PooledDataSource> pooledDataSources;
    private static final String EVENT_TRACE_LOGGER = "EVENT_TRACE_LOGGER";
    private Logger trace = Logger.getLogger(EVENT_TRACE_LOGGER);


    private MysqlTransportAdaptorType() {
        this.tables = new ConcurrentHashMap<OutputTransportAdaptorConfiguration, ConcurrentHashMap<String, TableInfo>>(32);
        this.pooledDataSources = new ConcurrentHashMap<OutputTransportAdaptorConfiguration, PooledDataSource>(8);
    }

    @Override
    protected List<OutputTransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        List<OutputTransportAdaptorDto.MessageType> supportOutputMessageTypes = new ArrayList<OutputTransportAdaptorDto.MessageType>();
        supportOutputMessageTypes.add(OutputTransportAdaptorDto.MessageType.MAP);
        return supportOutputMessageTypes;
    }

    /**
     * @return mysql transport adaptor instance
     */
    public static MysqlTransportAdaptorType getInstance() {
        return mysqlTransportAdaptor;
    }

    /**
     * @return name of the cassandra transport adaptor
     */
    @Override
    protected String getName() {
        return MysqlTransportAdaptorConstants.TRANSPORT_TYPE_MYSQL;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.output.transport.adaptor.mysql.i18n.Resources", Locale.getDefault());
    }


    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        Property hostName = new Property(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_HOSTNAME);
        hostName.setDisplayName(resourceBundle.getString(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_HOSTNAME));
        hostName.setRequired(true);
        propertyList.add(hostName);

//        // set port
        Property port = new Property(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_PORT);
        port.setDisplayName(resourceBundle.getString(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_PORT));
        port.setRequired(true);
        propertyList.add(port);

        // set user name
        Property userName = new Property(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_USER_NAME);
        userName.setDisplayName(resourceBundle.getString(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_USER_NAME));
        userName.setRequired(true);
        propertyList.add(userName);

        // set password
        Property password = new Property(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_PASSWORD);
        password.setDisplayName(resourceBundle.getString(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_PASSWORD));
        password.setRequired(true);
        password.setSecured(true);
        propertyList.add(password);

        return propertyList;
    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        Property databaseName = new Property(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_DATABASE_NAME);
        databaseName.setDisplayName(resourceBundle.getString(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_DATABASE_NAME));
        databaseName.setRequired(true);
        databaseName.setHint(resourceBundle.getString(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_DATABASE_NAME_HINT));
        propertyList.add(databaseName);

        Property tableName = new Property(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_TABLE_NAME);
        tableName.setDisplayName(resourceBundle.getString(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_TABLE_NAME));
        tableName.setRequired(true);
        propertyList.add(tableName);

        return propertyList;
    }

    /**
     * @param outputTransportMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     * @param outputTransportAdaptorConfiguration
     *                - transport configuration to be used
     */
    public void publish(
            OutputTransportAdaptorMessageConfiguration outputTransportMessageConfiguration,
            Object message,
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {

        Connection con = null;
        Statement stmt = null;
        try {
            if (message instanceof Map) {
                String databaseName = outputTransportMessageConfiguration.getOutputMessageProperties().get(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_DATABASE_NAME);
                String tableName = outputTransportMessageConfiguration.getOutputMessageProperties().get(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_TABLE_NAME);

                String fullTableName = databaseName + "." + tableName;
                ConcurrentHashMap<String, TableInfo> tableInfoMap = tables.get(outputTransportMessageConfiguration);

                createConnectionPoolIfNotExists(outputTransportAdaptorConfiguration);

                TableInfo tableInfo;
                if (tableInfoMap == null || tableInfoMap.get(fullTableName) == null) {
                    tableInfo = initializeTableInfo(databaseName, tableName, message, pooledDataSources.get(outputTransportAdaptorConfiguration));
                    if (tableInfoMap == null) {
                        tableInfoMap = new ConcurrentHashMap<String, TableInfo>();
                        tables.put(outputTransportAdaptorConfiguration, tableInfoMap);
                    }
                    tableInfoMap.put(fullTableName, tableInfo);
                } else {
                    tableInfo = tableInfoMap.get(fullTableName);
                }


                Map<String, Object> map = (Map<String, Object>) message;
                Attribute attribute;
                StringBuilder queryBuilder = new StringBuilder(tableInfo.getInsertStatementPrefix());
                for (int i = 0; i < tableInfo.getColumnOrder().size(); i++) {
                    attribute = tableInfo.getColumnOrder().get(i);
                    Object value = map.get(attribute.getName());
                    if (i > 0) {
                        queryBuilder.append(",");
                    }
                    if (value != null) {
                        switch (attribute.getType()) {
                            case STRING:
                                queryBuilder.append("'").append(value.toString()).append("'");
                                break;
                            default:
                                queryBuilder.append(value.toString());
                        }
                    } else {
                        switch (attribute.getType()) {
                            case INT:
                            case LONG:
                                queryBuilder.append("0");
                                break;
                            case FLOAT:
                            case DOUBLE:
                                queryBuilder.append("0.0");
                                break;
                            default:
                                queryBuilder.append("''");
                        }
                    }
                }

                queryBuilder.append(")");
                con = pooledDataSources.get(outputTransportAdaptorConfiguration).getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(queryBuilder.toString());
            }
        } catch (SQLException e) {
            log.error(e);
        } catch (ClassNotFoundException e) {
            log.error("unable to find the database driver", e);
        } finally {
            cleanupConnections(stmt, con);
        }
    }

    private void createConnectionPoolIfNotExists(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        if (pooledDataSources.get(outputTransportAdaptorConfiguration) == null) {
            Map<String, String> props = outputTransportAdaptorConfiguration.getOutputProperties();
            try {
                PooledDataSource dataSource = new PooledDataSource(MysqlTransportAdaptorConstants.DRIVER_CLASS_NAME,
                        props.get(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_HOSTNAME) + ":" + props.get(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_PORT),
                        props.get(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_USER_NAME),
                        props.get(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_PASSWORD));
                pooledDataSources.put(outputTransportAdaptorConfiguration, dataSource);
            } catch (ClassNotFoundException e) {
                log.error("Cannot find the mysql driver class", e);
            } catch (IllegalAccessException e) {
                log.error("Unable to access the DBMS", e);
            } catch (InstantiationException e) {
                log.error("Unable to load the mysql driver", e);
            }
        }
    }

    private void cleanupConnections(Statement stmt, Connection con) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("unable to close statement", e);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("unable to close connection", e);
            }
        }
    }


    private TableInfo initializeTableInfo(String databaseName, String tableName, Object message, PooledDataSource dataSource) throws SQLException {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName(tableName);

        Connection con = null;
        Statement stmt = null;

        try {
            // create the table.
            StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            stringBuilder.append(databaseName + "." + tableName);
            stringBuilder.append(" (");
            boolean appendComma = false;
            for (Map.Entry<String, Object> entry : (((Map<String, Object>) message).entrySet())) {
                if (appendComma) {
                    stringBuilder.append(",");
                } else {
                    appendComma = true;
                }
                stringBuilder.append(entry.getKey()).append("  ");
                if (entry.getValue() instanceof Integer) {
                    stringBuilder.append("INT");
                } else if (entry.getValue() instanceof Long) {
                    stringBuilder.append("BIGINT");
                } else if (entry.getValue() instanceof Float) {
                    stringBuilder.append("FLOAT");
                } else if (entry.getValue() instanceof Double) {
                    stringBuilder.append("DOUBLE");
                } else if (entry.getValue() instanceof String) {
                    stringBuilder.append("VARCHAR(255)");
                } else if (entry.getValue() instanceof Boolean) {
                    stringBuilder.append("BOOL");
                }
            }
            stringBuilder.append(")");

            con = dataSource.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(stringBuilder.toString());

            ArrayList<Attribute> tableColumnList = new ArrayList<Attribute>();
            stringBuilder = new StringBuilder("INSERT INTO ");
            stringBuilder.append(databaseName + "." + tableName);
            stringBuilder.append(" ( ");

            appendComma = false;
            DatabaseMetaData databaseMetaData = con.getMetaData();
            ResultSet rs = databaseMetaData.getColumns(databaseName, null, tableName, null);
            while (rs.next()) {
                AttributeType type = null;
                int colType = rs.getInt("DATA_TYPE");
                switch (colType) {
                    case Types.VARCHAR:
                        type = AttributeType.STRING;
                        break;
                    case Types.INTEGER:
                        type = AttributeType.INT;
                        break;
                    case Types.BIGINT:
                        type = AttributeType.LONG;
                        break;
                    case Types.DOUBLE:
                        type = AttributeType.DOUBLE;
                        break;
                    case Types.FLOAT:
                        type = AttributeType.FLOAT;
                        break;
                    case Types.BOOLEAN:
                        type = AttributeType.BOOL;
                        break;

                }
                Attribute attribute = new Attribute(rs.getString("COLUMN_NAME"), type);
                tableColumnList.add(attribute);

                if (appendComma) {
                    stringBuilder.append(",");
                } else {
                    appendComma = true;
                }
                stringBuilder.append(attribute.getName());

            }
            stringBuilder.append(") VALUES (");
            tableInfo.setInsertStatementPrefix(stringBuilder.toString());
            tableInfo.setColumnOrder(tableColumnList);
            return tableInfo;
        } catch (SQLException e) {
            log.error("error while initializing the table", e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to find the mysql driver class", e);
        } finally {
            cleanupConnections(stmt, con);
        }
        return null;
    }

    @Override
    public void testConnection(OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        // no test
    }


}

