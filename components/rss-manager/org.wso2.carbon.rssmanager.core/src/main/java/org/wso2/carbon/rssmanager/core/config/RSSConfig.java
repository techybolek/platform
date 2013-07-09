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
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironment;
import org.wso2.carbon.rssmanager.core.config.environment.RSSEnvironmentContext;
import org.wso2.carbon.rssmanager.core.internal.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.internal.util.RSSManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

/**
 * Represents a WSO2 RSS configuration.
 */
@XmlRootElement(name = "rss-configuration")
public class RSSConfig {

    private RSSEnvironment[] rssEnvironments;

    private RSSManagementRepository rssMgtRepository;

    private static RSSConfig currentRSSConfig;
    
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

    public static void init() throws RSSManagerException {
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
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while initializing RSS config", e);
        }
    }

    @XmlElement(name = "rss-mgt-repository", nillable = false)
    public RSSManagementRepository getRSSManagementRepository() {
        return rssMgtRepository;
    }

    @XmlElementWrapper(name = "rss-environments", nillable = false)
    @XmlElement(name = "rss-environment", nillable = false)
    public RSSEnvironment[] getRSSEnvironments() {
        return rssEnvironments;
    }

    public void setRSSManagementRepository(RSSManagementRepository rssMgtRepository) {
        this.rssMgtRepository = rssMgtRepository;
    }

    public void setRSSEnvironments(RSSEnvironment[] rssEnvironments) {
        this.rssEnvironments = rssEnvironments;
    }

    public RSSManager getRSSManager(RSSEnvironmentContext ctx) {
        for (RSSEnvironment rssEnvironment : getRSSEnvironments()) {
            if (rssEnvironment.getName().equals(ctx.getEnvironmentName())) {
                return rssEnvironment.getRSSManager();
            }
        }
        return null;
    }

    /**
     * Initializes RSS environments
     *
     * @throws RSSManagerException If the flow is interrupted by some erroneous condition
     */
    public void initRSSEnvironments() throws RSSManagerException {
        RSSEnvironment[] rssEnvironments = getRSSEnvironments();
        for (RSSEnvironment rssEnvironment : rssEnvironments) {
            rssEnvironment.init();
        }
    }

}
