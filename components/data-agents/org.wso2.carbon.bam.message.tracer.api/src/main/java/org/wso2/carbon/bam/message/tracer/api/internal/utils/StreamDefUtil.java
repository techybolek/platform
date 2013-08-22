/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.message.tracer.api.internal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.message.tracer.api.internal.conf.EventStreamDef;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

public class StreamDefUtil {

    private static Log log = LogFactory.getLog(StreamDefUtil.class);

    public static StreamDefinition getStreamDefinition() {

        EventStreamDef configStreamDef = new EventStreamDef();
        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(configStreamDef.getStreamName(),
                                                    configStreamDef.getVersion());
            streamDefinition.setNickName(configStreamDef.getNickName());
            streamDefinition.setDescription(configStreamDef.getDescription());

            List<Attribute> metaDataAttributeList = getMetaDataDef();
            streamDefinition.setMetaData(metaDataAttributeList);

            List<Attribute> payLoadData = getPayLoadDataDef();
            streamDefinition.setPayloadData(payLoadData);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Unable to create StreamDefinition : " + e.getErrorMessage(), e);
        }

        return streamDefinition;
    }

    private static List<Attribute> getPayLoadDataDef() {

        List<Attribute> payLoadList = new ArrayList<Attribute>();
        payLoadList.add(new Attribute("payload", AttributeType.STRING));
        payLoadList.add(new Attribute("type", AttributeType.STRING));
        payLoadList.add(new Attribute("timestamp", AttributeType.STRING));

        return payLoadList;
    }

    private static List<Attribute> getMetaDataDef() {

        List<Attribute> metaDataList = new ArrayList<Attribute>();
        metaDataList.add(new Attribute("host", AttributeType.STRING));
        return metaDataList;
    }
}
