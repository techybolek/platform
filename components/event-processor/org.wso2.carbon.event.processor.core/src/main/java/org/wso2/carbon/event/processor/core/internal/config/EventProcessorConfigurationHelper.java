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
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.StreamConfiguration;
import org.wso2.carbon.event.processor.core.internal.util.EventProcessorConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;


public class EventProcessorConfigurationHelper {

    public static ExecutionPlanConfiguration fromOM(OMElement queryPlanConfigElement) {
        ExecutionPlanConfiguration queryPlanConfig = new ExecutionPlanConfiguration();
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
                String verson = importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_VERSION));
                StreamConfiguration streamConfiguration;
                if (verson != null) {
                    streamConfiguration = new StreamConfiguration(importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)), verson);
                } else {
                    streamConfiguration = new StreamConfiguration(importedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)));
                }
                OMAttribute as = importedStream.getAttribute(new QName(EventProcessorConstants.EP_ATTR_AS));
                if (as != null && as.getAttributeValue() != null && as.getAttributeValue().trim().length() > 0) {
                    streamConfiguration.setSiddhiStreamName(as.getAttributeValue());
                }
                queryPlanConfig.addImportedStream(streamConfiguration); // todo validate
            }
        }

        Iterator allExportedStreamsIterator = queryPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_EXP_STREAMS));
        while (allExportedStreamsIterator.hasNext()) {
            Iterator exportedStreamIterator = ((OMElement) allExportedStreamsIterator.next()).getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_STREAM));
            while (exportedStreamIterator.hasNext()) {
                OMElement exportedStream = (OMElement) exportedStreamIterator.next();
                StreamConfiguration streamConfiguration = new StreamConfiguration(exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_NAME)), exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_AS)), exportedStream.getAttributeValue(new QName(EventProcessorConstants.EP_ATTR_VERSION)));
                queryPlanConfig.addExportedStream(streamConfiguration);
                OMAttribute valueOf = exportedStream.getAttribute(new QName(EventProcessorConstants.EP_ATTR_VALUEOF));
                if (valueOf != null && valueOf.getAttributeValue() != null && valueOf.getAttributeValue().trim().length() > 0) {
                    streamConfiguration.setSiddhiStreamName(valueOf.getAttributeValue());
                }
            }
        }

        Iterator queryIterator = queryPlanConfigElement.getChildrenWithName(new QName(EventProcessorConstants.EP_CONF_NS, EventProcessorConstants.EP_ELE_QUERIES));
        if (queryIterator.hasNext()) {
            queryPlanConfig.setQueryExpressions(((OMElement) queryIterator.next()).getText());
        }
        return queryPlanConfig;
    }


    public static OMElement toOM(ExecutionPlanConfiguration queryPlanConfig) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement queryPlan = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_ROOT_ELEMENT));
        queryPlan.declareDefaultNamespace(EventProcessorConstants.EP_CONF_NS);
        queryPlan.addAttribute(EventProcessorConstants.EP_ATTR_NAME, queryPlanConfig.getName(), null);

        OMElement description = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_DESC));
        description.setNamespace(queryPlan.getDefaultNamespace());
        description.setText(queryPlanConfig.getDescription());
        queryPlan.addChild(description);

        OMElement siddhiConfiguration = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_SIDDHI_CONFIG));
        siddhiConfiguration.setNamespace(queryPlan.getDefaultNamespace());
        for (Map.Entry<String, String> entry : queryPlanConfig.getSiddhiConfigurationProperties().entrySet()) {
            OMElement propertyElement = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_PROPERTY));
            propertyElement.setNamespace(queryPlan.getDefaultNamespace());
            propertyElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, entry.getKey(), null);
            propertyElement.setText(entry.getValue());
            siddhiConfiguration.addChild(propertyElement);
        }
        queryPlan.addChild(siddhiConfiguration);

        OMElement importedStreams = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_IMP_STREAMS));
        importedStreams.setNamespace(queryPlan.getDefaultNamespace());
        for (StreamConfiguration stream : queryPlanConfig.getImportedStreams()) {
            OMElement streamElement = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_STREAM));
            streamElement.setNamespace(queryPlan.getDefaultNamespace());
            streamElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, stream.getName(), null);
            if (stream.getSiddhiStreamName() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_AS, stream.getSiddhiStreamName(), null);
            }
            if (stream.getVersion() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_VERSION, stream.getVersion(), null);
            }
            importedStreams.addChild(streamElement);
        }
        queryPlan.addChild(importedStreams);

        OMElement queries = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_QUERIES));
        queries.setNamespace(queryPlan.getDefaultNamespace());
        queries.setText(queryPlanConfig.getQueryExpressions());
        queryPlan.addChild(queries);

        OMElement exportedStreams = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_EXP_STREAMS));
        exportedStreams.setNamespace(queryPlan.getDefaultNamespace());
        for (StreamConfiguration stream : queryPlanConfig.getExportedStreams()) {
            OMElement streamElement = factory.createOMElement(new QName(EventProcessorConstants.EP_ELE_STREAM));
            streamElement.setNamespace(queryPlan.getDefaultNamespace());
            streamElement.addAttribute(EventProcessorConstants.EP_ATTR_NAME, stream.getName(), null);
            if (stream.getSiddhiStreamName() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_VALUEOF, stream.getSiddhiStreamName(), null);
            }
            if (stream.getVersion() != null) {
                streamElement.addAttribute(EventProcessorConstants.EP_ATTR_VERSION, stream.getVersion(), null);
            }
            exportedStreams.addChild(streamElement);
        }
        queryPlan.addChild(exportedStreams);
        return queryPlan;
    }

    public static boolean validateExecutionPlanConfiguration(OMElement om) {
        //todo
        if (!om.getQName().getLocalPart().equals(EventProcessorConstants.EP_ELE_ROOT_ELEMENT)) {
            return false;
        }
        return true;
    }


    public static boolean validateExecutionPlanConfiguration(
            ExecutionPlanConfiguration executionPlanConfiguration) {
        if (executionPlanConfiguration.getName() == null) {
            return false;
        }
        if (executionPlanConfiguration.getExportedStreams() == null || executionPlanConfiguration.getExportedStreams().size() == 0) {
            return false;
        }

        if (executionPlanConfiguration.getImportedStreams() == null || executionPlanConfiguration.getImportedStreams().size() == 0) {
            return false;
        }
        return true; // todo
    }
}
