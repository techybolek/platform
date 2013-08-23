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

package org.wso2.carbon.automation.core.context.configurationcontext;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.ContextConstants;

import java.util.Iterator;

/**
 * provides the configuration context
 */
public class ConfigurationContextFactory {

    /**
     * this method creates and returns the internal data structure for the configuration node in automation.xml
     * @param nodeElement
     */
    public Configuration createConfiguration(OMElement nodeElement) {
        Configuration configuration = new Configuration();
        OMNode node;
        OMElement omElement = nodeElement;
        //iterate through Configuration properties
        Iterator configurationPropertiesIterator = omElement.getChildElements();

        while (configurationPropertiesIterator.hasNext()) {

            node = (OMNode) configurationPropertiesIterator.next();
            String attribute = ((OMElementImpl) node).getLocalName();
            String attributeValue = ((OMElementImpl) node).getText();

            //here check for the each configuration property
            if (attribute.equals(ContextConstants.CONFIGURATION_CONTEXT_DEPLOYMENT_DELAY)) {
                configuration.setDeploymentDelay(Integer.parseInt(attributeValue));
            } else if (attribute.equals(ContextConstants.CONFIGURATION_CONTEXT_EXECUTION_ENVIRONMENT)) {
                configuration.setExecutionEnvironment(attributeValue);

            } else if (attribute.equals(ContextConstants.CONFIGURATION_CONTEXT_EXECUTION_MODE)) {
                configuration.setExecutionMode(attributeValue);

            } else if (attribute.equals(ContextConstants.CONFIGURATION_CONTEXT_CLOUD_ENABLED)) {
                configuration.setCloudEnabled(Boolean.parseBoolean(attributeValue));


            } else if (attribute.equals(ContextConstants.CONFIGURATION_CONTEXT_CLUSTERING)) {
                configuration.setClustering(Boolean.parseBoolean(attributeValue));

            } else if (attribute.equals(ContextConstants.CONFIGURATION_CONTEXT_COVERAGE)) {
                configuration.setCoverage(Boolean.parseBoolean(attributeValue));

            } else if (attribute.equals(ContextConstants.CONFIGURATION_CONTEXT_FRAMEWORK_DASHBOARD)) {
                configuration.setFrameworkDashboard(Boolean.parseBoolean(attributeValue));
            }


        }

        return configuration;

    }

    /*
    this method interface: provides the configuration context providing the appropriate configuration xml node
     */
    public ConfigurationContext getConfigurationContext(OMElement element) {

        ConfigurationContext configurationContext = new ConfigurationContext();
        configurationContext.setConfiguration(createConfiguration(element));
        return configurationContext;


    }
}
