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

import org.wso2.carbon.bam.message.tracer.api.data.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventConfigUtil {

    public static List<Object> getCorrelationData(Message message) {

        return new ArrayList<Object>();
    }

    public static List<Object> getMetaData(Message message) {

        List<Object> metaData = new ArrayList<Object>();
        metaData.add(message.getHost());
        return metaData;
    }

    public static List<Object> getEventData(Message message) {

        List<Object> payloadData = new ArrayList<Object>();
        payloadData.add(message.getPayload());
        payloadData.add(message.getActivityId());
        payloadData.add(message.getType());
        payloadData.add(message.getTimestamp());

        return payloadData;
    }

    public static Map<String, String> getArbitraryDataMap(Message message) {
        return message.getAdditionalValues();
    }
}
