/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.idp.mgt.dto;

import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtUtil;

public class TrustedIdPDTO {

    /**
     * The trusted IdP's unique name for this tenant.
     */
    private String idPName;

    /**
     * The trusted IdP's Issuer ID for this tenant.
     */
    private String idPIssuerId;

    /**
     * The trusted IdP's URL for this tenant.
     */
    private String idPUrl;

    /**
     * The trusted IdP's Certificate for this tenant.
     */
    private String publicCert;

    /**
     * The trusted IdP's roles for this tenant.
     */
    private String[] roles;

    /**
     * The trusted IdP's role mapping for this tenant.
     */
    private String[] roleMappings;

    /**
     * If the IdP is the primary IdP for this tenant
     */
    private boolean isPrimary;

    /**
     * Unique identifiers of the SPs
     */
    private String[] audience;

    //////////////////// Getters and Setters //////////////////////////

    public String getIdPName() {
        return idPName;
    }

    public void setIdPName(String idPName) {
        this.idPName = idPName;
    }

    public String getIdPIssuerId() {
        return idPIssuerId;
    }

    public void setIdPIssuerId(String idPIssuerId) {
        this.idPIssuerId = idPIssuerId;
    }

    public String getIdPUrl() {
        return idPUrl;
    }

    public void setIdPUrl(String idPUrl) throws IdentityProviderMgtException {
        if(idPUrl != null && !idPUrl.equals("")){
            IdentityProviderMgtUtil.validateURI(idPUrl);
        }
        this.idPUrl = idPUrl;
    }

    public String getPublicCert() {
        return publicCert;
    }

    public void setPublicCert(String publicCert) {
        this.publicCert = publicCert;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String[] getRoleMappings() {
        return roleMappings;
    }

    public void setRoleMappings(String[] roleMappings) {
        this.roleMappings = roleMappings;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public String[] getAudience() {
        return audience;
    }

    public void setAudience(String[] audience) {
        this.audience = audience;
    }

}
