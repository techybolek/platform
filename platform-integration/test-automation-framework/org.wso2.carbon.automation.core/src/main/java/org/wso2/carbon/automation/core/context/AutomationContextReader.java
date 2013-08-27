/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.core.context;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.context.configurationcontext.ConfigurationContextFactory;
import org.wso2.carbon.automation.core.context.databasecontext.DatabaseContextFactory;
import org.wso2.carbon.automation.core.context.featuremanagementContext.FeatureManagementContextFactory;
import org.wso2.carbon.automation.core.context.platformcontext.PlatformContextFactory;
import org.wso2.carbon.automation.core.context.securitycontext.SecurityContextFactory;
import org.wso2.carbon.automation.core.context.toolcontext.ToolContextFactory;
import org.wso2.carbon.automation.core.context.usermanagementcontext.UserManagerContextFactory;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutomationContextReader {
    private static final Log log = LogFactory.getLog(AutomationContextReader.class);
    AutomationContextReader contextReaderInstance;
    AutomationContext automationContext;
    XMLStreamReader xmlStream = null;

    public AutomationContextReader readAutomationContext() {
        if (contextReaderInstance == null) {
            synchronized (AutomationContextReader.class) {
                if (contextReaderInstance == null) {
                    contextReaderInstance = new AutomationContextReader();
                    readContext();
                }
            }
        }
        return contextReaderInstance;
    }

    public AutomationContext getAutomationContext() {
        return automationContext;
    }

    private void readContext() {
        {
            DataHandler handler;
            try {
                String nodeFile = ProductConstant.NODE_FILE_NAME;

                //URL clusterXmlURL = new File(String.format("%s%s", ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION, nodeFile)).toURI().toURL();
                URL clusterXmlURL = new File("/home/dharshana/wso2source/carbon/platform/trunk/products/is/modules/integration/tests/src/test/resources/automation.xml").toURI().toURL();
                handler = new DataHandler(clusterXmlURL);
                xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(handler.getInputStream());
                getAutomationPlatform(xmlStream);

            } catch (XMLStreamException e) {
                log.error(String.format("Cannot create Stream :-%s", e.getMessage()));
            } catch (IOException e) {
                log.error(String.format("File Input Error :-%s", e.getMessage()));
            }
        }
    }

    private AutomationContext getAutomationPlatform(XMLStreamReader xmlStreamReader) {
        StAXOMBuilder builder = new StAXOMBuilder(xmlStreamReader);
        OMElement endPointElem = builder.getDocumentElement();
        DatabaseContextFactory databaseContextFactory = new DatabaseContextFactory();
        ConfigurationContextFactory configContextFactory = new ConfigurationContextFactory();
        PlatformContextFactory platformContextFactory = new PlatformContextFactory();
        SecurityContextFactory securityContextFactory = new SecurityContextFactory();
        ToolContextFactory toolsContextFactory = new ToolContextFactory();
        UserManagerContextFactory userManagerContextFactory = new UserManagerContextFactory();
        FeatureManagementContextFactory featureManagementContextFactory = new FeatureManagementContextFactory();
        OMElement nodeElement;
        Iterator elemChildren = endPointElem.getChildElements();
        while (elemChildren.hasNext()) {
            nodeElement = (OMElement) elemChildren.next();
            if (nodeElement.getLocalName().equals(ContextConstants.CONFIGURATION_CONTEXT_NODE)) {

                configContextFactory.createConfiguration(nodeElement);
            }
            if (nodeElement.getLocalName().equals(ContextConstants.DATABASE_CONTEXT_NODE)) {

                databaseContextFactory.createDatabaseContext(nodeElement);
            }
            if (nodeElement.getLocalName().equals(ContextConstants.PLATFORM_CONTEXT_NODE)) {

                platformContextFactory.createConfiguration(nodeElement);
            }
            if (nodeElement.getLocalName().equals(ContextConstants.SECURITY_CONTEXT_NODE)) {
                securityContextFactory.createSecurityContext(nodeElement);
            }
            if (nodeElement.getLocalName().equals(ContextConstants.TOOLS_CONTEXT_NODE)) {

                toolsContextFactory.createConfiguration(nodeElement);
            }
            if (nodeElement.getLocalName().equals(ContextConstants.USER_MANAGEMENT_CONTEXT_NODE)) {
                userManagerContextFactory.createConfiguration(nodeElement);
            }
            if (nodeElement.getLocalName().equals(ContextConstants.FEATURE_MANAGEMENT_CONTEXT_NODE)) {
                featureManagementContextFactory.createFeatureManagementContext(nodeElement);
            }
        }
        automationContext.setConfigurationContext(configContextFactory.getConfigurationContext());
        automationContext.setDatabaseContext(databaseContextFactory.getDatabaseContext());
        automationContext.setFeatureManagementContext(featureManagementContextFactory.getFeatureManagementContext());
        automationContext.setPlatformContext(platformContextFactory.getPlatformContext());
        automationContext.setSecurityContext(securityContextFactory.getSecurityContext());

        return automationContext;
    }
}
