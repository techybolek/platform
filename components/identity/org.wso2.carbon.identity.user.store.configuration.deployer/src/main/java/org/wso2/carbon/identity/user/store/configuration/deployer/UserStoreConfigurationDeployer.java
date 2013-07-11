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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.user.store.configuration.deployer.internal.UserStoreConfigComponent;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.config.UserStoreConfigXMLProcessor;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is to deploy a new User Store Management Configuration file dropped or created at repository/deployment/server/userstores
 * or repository/tenant/<>tenantId</>/userstores. Whenever a new file with .xml extension is added/deleted or a modification is done to
 * an existing file, deployer will automatically update the existing realm configuration org.wso2.carbon.identity.user.store.configuration
 * according to the new file.
 */
public class UserStoreConfigurationDeployer extends AbstractDeployer {


    private static Log log = LogFactory.getLog(UserStoreConfigurationDeployer.class);


    private AxisConfiguration axisConfig;

    public void init(ConfigurationContext configurationContext) {
        log.info("User Store Configuration Deployer initiated.");
        this.axisConfig = configurationContext.getAxisConfiguration();

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
        UserStoreConfigXMLProcessor userStoreXMLProcessor = new UserStoreConfigXMLProcessor(path);
        RealmConfiguration realmConfiguration;
        File userMgtConfigFile = new File(path);
        String modifiedTenantId = null;

        try {
            realmConfiguration = userStoreXMLProcessor.buildUserStoreConfigurationFromFile();
            RealmService realmService = UserStoreConfigComponent.getRealmService();

            //tenant modified secondary user store configuration
            if (path.contains("tenants")) {
                Pattern p = Pattern.compile("-?\\d+");
                Matcher m = p.matcher(path);
                while (m.find()) {
                    modifiedTenantId = m.group();
                }
                int tenantId = Integer.parseInt(modifiedTenantId);
                realmConfiguration.setTenantId(tenantId);
                setSecondaryUserStore(realmService.getTenantUserRealm(tenantId).getRealmConfiguration(), realmConfiguration);

            } else {
                //super tenant modified secondary user store configuration
                setSecondaryUserStore(realmService.getBootstrapRealmConfiguration(), realmConfiguration);
            }
                log.info("Realm configuration of tenant:" + CarbonContext.getCurrentContext().getTenantId() + "  modified with " + path);

        } catch (Exception ex) {
            throw new DeploymentException("The deployment of " + userMgtConfigFile.getName() + " is not valid.", ex);
        }

    }

    /**
     * Set secondary user store at the very end of chain
     *
     * @param parent : primary user store
     * @param child : secondary user store
     */
    private void setSecondaryUserStore(RealmConfiguration parent, RealmConfiguration child) {

        while (parent.getSecondaryRealmConfig() != null) {
            parent = parent.getSecondaryRealmConfig();
        }

        parent.setSecondaryRealmConfig(child);
    }

    /**
     * Trigger un-deploying of a deployed file. Removes the deleted user store from chain
     *
     * @param fileName: domain name --> file name
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void undeploy(String fileName) throws DeploymentException {
        String[] fileNames = fileName.split(File.separator);
        String domainName = fileNames[fileNames.length - 1].replace(".xml", "").replace("_", ".");

        int tenantId;
        RealmConfiguration secondary;
        try {
            tenantId = CarbonContext.getCurrentContext().getTenantId();
            RealmConfiguration realmConfig = UserStoreConfigComponent.getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration();

            while (realmConfig.getSecondaryRealmConfig() != null) {
                secondary = realmConfig.getSecondaryRealmConfig();
                if (secondary.getUserStoreProperty("DomainName").equalsIgnoreCase(domainName)) {
                    realmConfig.setSecondaryRealmConfig(secondary.getSecondaryRealmConfig());
                    log.info("User store: " + domainName + " of tenant:" + tenantId + " is removed.");
                    return;
                } else {
                    realmConfig = realmConfig.getSecondaryRealmConfig();
                }
            }


        } catch (Exception ex) {
            throw new DeploymentException("Error occurred at undeploying " + domainName + " from tenant:" + CarbonContext.getCurrentContext().getTenantId(), ex);
        }

    }

    /**
     * Unset secondary user store
     *
     * @param parent : primary user store
     * @param child : secondary user store
     */
    private void unSetSecondaryUserStore(RealmConfiguration parent, RealmConfiguration child) {

        while (parent.getSecondaryRealmConfig() != null) {
            parent = parent.getSecondaryRealmConfig();
        }

        parent.setSecondaryRealmConfig(child);
    }


    public void setDirectory(String s) {

    }

    public void setExtension(String s) {

    }

}
