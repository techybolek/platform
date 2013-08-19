package org.wso2.carbon.output.transport.adaptor.mysql.test;

import org.junit.Test;
import org.wso2.carbon.output.transport.adaptor.core.config.InternalOutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.config.OutputTransportAdaptorConfiguration;
import org.wso2.carbon.output.transport.adaptor.core.message.config.OutputTransportAdaptorMessageConfiguration;

public class MySqlTestCase {

    @Test
    public void testTableInit() {
//        MysqlTransportAdaptorFactory factory = new MysqlTransportAdaptorFactory();
//        MysqlTransportAdaptorType adaptor = (MysqlTransportAdaptorType) factory.getTransportAdaptor();
//
//        OutputTransportAdaptorConfiguration outputAdaptorConfig = new OutputTransportAdaptorConfiguration();
//        InternalOutputTransportAdaptorConfiguration internalConfig = new InternalOutputTransportAdaptorConfiguration();
//        internalConfig.addTransportAdaptorProperty(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_HOSTNAME, "jdbc:mysql://localhost");
//        internalConfig.addTransportAdaptorProperty(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_PORT, "3306");
//        internalConfig.addTransportAdaptorProperty(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_USER_NAME, "root");
//        internalConfig.addTransportAdaptorProperty(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_PASSWORD, "root");
//        outputAdaptorConfig.setOutputConfiguration(internalConfig);
//
//        OutputTransportAdaptorMessageConfiguration outputMsgConfig = new OutputTransportAdaptorMessageConfiguration();
//        Map<String, String> messageConfig = new Hashtable<String, String>(2);
//        messageConfig.put(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_TABLE_NAME, "testCaseTable");
//        messageConfig.put(MysqlTransportAdaptorConstants.TRANSPORT_MYSQL_DATABASE_NAME, "cepdb");
//        outputMsgConfig.setOutputMessageProperties(messageConfig);
//
//        Map<String, Object> event = new HashMap<String, Object>(4);
//        event.put("name", "user1");
//        event.put("ipAddr", "10.1.1.1");
//        event.put("accessAttempts", 3);
//
//        adaptor.publish(outputMsgConfig, event, outputAdaptorConfig);

    }

}
