/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.lb.common.conf.util;

import java.util.Map;

/**
 * This object will hold all the data related to a tenant.
 */
public class TenantDomainContext {
    
    /**
     * this is the unique identifier for this object
     */
    private int tenantId;
    
    /**
     * Domain, which this tenant belongs to.
     */
    private String domain;
    
    /**
     * Sub domain, which this tenant belongs to.
     */
    private String subDomain;

    private int groupMgtPort;

    private Map<String, String> members;
    
    public TenantDomainContext(int tenantId, String domain, String subDomain) {
        this.tenantId = tenantId;
        this.domain = domain;
        this.subDomain = subDomain;
    }

    /** Getters and Setters **/

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public int getGroupMgtPort() {
        return groupMgtPort;
    }

    public void setGroupMgtPort(int groupMgtPort) {
        this.groupMgtPort = groupMgtPort;
    }

    public Map<String, String> getMembers(){
        return members;
    }

    public void setMembers(Map<String, String> members){
        this.members = members;
    }


    /** End of Getters and Setters **/
    
    
    

}
