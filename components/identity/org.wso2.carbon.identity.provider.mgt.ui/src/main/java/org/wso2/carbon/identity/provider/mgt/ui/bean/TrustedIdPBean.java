/*
* Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.identity.provider.mgt.ui.bean;

import org.wso2.carbon.identity.provider.mgt.ui.util.IdentityProviderMgtUIUtil;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class TrustedIdPBean {

    /**
     * The trusted IDP's Issuer ID for this tenant.
     */
    private String idPIssuerId;

    /**
     * The trusted IDP's URL for this tenant.
     */
    private String idPUrl;

    /**
     * The trusted IdP's Certificate for this tenant
     */
    private CertData certData;

    /**
     * The trusted IDP's roles for this tenant.
     */
    private List<String> roles;

    /**
     * The trusted IDP's role mapping for this tenant.
     */
    private Map<String,String> roleMappings;

    /**
     * Id this IdP is the primary IdP for this tenant.
     */
    private boolean primary;

    /**
     * Unique identifiers of the SPs
     */
    private List<String> audience;

    //////////////////// Getters and Setters //////////////////////////


    public String getIdPIssuerId() {
        return idPIssuerId;
    }

    public void setIdPIssuerId(String idPIssuerId) {
        this.idPIssuerId = idPIssuerId;
    }

    public String getIdPUrl() {
        return idPUrl;
    }

    public void setIdPUrl(String idPUrl) throws MalformedURLException {
        if(idPUrl != null && !idPUrl.equals("")){
            IdentityProviderMgtUIUtil.validateURI(idPUrl);
        }
        this.idPUrl = idPUrl;
    }

    public CertData getCertData(){
        return certData;
    }

    public void setCertData(CertData certData){
        this.certData = certData;
    }

    public Map<String, String> getRoleMappings() {
        return roleMappings;
    }

    public void setRoleMappings(Map<String, String> roleMappings) {
        this.roleMappings = roleMappings;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public List<String> getAudience() {
        return audience;
    }

    public void setAudience(List<String> audience) {
        this.audience = audience;
    }

}
