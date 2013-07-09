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
package org.wso2.carbon.rssmanager.core.entity;

import org.wso2.carbon.rssmanager.core.config.RDBMSConfiguration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class to represent an RSS Server Instance.
 */
@XmlRootElement(name = "rss-instance")
public class RSSInstance {

    private int id;

    private String name;

    private String dbmsType;

    private String instanceType;

    private String serverCategory;

    private RDBMSConfiguration dataSourceConfig;

    public RSSInstance(int id, String name, String dbmsType, String instanceType,
                       String serverCategory, RDBMSConfiguration dataSourceConfig) {
        this.id = id;
        this.name = name;
        this.dbmsType = dbmsType;
        this.instanceType = instanceType;
        this.serverCategory = serverCategory;
        this.dataSourceConfig = dataSourceConfig;
    }

    public RSSInstance() {
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement (name = "dbms-type")
    public String getDbmsType() {
        return dbmsType;
    }

    public void setDbmsType(String dbmsType) {
        this.dbmsType = dbmsType;
    }

    @XmlElement (name = "instance-type")
    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    @XmlElement(name = "server-category")
    public String getServerCategory() {
        return serverCategory;
    }

    public void setServerCategory(String serverCategory) {
        this.serverCategory = serverCategory;
    }

    @XmlElement (name = "datasource-config")
    public RDBMSConfiguration getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setDataSourceConfig(RDBMSConfiguration dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
