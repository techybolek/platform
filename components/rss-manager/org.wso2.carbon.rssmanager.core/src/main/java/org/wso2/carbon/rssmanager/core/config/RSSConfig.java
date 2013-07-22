/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.rssmanager.core.config;

import org.w3c.dom.Document;
import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a WSO2 RSS configuration.
 */
@XmlRootElement(name = "rss-configuration")
public final class RSSConfig {

    private static RSSConfig currentRSSConfig;

    private RSSEnvironment[] rssEnvironments;

    private RSSManagementRepository rssMgtRepository;

    private Map<String, RSSEnvironment> rssEnvironmentMap = new HashMap<String, RSSEnvironment>();

    /**
     * Retrieves the RSS config reading the rss-instance configuration file.
     *
     * @return RSSConfig
     * @throws RSSManagerException Is thrown if the RSS configuration is not initialized properly
     */
    public static synchronized RSSConfig getInstance() throws RSSManagerException {
        if (currentRSSConfig == null) {
            throw new RSSManagerException("RSS configuration is not initialized and is null");
        }
        return currentRSSConfig;
    }

    public void init() throws RSSManagerException {
        String rssConfigXMLPath = CarbonUtils.getCarbonConfigDirPath() +
                File.separator + "etc" + File.separator + RSSManagerConstants.RSS_CONFIG_XML_NAME;
        try {
            File rssConfig = new File(rssConfigXMLPath);
            Document doc = RSSManagerUtil.convertToDocument(rssConfig);
            RSSManagerUtil.secureResolveDocument(doc);
            try {
                JAXBContext ctx = JAXBContext.newInstance(RSSConfig.class);
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                currentRSSConfig = (RSSConfig) unmarshaller.unmarshal(doc);
            } catch (JAXBException e) {
                throw new RSSManagerException("Error occurred while creating JAXB Context to " +
                        "parse RSSConfig : " + e.getMessage(), e);
            }

            /* Populating a map with environment information to be able to lookup efficiently */
            this.populateRSSEnvironmentMap();
            /* Initializing environment configurations */
            this.initRSSEnvironments();
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while initializing RSS config", e);
        }
    }

    @XmlElement(name = "rss-mgt-repository", nillable = false)
    public RSSManagementRepository getRSSManagementRepository() {
        return rssMgtRepository;
    }

    public void setRSSManagementRepository(RSSManagementRepository rssMgtRepository) {
        this.rssMgtRepository = rssMgtRepository;
    }

    @XmlElementWrapper(name = "rss-environments", nillable = false)
    @XmlElement(name = "rss-environment", nillable = false)
    public RSSEnvironment[] getRSSEnvironments() {
        return rssEnvironments;
    }

    public void setRSSEnvironments(RSSEnvironment[] rssEnvironments) {
        this.rssEnvironments = rssEnvironments;
    }

    private Map<String, RSSEnvironment> getEnvironmentMap() {
        return rssEnvironmentMap;
    }

    public RSSManager getRSSManager(RSSEnvironmentContext ctx) throws RSSManagerException {
        String envName = ctx.getEnvironmentName();
        RSSEnvironment env = getEnvironmentMap().get(envName);
        if (env == null) {
            throw new RSSManagerException("RSS Environment '" + envName + "' does not exist");
        }
        return env.getRSSManager(ctx.getRssInstanceName());
    }

    /**
     * Initializes all RSS environments.
     *
     * @throws RSSManagerException If the flow is interrupted by some erroneous condition
     */
    private void initRSSEnvironments() throws RSSManagerException {
        RSSEnvironment[] envs = getRSSEnvironments();
        for (RSSEnvironment env : envs) {
            env.init();
        }
    }

    /**
     * Populates a map with all environment information to be able to lookup environments with its
     * name efficiently for later operations.
     */
    private void populateRSSEnvironmentMap() {
        for (RSSEnvironment env : getRSSEnvironments()) {
            getEnvironmentMap().put(env.getName(), env);
        }
    }

}
