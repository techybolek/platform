/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.siddhi.core.table;


import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RDBMSEventTable implements EventTable {

    static final Logger log = Logger.getLogger(InMemoryEventTable.class);

    private TableDefinition tableDefinition;
    private QueryEventSource queryEventSource;
    // attribute list used for accessing the table.
    private List<Attribute> attributeList;

    private SiddhiDataSource dataSource;
    private String databaseName;
    private String tableName;
    private String fullTableName;  // schema.tableName
    private String tableColumnList;   // for insertion queries.

    private boolean isInitialized;

    public RDBMSEventTable(TableDefinition tableDefinition, SiddhiContext siddhiContext) {
        this.tableDefinition = tableDefinition;
        this.queryEventSource = new QueryEventSource(tableDefinition.getExternalTable().getTableName(), tableDefinition.getTableId(), tableDefinition, null, null, null);
        this.dataSource = siddhiContext.getSiddhiDataSource(tableDefinition.getExternalTable().getTableType(), tableDefinition.getExternalTable().getDataSourceName());
        this.attributeList = new ArrayList<Attribute>();
        try {
            initializeConnection();
        } catch (Exception e) {
            log.error("Unable to connect to the database: " + tableDefinition.getExternalTable().getDatabaseName(), e);
        }
    }


    private void initializeConnection() throws SQLException, ClassNotFoundException {
        if (!isInitialized) {
            Connection con = null;
            Statement statement = null;
            try {
                databaseName = tableDefinition.getExternalTable().getDatabaseName();
                tableName = tableDefinition.getExternalTable().getTableName();
                fullTableName = databaseName + "." + tableName;

                con = dataSource.getConnection();
                statement = con.createStatement();

                // database creation.
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + databaseName);

                // table creation.
                StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                stringBuilder.append(fullTableName);
                stringBuilder.append(" (");
                boolean appendComma = false;
                for (Attribute column : tableDefinition.getAttributeList()) {
                    if (appendComma) {
                        stringBuilder.append(", ");
                    } else {
                        appendComma = true;
                    }
                    stringBuilder.append(column.getName());
                    stringBuilder.append("  ");
                    switch (column.getType()) {
                        case INT:
                            stringBuilder.append("INT");
                            break;
                        case LONG:
                            stringBuilder.append("BIGINT");
                            break;
                        case FLOAT:
                            stringBuilder.append("DECIMAL(30,10)");
                            break;
                        case DOUBLE:
                            stringBuilder.append("DECIMAL(40,15)");
                            break;
                        case BOOL:
                            stringBuilder.append("BOOL");
                            break;
                        default:
                            stringBuilder.append("VARCHAR(255)");
                            break;
                    }
                }
                stringBuilder.append(");");
                statement.execute(stringBuilder.toString());

                StringBuilder builder = new StringBuilder("(");
                appendComma = false;
                for (Attribute att : tableDefinition.getAttributeList()) {
                    attributeList.add(att);
                    if (appendComma) {
                        builder.append(",");
                    }
                    builder.append(att.getName());
                    appendComma = true;
                }
                builder.append(")");
                tableColumnList = builder.toString();

                isInitialized = true;
            } finally {
                cleanUpConnections(statement, con);
            }
        }
    }


    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void add(StreamEvent streamEvent) {
        try {
            initializeConnection();
            if (streamEvent instanceof AtomicEvent) {
                insertToTable((Event) streamEvent);
            } else {
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    insertToTable(((ListEvent) streamEvent).getEvent(i));
                }
            }
        } catch (Exception ex) {
            log.error("Unable to insert the event " + streamEvent.toString() + " to table: " + tableDefinition.getExternalTable().getTableName(), ex);
        }
    }

    private void insertToTable(Event event) throws SQLException, ClassNotFoundException {
        Connection con = null;
        Statement statement = null;

        try {
            StringBuilder builder = new StringBuilder("INSERT INTO ");
            builder.append(fullTableName);
            builder.append(tableColumnList);

            builder.append(" VALUES (");
            for (int i = 0; i < attributeList.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }

                switch (attributeList.get(i).getType()) {
                    case STRING:
                    case TYPE:
                        builder.append("'");
                        builder.append(event.getData(i).toString());
                        builder.append("'");
                        break;
                    default:
                        builder.append(event.getData(i).toString());
                        break;

                }
            }
            builder.append(")");

            con = dataSource.getConnection();
            statement = con.createStatement();
            statement.executeUpdate(builder.toString());
        } finally {
            cleanUpConnections(statement, con);
        }
    }

    @Override
    public void delete(StreamEvent streamEvent, ConditionExecutor conditionExecutor) {
        Statement statement = null;
        Connection con = null;
        try {
            initializeConnection();
            con = dataSource.getConnection();
            statement = con.createStatement();
            StringBuilder statementBuilder = new StringBuilder("DELETE FROM ");
            statementBuilder.append(fullTableName);
            statementBuilder.append(" WHERE ");
            if (streamEvent instanceof AtomicEvent) {
                statementBuilder.append(conditionExecutor.constructSQLPredicate((Event) streamEvent, tableDefinition));
            } else {
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    if (i > 0) {
                        statementBuilder.append(" OR ");
                    }
                    statementBuilder.append("(");
                    statementBuilder.append(conditionExecutor.constructSQLPredicate((Event) ((ListEvent) streamEvent).getEvent(i), tableDefinition));
                    statementBuilder.append(")");
                }
            }
            statement.execute(statementBuilder.toString());
        } catch (SQLException e) {
            log.error("Unable to execute deletion.", e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to load the database driver.", e);
        } finally {
            cleanUpConnections(statement, con);
        }

    }

    @Override
    public void update(StreamEvent streamEvent, ConditionExecutor conditionExecutor, int[] attributeUpdateMappingPosition) {
        Connection con = null;
        Statement statement = null;

        try {
            initializeConnection();
            con = dataSource.getConnection();
            statement = con.createStatement();

            if (streamEvent instanceof AtomicEvent) {
                statement.executeUpdate(createUpdateQuery((Event) streamEvent, conditionExecutor, attributeUpdateMappingPosition));
            } else {
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    statement.addBatch(createUpdateQuery(((ListEvent) streamEvent).getEvent(i), conditionExecutor, attributeUpdateMappingPosition));
                }
                statement.executeBatch();
            }
        } catch (SQLException e) {
            log.error("Unable to execute update on " + streamEvent, e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to load the database driver for " + tableDefinition.getExternalTable().getTableName(), e);
        } finally {
            cleanUpConnections(statement, con);
        }

    }

    @Override
    public boolean contains(AtomicEvent atomicEvent, ConditionExecutor conditionExecutor) {
        Connection con = null;
        Statement statement = null;
        try {
            String predicate = conditionExecutor.constructSQLPredicate(atomicEvent, tableDefinition);
            con = dataSource.getConnection();
            statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + fullTableName + " WHERE " + predicate + " LIMIT 0,1");
            resultSet.setFetchSize(1);
            boolean contains = resultSet.next();
            resultSet.close();
            return contains;
        } catch (SQLException e) {
            log.error("Can't read the database table: " + tableDefinition.getExternalTable().getTableName(), e);
        } catch (ClassNotFoundException e) {
            log.error("Can't load the driver class", e);
        } finally {
            cleanUpConnections(statement, con);
        }
        return false;
    }

    private String createUpdateQuery(Event atomicEvent, ConditionExecutor conditionExecutor, int[] attributeMappingPositions) {
        StringBuilder statementBuilder = new StringBuilder("UPDATE ");
        statementBuilder.append(fullTableName);
        statementBuilder.append(" SET ");

        for (int i = 0; i < attributeList.size(); i++) {
            if (i > 0) {
                statementBuilder.append(", ");
            }
            statementBuilder.append(attributeList.get(i).getName());
            statementBuilder.append(" = '");
            statementBuilder.append(atomicEvent.getData(i));
            statementBuilder.append("'");
        }
        statementBuilder.append(" WHERE ");
        if (conditionExecutor != null) {
            statementBuilder.append(conditionExecutor.constructSQLPredicate(atomicEvent, tableDefinition));
        } else {
            boolean isStringType = false;
            for (int i = 0; i < attributeMappingPositions.length; i++) {
                if (i > 0) {
                    statementBuilder.append(" AND ");
                }
                isStringType = attributeList.get(attributeMappingPositions[i]).getType() == Attribute.Type.STRING;
                statementBuilder.append(attributeList.get(attributeMappingPositions[i]).getName());
                statementBuilder.append(" = ");
                if (isStringType) {
                    statementBuilder.append("'");
                }
                statementBuilder.append(atomicEvent.getData(attributeMappingPositions[i]));
                if (isStringType) {
                    statementBuilder.append("'");
                }
            }
        }
        return statementBuilder.toString();
    }

    @Override
    public QueryEventSource getQueryEventSource() {
        return queryEventSource;
    }

    @Override
    public Iterator<StreamEvent> iterator(String SQLPredicate) {
        Connection con = null;
        Statement statement = null;
        try {
            con = dataSource.getConnection();
            statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + fullTableName +
                    ((SQLPredicate == null) ? "" : (" WHERE " + SQLPredicate)));
            resultSet.setFetchSize(10000);
            ArrayList<StreamEvent> eventList = new ArrayList<StreamEvent>();
            long timestamp = System.currentTimeMillis();
            while (resultSet.next()) {
                Object[] data = new Object[attributeList.size()];
                for (int i = 0; i < attributeList.size(); i++) {
                    switch (attributeList.get(i).getType()) {
                        case BOOL:
                            data[i] = resultSet.getBoolean(attributeList.get(i).getName());
                            break;
                        case DOUBLE:
                            data[i] = resultSet.getDouble(attributeList.get(i).getName());
                            break;
                        case FLOAT:
                            data[i] = resultSet.getFloat(attributeList.get(i).getName());
                            break;
                        case INT:
                            data[i] = resultSet.getInt(attributeList.get(i).getName());
                            break;
                        case LONG:
                            data[i] = resultSet.getLong(attributeList.get(i).getName());
                            break;
                        case STRING:
                            data[i] = resultSet.getString(attributeList.get(i).getName());
                            break;
                        default:
                            data[i] = resultSet.getObject(attributeList.get(i).getName());

                    }
                }
                Event event = new InEvent(tableDefinition.getExternalTable().getTableName(), timestamp, data);
                eventList.add(event);
            }
            resultSet.close();
            return eventList.iterator();
        } catch (SQLException e) {
            log.error("Unable to read the table: " + tableDefinition.getExternalTable().getTableName(), e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to load the database driver class", e);
        } finally {
            cleanUpConnections(statement, con);
        }
        return null;
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return iterator(null);
    }

    private void cleanUpConnections(Statement stmt, Connection con) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("unable to release statement", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("unable to release connection", e);
            }
        }
    }
}
