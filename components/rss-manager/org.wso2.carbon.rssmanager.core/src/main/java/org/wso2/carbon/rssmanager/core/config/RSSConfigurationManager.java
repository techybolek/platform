/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.manager.RSSManagerProxyFactory;
import org.wso2.carbon.rssmanager.core.util.RSSManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class RSSConfigurationManager {

    private RSSManager rssManager;
    private RSSConfiguration currentRSSConfig;
    private static RSSConfigurationManager rssConfigManager = new RSSConfigurationManager();

    private RSSConfigurationManager() {
        /* Making the constructor of RSSConfigurationManager private as it is being used as a
         * Singleton */
    }

    public static RSSConfigurationManager getInstance() {
        return rssConfigManager;
    }

    public RSSManager getRSSManager() {
        if (rssManager == null) {
            /* The synchronize block is added to prevent a concurrent thread trying to access the
            rss manager while it is being initialized. */
            synchronized (this) {
                return rssManager;
            }
        }
        return rssManager;
    }

    public synchronized void initConfig() throws RSSManagerException {
        String rssConfigXMLPath = CarbonUtils.getCarbonConfigDirPath() +
                File.separator + "etc" + File.separator + RSSManagerConstants.RSS_CONFIG_XML_NAME;
        try {
            File rssConfig = new File(rssConfigXMLPath);
            Document doc = RSSManagerUtil.convertToDocument(rssConfig);
            RSSManagerUtil.secureResolveDocument(doc);

            /* Un-marshaling RSS configuration */
            JAXBContext ctx = JAXBContext.newInstance(RSSConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            this.currentRSSConfig = (RSSConfiguration) unmarshaller.unmarshal(doc);

            this.rssManager =
                    RSSManagerProxyFactory.getRSSManagerProxy(
                            getRSSConfiguration().getRSSProvider(), getRSSConfiguration());
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while initializing RSS config", e);
        }
    }

    private RSSConfiguration getRSSConfiguration() {
        return currentRSSConfig;
    }

}
