/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.builder.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.event.builder.core.internal.config.TupleEventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.config.TupleEventBuilderConfigBuilder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConfigurationSyntax;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderServiceValueHolder;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

/**
 * Deploy event builders as axis2 service
 */
public class EventBuilderDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(EventBuilderDeployer.class);
    private ConfigurationContext configurationContext;

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;

    }

    /**
     * Process the event builder configuration file, create it and deploy it
     *
     * @param deploymentFileData information about the transport builder
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();
        File eventBuilderConfigFile = new File(path);
        CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
        int tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        String eventBuilderName = null;

        try {

            OMElement ebConfigOMElement = getEbConfigOMElement(path, eventBuilderConfigFile);

            EventBuilder eventBuilder = TupleEventBuilderConfigBuilder.getInstance().fromOM(ebConfigOMElement);
            eventBuilderName = ebConfigOMElement.getAttributeValue(new QName(EventBuilderConfigurationSyntax.EB_ATTR_NAME));

            if (carbonEventBuilderService.checkEventBuilderValidity(tenantID, eventBuilderName)) {
                carbonEventBuilderService.removeFromCancelDeployMap(tenantID, eventBuilderName);
                carbonEventBuilderService.addEventBuilder(eventBuilder, configurationContext.getAxisConfiguration());
                carbonEventBuilderService.addFileConfiguration(tenantID, eventBuilderName, path, true);
                log.info(eventBuilderName + " successfully deployed.");
            } else {
                throw new EventBuilderConfigurationException(eventBuilderName + " is already registered for this tenant");
            }

        } catch (Throwable t) {
            carbonEventBuilderService.addFileConfiguration(tenantID, eventBuilderName, path, false);
            log.error("Invalid configuration at " + eventBuilderConfigFile.getName(), t);
            throw new DeploymentException(t);
        }


    }

    private OMElement getEbConfigOMElement(String path,
                                           File ebConfigFile) throws DeploymentException {
        OMElement ebConfigElement = null;
        BufferedInputStream inputStream = null;
        try {

            inputStream = new BufferedInputStream(new FileInputStream(ebConfigFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            ebConfigElement = builder.getDocumentElement();
            ebConfigElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " .xml file cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + ebConfigFile.getName() + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Cannot close the input stream";
                log.error(errorMessage, e);
            }
        }

        return ebConfigElement;
    }

    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed event builder configuration file
     *
     * @param filePath the path to the EventBuilderConfiguration file to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void undeploy(String filePath) throws DeploymentException {
        int tenantID = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();

        CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
        carbonEventBuilderService.removeFromCancelUnDeployMap(tenantID, filePath);
        carbonEventBuilderService.removeEventBuilderConfiguration(filePath, tenantID);
    }

    public void setDirectory(String directory) {

    }
}


