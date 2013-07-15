/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.core.common;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.core.AnalyticsConstants;
import org.wso2.carbon.analytics.core.exception.AnalyticsConfigurationException;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *Build annotations from annotation-config.xml
 */
public class AnnotationFactory {
    private static final Log log = LogFactory.getLog(AnnotationFactory.class);



    public OMElement loadConfigXML() throws AnalyticsConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + AnalyticsConstants.ANALYTICS_DIR + File.separator + AnalyticsConstants.ANNOTATION_CONFIG_XML;

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = AnalyticsConstants.ANNOTATION_CONFIG_XML
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new AnalyticsConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + AnalyticsConstants.ANNOTATION_CONFIG_XML
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new AnalyticsConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
    }


    public Map populateAnnotations(OMElement config
    ) {

        Map<String, String> annotations = new HashMap<String, String>();

        OMElement annotationElems = config.getFirstChildWithName(
                new QName(AnalyticsConstants.ANALYTICS_NAMESPACE,
                        AnalyticsConstants.ANNOTATIONS__ELEMENT));

        if (annotationElems != null) {
            for (Iterator annotationIterator = annotationElems.getChildElements();
                 annotationIterator.hasNext(); ) {
                OMElement annotation = (OMElement) annotationIterator.next();
                OMElement nameElem = annotation.getFirstChildWithName(new QName(AnalyticsConstants.ANNOTATION_NAME__ELEMENT));
                OMElement classElem = annotation.getFirstChildWithName(new QName(AnalyticsConstants.ANNOTATION_CLASS_ELEMENT));

                annotations.put(nameElem.getText(), classElem.getText());
            }
        }

        return annotations;
    }


}
