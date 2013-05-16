/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.processor.core.internal.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.processor.core.QueryPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;


public class EventProcessorConfigurationHelper {

    public static QueryPlanConfiguration fromOM(OMElement queryPlanConfigElement) {
        QueryPlanConfiguration queryPlanConfig = new QueryPlanConfiguration();
        queryPlanConfig.setName(queryPlanConfigElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)));


        Iterator descIterator = queryPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_DESC));
        if (descIterator.hasNext()) {
            OMElement descriptionElement = (OMElement) descIterator.next();
            queryPlanConfig.setDescription(descriptionElement.getText());
        }

        Iterator siddhiConfigIterator = queryPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_SIDDHI_CONFIG));
        while (siddhiConfigIterator.hasNext()) {
            Iterator siddhiConfigPropertyIterator = ((OMElement) siddhiConfigIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_PROPERTY));
            while (siddhiConfigPropertyIterator.hasNext()) {
                OMElement configPropertyElement = (OMElement) siddhiConfigPropertyIterator.next();
                queryPlanConfig.addSiddhiConfigurationProperty(configPropertyElement.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)), configPropertyElement.getText());
            }
        }

        Iterator allImportedStreamsIterator = queryPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_IMP_STREAMS));
        while (allImportedStreamsIterator.hasNext()) {
            Iterator importedStreamIterator = ((OMElement) allImportedStreamsIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_STREAM));
            while (importedStreamIterator.hasNext()) {
                OMElement importedStream = (OMElement) importedStreamIterator.next();
                queryPlanConfig.addImportedStream(new StreamConfiguration(importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)))); // todo validate
            }
        }


        Iterator allExportedStreamsIterator = queryPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_EXP_STREAMS));
        while (allExportedStreamsIterator.hasNext()) {
            Iterator exportedStreamIterator = ((OMElement) allExportedStreamsIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_STREAM));
            while (exportedStreamIterator.hasNext()) {
                OMElement exportedStream = (OMElement) exportedStreamIterator.next();
                queryPlanConfig.addExportedStream(new StreamConfiguration(exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)), exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_AS)))); // todo validate

            }
        }

        Iterator queryIterator = queryPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_QUERIES));
        if (queryIterator.hasNext()) {
            queryPlanConfig.setQueryExpressions(((OMElement) queryIterator.next()).getText());
        }
        return queryPlanConfig;
    }


    public static OMElement toOM(QueryPlanConfiguration queryPlanConfig) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement queryPlan = factory.createOMElement(new QName(
                EventProcessorConstants.EP_CONF_NS,
                EventProcessorConstants.EP_ELE_ROOT_ELEMENT, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
        queryPlan.addAttribute(EventProcessorConstants.EP_ATTR_NAME, queryPlanConfig.getName(),
                null);

        OMElement description = factory.createOMElement(new QName(EventProcessorConstants.EP_CONF_NS,
                EventProcessorConstants.EP_ELE_DESC, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
        description.setText(queryPlanConfig.getDescription());
        queryPlan.addChild(description);


        OMElement siddhiConfiguration = factory.createOMElement(new QName(EventProcessorConstants.EP_CONF_NS,
                EventProcessorConstants.EP_ELE_SIDDHI_CONFIG, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
        for (Map.Entry<String, String> entry: queryPlanConfig.getSiddhiConfigurationProperties().entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(EventProcessorConstants.EP_CONF_NS,
                    EventProcessorConstants.EP_ELE_PROPERTY, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
            propertyElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, entry.getKey(), null);
            propertyElement.setText(entry.getValue());
            siddhiConfiguration.addChild(propertyElement);
        }
        queryPlan.addChild(siddhiConfiguration);


        OMElement importedStreams = factory.createOMElement(new QName(EventProcessorConstants.EP_CONF_NS,
                EventProcessorConstants.EP_ELE_IMP_STREAMS, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
        for (StreamConfiguration stream: queryPlanConfig.getImportedStreams()) {
            OMElement streamElement = factory.createOMElement(new QName(EventProcessorConstants.EP_CONF_NS,
                    EventProcessorConstants.EP_ELE_STREAM, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
            streamElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, stream.getName(), null);
            importedStreams.addChild(streamElement);
        }
        queryPlan.addChild(importedStreams);


        OMElement queries = factory.createOMElement(new QName(EventProcessorConstants.EP_CONF_NS,
                EventProcessorConstants.EP_ELE_QUERIES, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
        queries.setText(queryPlanConfig.getQueryExpressions());
        queryPlan.addChild(queries);


        OMElement exportedStreams = factory.createOMElement(new QName(EventProcessorConstants.EP_CONF_NS,
                EventProcessorConstants.EP_ELE_EXP_STREAMS, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
        for (StreamConfiguration stream: queryPlanConfig.getExportedStreams()) {
            OMElement streamElement = factory.createOMElement(new QName(EventProcessorConstants.EP_CONF_NS,
                    EventProcessorConstants.EP_ELE_STREAM, EventProcessorConstants.EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX));
            streamElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, stream.getName(), null);
            if (stream.getAs() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_AS, stream.getAs(), null);
            }
            exportedStreams.addChild(streamElement);
        }
        queryPlan.addChild(exportedStreams);

        return queryPlan;
    }

    public static boolean validateQueryPlanConfiguration(QueryPlanConfiguration queryPlanConfiguration) {
        return true; // todo
    }
}
