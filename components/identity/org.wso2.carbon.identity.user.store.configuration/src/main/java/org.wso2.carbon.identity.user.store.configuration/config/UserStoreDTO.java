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
package org.wso2.carbon.identity.user.store.configuration.config;

import org.wso2.carbon.user.api.Property;

public class UserStoreDTO {
    private int order;
    private String className;
    private String description;
    private String domainId;
    private String connectionName;
    private String connectionURL;
    private String connectionPassword;
    private String tenantManager;
    private Boolean isDisabled;
    private Boolean isSCIMEnabled;
    private Boolean isKDCEnabled;
    private Property[] properties;



    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    public String getTenantManager() {
        return tenantManager;
    }

    public void setTenantManager(String tenantManager) {
        this.tenantManager = tenantManager;
    }

    public Boolean getDisabled() {
        return isDisabled;
    }

    public void setDisabled(Boolean disabled) {
        isDisabled = disabled;
    }

    public Boolean getSCIMEnabled() {
        return isSCIMEnabled;
    }

    public void setSCIMEnabled(Boolean SCIMEnabled) {
        isSCIMEnabled = SCIMEnabled;
    }

    public Boolean getKDCEnabled() {
        return isKDCEnabled;
    }

    public void setKDCEnabled(Boolean KDCEnabled) {
        isKDCEnabled = KDCEnabled;
    }

    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }
}
