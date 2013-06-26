/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.user.store.configuration.deployer;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.identity.user.store.configuration.deployer.internal.UserStoreConfigComponent;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;

/**
 * This is to deploy a new User Store Management Configuration file dropped or created at repository/conf/server/userstore.
 * Whenever a new file with .xml extension is added or a modification is done to an existing file, deployer will
 * automatically update the existing realm configuration org.wso2.carbon.identity.user.store.configuration according to the new file.
 */
public class UserStoreConfigurationDeployer extends AbstractDeployer {


    private static Log log = LogFactory.getLog(UserStoreConfigurationDeployer.class);
    private File userMgtConfigFile;
    private RealmConfigXMLProcessor realmConfigXMLProcessor = new RealmConfigXMLProcessor();

    public void init(ConfigurationContext configurationContext) {
        log.info("User Store Configuration Deployer initiated.");

    }

    /**
     * Trigger deploying of new org.wso2.carbon.identity.user.store.configuration file
     *
     * @param deploymentFileData information about the user store org.wso2.carbon.identity.user.store.configuration
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String path = deploymentFileData.getAbsolutePath();
        OMElement realmElement;
        RealmConfiguration realmConfiguration;
        userMgtConfigFile = new File(path);
        log.info(path + " deploying...");
        try {

            realmElement = getRealmElement();
            realmConfiguration = realmConfigXMLProcessor.buildRealmConfiguration(realmElement);
            UserStoreConfigComponent.getRealmService().setBootstrapRealmConfiguration(realmConfiguration);
            log.info(path + " deployed...");

        } catch (Exception ex) {
            throw new DeploymentException("The deployment of " + userMgtConfigFile.getName() + " is not valid.", ex);
        }
    }

    public void setDirectory(String s) {

    }

    public void setExtension(String s) {

    }


    /**
     * Create an OMElement from the user-mgt.xml file.
     *
     * @return
     * @throws XMLStreamException
     * @throws IOException
     * @throws UserStoreException
     */
    private OMElement getRealmElement() throws XMLStreamException, IOException, UserStoreException {
        StAXOMBuilder builder = null;
        InputStream inStream;
        try {

            inStream = new FileInputStream(userMgtConfigFile);

            if (inStream == null) {
                String message = "Configuration file could not be read in.";
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new FileNotFoundException(message);
            }
            inStream = CarbonUtils.replaceSystemVariablesInXml(inStream);
        } catch (CarbonException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
        builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        realmConfigXMLProcessor.setSecretResolver(documentElement);

        OMElement realmElement = documentElement.getFirstChildWithName(new QName(
                UserCoreConstants.RealmConfig.LOCAL_NAME_REALM));

        return realmElement;
    }

}
