/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.bam.webapp.stat.publisher.publish;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.Property;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.ServiceEventingConfigData;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

//import org.wso2.carbon.bam.webapp.stat.publisher.util.StatisticsType;

public class StreamDefinitionCreatorUtil {

    private static Log log = LogFactory.getLog(StreamDefinitionCreatorUtil.class);

    public static StreamDefinition getStreamDefinition(ServiceEventingConfigData configData) {
        StreamDefinition streamDefForServiceStats;

        StreamDefinition streamDef = null;
        streamDefForServiceStats = streamDefinitionForServiceStats(configData);
        streamDef = streamDefForServiceStats;

        return streamDef;
    }

    private static StreamDefinition streamDefinitionForServiceStats(ServiceEventingConfigData configData) {
        StreamDefinition streamDef = null;
        try {
            streamDef = new StreamDefinition(
                    configData.getStreamName(), configData.getVersion());
            streamDef.setNickName(configData.getNickName());
            streamDef.setDescription(configData.getDescription());

            List<Attribute> metaDataAttributeList = new ArrayList<Attribute>();
            setUserAgentMetadata(metaDataAttributeList);
            setPropertiesAsMetaData(metaDataAttributeList, configData);

            streamDef.setMetaData(metaDataAttributeList);

            List<Attribute> payLoadData = new ArrayList<Attribute>();
            payLoadData = addCommonPayLoadData(payLoadData);
            payLoadData = addServiceStatsPayLoadData(payLoadData);
            streamDef.setPayloadData(payLoadData);

        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream Definition", e);
        }
        return streamDef;
    }

    private static void setPropertiesAsMetaData(List<Attribute> metaDataAttributeList,
                                                ServiceEventingConfigData configData) {
        Property[] properties = configData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaDataAttributeList.add(new Attribute(property.getKey(), AttributeType.STRING));
                }
            }
        }
    }

/*    private static List<Attribute> addOutOnlyPayLoadData(List<Attribute> payLoadData) {
        payLoadData.add(new Attribute(BAMDataPublisherConstants.OUT_MSG_ID,
                                      AttributeType.STRING));
        payLoadData.add(new Attribute(BAMDataPublisherConstants.OUT_MSG_BODY,
                                      AttributeType.STRING));
        return payLoadData;
    }*/

    private static List<Attribute> addCommonPayLoadData(List<Attribute> payLoadData) {

        payLoadData.add(new Attribute("appName",
                AttributeType.STRING));
        payLoadData.add(new Attribute("appOwnerTenant",
                AttributeType.STRING));
        payLoadData.add(new Attribute("appVersion",
                AttributeType.STRING));
        payLoadData.add(new Attribute("userId",
                AttributeType.STRING));
        payLoadData.add(new Attribute("userTenant",
                AttributeType.STRING));
        payLoadData.add(new Attribute("resource",
                AttributeType.STRING));
        payLoadData.add(new Attribute("requestCount",
                AttributeType.INT));
        payLoadData.add(new Attribute("requestTime",
                AttributeType.LONG));


/*
        payLoadData.add(new Attribute(BAMDataPublisherConstants.SERVICE_NAME,
                AttributeType.STRING));
        payLoadData.add(new Attribute(BAMDataPublisherConstants.OPERATION_NAME,
                AttributeType.STRING));
        payLoadData.add(new Attribute(BAMDataPublisherConstants.TIMESTAMP,
                AttributeType.LONG));
*/
        return payLoadData;
    }

    private static List<Attribute> addServiceStatsPayLoadData(List<Attribute> payLoadData) {
/*
        payLoadData.add(new Attribute(WebappStatisticsPublisherConstants.RESPONSE_TIME,
                AttributeType.LONG));
        payLoadData.add(new Attribute(WebappStatisticsPublisherConstants.REQUEST_COUNT,
                AttributeType.INT));
        payLoadData.add(new Attribute(WebappStatisticsPublisherConstants.RESPONSE_COUNT,
                AttributeType.INT));
        payLoadData.add(new Attribute(WebappStatisticsPublisherConstants.FAULT_COUNT,
                AttributeType.INT));
*/
        return payLoadData;
    }

    private static void setUserAgentMetadata(List<Attribute> attributeList) {
        attributeList.add(new Attribute("clientType",
                AttributeType.STRING));

/*
        attributeList.add(new Attribute(BAMDataPublisherConstants.REQUEST_URL,
                AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.REMOTE_ADDRESS,
                AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.CONTENT_TYPE,
                AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.USER_AGENT,
                AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.HOST,
                AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.REFERER,
                AttributeType.STRING));
*/
    }
}
