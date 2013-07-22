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

package org.wso2.carbon.rssmanager.core.config.environment;

import org.wso2.carbon.rssmanager.common.RSSManagerConstants;
import org.wso2.carbon.rssmanager.core.entity.RSSInstance;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;
import org.wso2.carbon.rssmanager.core.manager.AbstractRSSManagerFactory;
import org.wso2.carbon.rssmanager.core.manager.RSSManager;
import org.wso2.carbon.rssmanager.core.manager.RSSManagerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rss-environment")
public class RSSEnvironment {

    private int id;

    private String name;

    private String rssProvider;

    private RSSManager systemRSSManager;

    private RSSManager userDefinedRSSManager;

    private RSSInstance[] rssInstances;

    public void init() throws RSSManagerException {
        RSSManagerFactory factory =
                AbstractRSSManagerFactory.getRSSManagerFactory(getRSSProvider());
        this.systemRSSManager = factory.getSystemRSSManager();
        this.userDefinedRSSManager = factory.getUserDefinedRSSManager();
        this.initRSSEnvironment();
        //this.initSystemRSSInstances();
    }

    @XmlElement(name = "name", nillable = false, required = true)
    public String getName() {
        return name;
    }

    @XmlElement(name = "rss-provider", nillable = false)
    public String getRSSProvider() {
        return rssProvider;
    }

    @XmlElementWrapper(name = "rss-instances", nillable = false)
    @XmlElement(name = "rss-instance", nillable = false)
    public RSSInstance[] getRSSInstances() {
        return rssInstances;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRSSProvider(String rssProvider) {
        this.rssProvider = rssProvider;
    }

    public void setRSSInstances(RSSInstance[] rssInstances) {
        this.rssInstances = rssInstances;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RSSEnvironment)) {
            return false;
        }
        RSSEnvironment rssEnvironment = (RSSEnvironment) o;
        return this.getName().equals(rssEnvironment.getName());
    }

    @Override
    public int hashCode() {
        assert false : "hashCode() is not implemented";
        return -1;
    }

    private RSSManager getSystemRSSManager() {
        return systemRSSManager;
    }

    private RSSManager getUserDefinedRSSManager() {
        return userDefinedRSSManager;
    }

    public RSSManager getRSSManager(String type) throws RSSManagerException {
        if (type == null || "".equals(type)) {
            throw new IllegalArgumentException("RSS Manager type '" + type + "' is not allowed");
        }
        if (RSSManagerConstants.WSO2_RSS_INSTANCE_TYPE.equals(type)) {
            return getSystemRSSManager();
        }
        if (RSSManagerConstants.USER_DEFINED_INSTANCE_TYPE.equals(type)) {
            return getUserDefinedRSSManager();
        }
        throw new RSSManagerException("Error occurred while retrieving RSS Manager attached to " +
                "the RSS environment '" + getName() + "'. Invalid RSS manager type '" + type + "'");
    }

    private void initRSSEnvironment() throws RSSManagerException {
        getSystemRSSManager().createRSSEnvironment();
    }

    /**
     * Initialises the RSS DAO database by reading from the "rss-config.xml".
     *
     * @throws org.wso2.carbon.rssmanager.core.exception.RSSManagerException
     *          rssDaoException
     */
//    private void initSystemRSSInstances() throws RSSManagerException {
//        try {
//            /* adds the rss instances listed in the configuration file,
//             * if any of them are already existing in the database, they will be updated */
//            Map<String, RSSInstance> rssInstances = new HashMap<String, RSSInstance>();
//            for (RSSInstance tmpInst : getRSSInstances()) {
//                rssInstances.put(tmpInst.getName(), tmpInst);
//            }
//            int tenantId = RSSManagerUtil.getTenantId();
//            getRSSManager().beginTransaction();
//
//            PrivilegedCarbonContext.startTenantFlow();
//            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
//                    MultitenantConstants.SUPER_TENANT_ID);
//
//            RSSInstance[] systemRSSInstances = new RSSInstance[0];
//
//            for (RSSInstance tmpInst : systemRSSInstances) {
//                RSSInstance reloadedRssInst = rssInstances.get(tmpInst.getName());
//                RSSInstance prevKey = rssInstances.remove(tmpInst.getName());
//                if (prevKey == null) {
//                    log.warn("Configuration corresponding to RSS instance named '" + tmpInst.getName() +
//                            "' is missing in the rss-config.xml");
//                    continue;
//                }
//                getRSSManager().getRSSDAO().getRSSInstanceDAO().updateRSSInstance(reloadedRssInst, tenantId);
//            }
//            for (RSSInstance inst : rssInstances.values()) {
//                getRSSManager().getRSSDAO().getRSSInstanceDAO().addRSSInstance(inst, tenantId);
//            }
//            getRSSManager().endTransaction();
//        } catch (RSSDAOException e) {
//            if (getRSSManager().isInTransaction()) {
//                getRSSManager().rollbackTransaction();
//            }
//            throw new RSSManagerException("Error occurred while initializing RSS environment '" +
//                    getName() + "' : " + e.getMessage(), e);
//        }
//    }

}
