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

package org.wso2.carbon.transport.adaptor.cassandra;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.transport.adaptor.cassandra.internal.util.CassandraTransportAdaptorConstants;
import org.wso2.carbon.transport.adaptor.core.AbstractTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.OutputTransportAdaptor;
import org.wso2.carbon.transport.adaptor.core.Property;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorDto;
import org.wso2.carbon.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorEventProcessingException;
import org.wso2.carbon.transport.adaptor.core.message.config.OutputTransportMessageConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CassandraTransportAdaptorType extends AbstractTransportAdaptor
        implements OutputTransportAdaptor {

    private static final Log log = LogFactory.getLog(CassandraTransportAdaptorType.class);
    private StringSerializer sser = new StringSerializer();
    private static CassandraTransportAdaptorType cassandraTransportAdaptor = new CassandraTransportAdaptorType();
    private ResourceBundle resourceBundle;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, TransportAdaptorInfo>> tenantedCassandraClusterCache = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, TransportAdaptorInfo>>();

    private CassandraTransportAdaptorType() {

    }

    @Override
    protected TransportAdaptorDto.TransportAdaptorType getTransportAdaptorType() {
        return TransportAdaptorDto.TransportAdaptorType.OUT;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedInputMessageTypes() {
        return null;
    }

    @Override
    protected List<TransportAdaptorDto.MessageType> getSupportedOutputMessageTypes() {
        List<TransportAdaptorDto.MessageType> supportOutputMessageTypes = new ArrayList<TransportAdaptorDto.MessageType>();
        supportOutputMessageTypes.add(TransportAdaptorDto.MessageType.MAP);
        return supportOutputMessageTypes;
    }

    /**
     * @return cassandra transport adaptor instance
     */
    public static org.wso2.carbon.transport.adaptor.cassandra.CassandraTransportAdaptorType getInstance() {

        return cassandraTransportAdaptor;
    }

    /**
     * @return name of the cassandra transport adaptor
     */
    @Override
    protected String getName() {
        return CassandraTransportAdaptorConstants.TRANSPORT_TYPE_CASSANDRA;
    }

    /**
     * Initialises the resource bundle
     */
    @Override
    protected void init() {
        //To change body of implemented methods use File | Settings | File Templates.
        resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.transport.adaptor.cassandra.i18n.Resources", Locale.getDefault());
    }

    /**
     * @return common adaptor configuration property list
     */
    @Override
    protected List<Property> getCommonAdaptorConfig() {


        List<Property> propertyList = new ArrayList<Property>();


        // set cluster name
        Property clusterName = new Property(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_CLUSTER_NAME);
        clusterName.setDisplayName(
                resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_CLUSTER_NAME));
        clusterName.setRequired(true);
        clusterName.setHint(resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_CLUSTER_NAME_HINT));
        propertyList.add(clusterName);

        // set host name
        Property hostName = new Property(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_HOSTNAME);
        hostName.setDisplayName(
                resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_HOSTNAME));
        hostName.setRequired(true);
        propertyList.add(hostName);


        // set port
        Property port = new Property(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_PORT);
        port.setDisplayName(
                resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_PORT));
        port.setRequired(true);
        propertyList.add(port);


        // set user name
        Property userName = new Property(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_USER_NAME);
        userName.setDisplayName(
                resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_USER_NAME));
        userName.setRequired(true);
        propertyList.add(userName);


        // set password
        Property password = new Property(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_PASSWORD);
        password.setDisplayName(
                resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_PASSWORD));
        password.setRequired(true);
        password.setSecured(true);
        propertyList.add(password);


        return propertyList;
    }

    /**
     * @return output adaptor configuration property list
     */
    @Override
    public List<Property> getOutputAdaptorProperties() {

        List<Property> propertyList = new ArrayList<Property>();


        // set index all columns
        Property indexAllColumns = new Property(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_INDEX_ALL_COLUMNS);
        indexAllColumns.setDisplayName(
                resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_INDEX_ALL_COLUMNS));
        indexAllColumns.setOptions(new String[]{"true", "false"});
        indexAllColumns.setDefaultValue("false");
        indexAllColumns.setHint(resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_INDEX_ALL_COLUMNS_HINT));
        propertyList.add(indexAllColumns);

        return propertyList;

    }

    /**
     * @return output message configuration property list
     */
    @Override
    public List<Property> getOutputMessageProperties() {

        List<Property> propertyList = new ArrayList<Property>();

        // key space
        Property keySpace = new Property(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_KEY_SPACE_NAME);
        keySpace.setDisplayName(
                resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_KEY_SPACE_NAME));
        keySpace.setRequired(true);
        propertyList.add(keySpace);

        // column family
        Property columnFamily = new Property(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_COLUMN_FAMILY_NAME);
        columnFamily.setDisplayName(
                resourceBundle.getString(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_COLUMN_FAMILY_NAME));
        columnFamily.setRequired(true);
        propertyList.add(columnFamily);

        return propertyList;
    }

    /**
     * @param outputTransportMessageConfiguration
     *                - topic name to publish messages
     * @param message - is and Object[]{Event, EventDefinition}
     * @param outputTransportAdaptorConfiguration
     *                - transport configuration to be used
     */
    public void publish(OutputTransportMessageConfiguration outputTransportMessageConfiguration,
                        Object message,
                        OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        Integer tenantId = CarbonContext.getCurrentContext().getTenantId();
        if (message instanceof Map) {

            ConcurrentHashMap<String, TransportAdaptorInfo> cassandraClusterCache = tenantedCassandraClusterCache.get(tenantId);
            if (null == cassandraClusterCache) {
                cassandraClusterCache = new ConcurrentHashMap<String, TransportAdaptorInfo>();
                if (null != tenantedCassandraClusterCache.putIfAbsent(tenantId, cassandraClusterCache)) {
                    cassandraClusterCache = tenantedCassandraClusterCache.get(tenantId);
                }
            }

            TransportAdaptorInfo transportAdaptorInfo = cassandraClusterCache.get(outputTransportAdaptorConfiguration.getName());
            if (null == transportAdaptorInfo) {
                Map<String, String> properties = outputTransportAdaptorConfiguration.getCommonProperties();

                Map<String, String> credentials = new HashMap<String, String>();
                credentials.put("username", properties.get(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_USER_NAME));
                credentials.put("password", properties.get(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_PASSWORD));

                Cluster cluster = HFactory.createCluster(properties.get(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_CLUSTER_NAME),
                                                         new CassandraHostConfigurator(
                                                                 properties.get(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_HOSTNAME) + ":" +
                                                                 properties.get(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_PORT)), credentials);

                String indexAllColumnsString = properties.get(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_INDEX_ALL_COLUMNS);
                boolean indexAllColumns = false;
                if (indexAllColumnsString != null && indexAllColumnsString.equals("true")) {
                    indexAllColumns = true;
                }
                transportAdaptorInfo = new TransportAdaptorInfo(cluster, indexAllColumns);
                if (null != cassandraClusterCache.putIfAbsent(outputTransportAdaptorConfiguration.getName(), transportAdaptorInfo)) {
                    transportAdaptorInfo = cassandraClusterCache.get(outputTransportAdaptorConfiguration.getName());
                } else {
                    log.info("Initiated Cassandra Writer " + outputTransportAdaptorConfiguration.getName());
                }
            }


            String keySpaceName = outputTransportMessageConfiguration.getOutputMessageProperties().get(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_KEY_SPACE_NAME);
            String columnFamilyName = outputTransportMessageConfiguration.getOutputMessageProperties().get(CassandraTransportAdaptorConstants.TRANSPORT_CASSANDRA_COLUMN_FAMILY_NAME);
            MessageInfo messageInfo = transportAdaptorInfo.getMessageInfoMap().get(outputTransportMessageConfiguration);
            if (null == messageInfo) {
                Keyspace keyspace = HFactory.createKeyspace(keySpaceName, transportAdaptorInfo.getCluster());
                messageInfo = new MessageInfo(keyspace);
                if (null != transportAdaptorInfo.getMessageInfoMap().putIfAbsent(outputTransportMessageConfiguration, messageInfo)) {
                    messageInfo = transportAdaptorInfo.getMessageInfoMap().get(outputTransportMessageConfiguration);
                }
            }

            if (transportAdaptorInfo.getCluster().describeKeyspace(keySpaceName) == null) {
                BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
                columnFamilyDefinition.setKeyspaceName(keySpaceName);
                columnFamilyDefinition.setName(columnFamilyName);
                columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
                columnFamilyDefinition
                        .setDefaultValidationClass(ComparatorType.UTF8TYPE
                                                           .getClassName());
                columnFamilyDefinition
                        .setKeyValidationClass(ComparatorType.UTF8TYPE
                                                       .getClassName());

                ColumnFamilyDefinition cfDef = new ThriftCfDef(
                        columnFamilyDefinition);

                KeyspaceDefinition keyspaceDefinition = HFactory
                        .createKeyspaceDefinition(keySpaceName,
                                                  "org.apache.cassandra.locator.SimpleStrategy", 1,
                                                  Arrays.asList(cfDef));
                transportAdaptorInfo.getCluster().addKeyspace(keyspaceDefinition);
                KeyspaceDefinition fromCluster = transportAdaptorInfo.getCluster().describeKeyspace(keySpaceName);
                messageInfo.setColumnFamilyDefinition(new BasicColumnFamilyDefinition(fromCluster.getCfDefs().get(0)));
            } else {
                KeyspaceDefinition fromCluster = transportAdaptorInfo.getCluster().describeKeyspace(keySpaceName);
                for (ColumnFamilyDefinition columnFamilyDefinition : fromCluster.getCfDefs()) {
                    if (columnFamilyDefinition.getName().equals(columnFamilyName)) {
                        messageInfo.setColumnFamilyDefinition(new BasicColumnFamilyDefinition(columnFamilyDefinition));
                        break;
                    }
                }
            }


            Mutator<String> mutator = HFactory.createMutator(messageInfo.getKeyspace(), sser);
            String uuid = UUID.randomUUID().toString();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) message).entrySet()) {

                if (transportAdaptorInfo.isIndexAllColumns() && !messageInfo.getColumnNames().contains(entry.getKey())) {
                    BasicColumnFamilyDefinition columnFamilyDefinition = messageInfo.getColumnFamilyDefinition();
                    BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
                    columnDefinition.setName(StringSerializer.get().toByteBuffer(
                            entry.getKey()));
                    columnDefinition.setIndexType(ColumnIndexType.KEYS);
                    columnDefinition.setIndexName(keySpaceName + "_"+ columnFamilyName + "_" + entry.getKey() + "_Index");
                    columnDefinition.setValidationClass(ComparatorType.UTF8TYPE
                                                                .getClassName());
                    columnFamilyDefinition.addColumnDefinition(columnDefinition);
                    transportAdaptorInfo.getCluster().updateColumnFamily(new ThriftCfDef(columnFamilyDefinition));
                    messageInfo.getColumnNames().add(entry.getKey());
                }
                mutator.insert(uuid, columnFamilyName, HFactory.createStringColumn(entry.getKey(), entry.getValue().toString()));
            }

            mutator.execute();
        }
    }

    private void publishEvent(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration,
            AsyncDataPublisher dataPublisher,
            Event event, StreamDefinition streamDefinition) {
        try {
            dataPublisher.publish(streamDefinition.getName(), streamDefinition.getVersion(), event);
        } catch (AgentException ex) {
            throw new TransportAdaptorEventProcessingException(
                    "Cannot publish data via DataPublisher for the transport configuration:" +
                    outputTransportAdaptorConfiguration.getName() + " for the  event " + event, ex);
        }

    }

    @Override
    public void testConnection(
            OutputTransportAdaptorConfiguration outputTransportAdaptorConfiguration) {
        // no test
    }

    class MessageInfo {
        private Keyspace keyspace;
        private BasicColumnFamilyDefinition columnFamilyDefinition;
        private List<String> columnNames = new ArrayList<String>();


        MessageInfo(Keyspace keyspace) {
            this.keyspace = keyspace;
        }

        public Keyspace getKeyspace() {
            return keyspace;
        }

        public BasicColumnFamilyDefinition getColumnFamilyDefinition() {
            return columnFamilyDefinition;
        }

        public void setColumnFamilyDefinition(BasicColumnFamilyDefinition columnFamilyDefinition) {
            this.columnFamilyDefinition = columnFamilyDefinition;
        }

        public List<String> getColumnNames() {
            return columnNames;
        }
    }

    class TransportAdaptorInfo {
        private Cluster cluster;
        private boolean indexAllColumns;
        private ConcurrentHashMap<OutputTransportMessageConfiguration, MessageInfo> messageInfoMap = new ConcurrentHashMap<OutputTransportMessageConfiguration, MessageInfo>();

        TransportAdaptorInfo(Cluster cluster, boolean indexAllColumns) {
            this.cluster = cluster;
            this.indexAllColumns = indexAllColumns;
        }

        public Cluster getCluster() {
            return cluster;
        }

        public ConcurrentHashMap<OutputTransportMessageConfiguration, MessageInfo> getMessageInfoMap() {
            return messageInfoMap;
        }

        public boolean isIndexAllColumns() {
            return indexAllColumns;
        }
    }
}
