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
package org.wso2.carbon.idp.mgt;

import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.idp.mgt.dao.IdPMgtDAO;
import org.wso2.carbon.idp.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.idp.mgt.model.TrustedIdPDO;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtConstants;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtUtil;
import org.wso2.carbon.idp.mgt.util.SAMLResponseValidator;
import org.wso2.carbon.idp.mgt.util.X509CredentialImpl;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdPMetadataService {

    private static IdPMgtDAO dao = new IdPMgtDAO();

    /**
     * Retrieves registered IdPs for a given tenant
     *
     * @throws org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException
     */
    public String[] getTenantIdPs(String tenantDomain) throws IdentityProviderMgtException {
        int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
        Connection dbConnection = null;
        List<String> tenantIdPs = null;
        try {
            dbConnection = IdentityProviderMgtUtil.getDBConnection();
            tenantIdPs = dao.getTenantIdPs(dbConnection, tenantId, tenantDomain);
        } catch (IdentityProviderMgtException e) {
            throw new IdentityProviderMgtException("Error getting Identity DB connection", e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
        return tenantIdPs.toArray(new String[tenantIdPs.size()]);
    }

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @param tenantDomain Tenant domain whose information is requested
     * @throws org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException
     */
    public TrustedIdPDTO getTenantIdPMetaData(String issuer, String tenantDomain) throws IdentityProviderMgtException {
        int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
        TrustedIdPDO trustedIdPDO = dao.getTenantIdP(issuer, tenantId, tenantDomain);
        TrustedIdPDTO trustedIdPDTO = null;
        if(trustedIdPDO != null){
            trustedIdPDTO = new TrustedIdPDTO();
            trustedIdPDTO.setIdPIssuerId(trustedIdPDO.getIdPIssuerId());
            trustedIdPDO.setPrimary(trustedIdPDO.isPrimary());
            trustedIdPDTO.setIdPUrl(trustedIdPDO.getIdPUrl());
            if(trustedIdPDO.getPublicCertThumbPrint() != null){
                trustedIdPDTO.setPublicCert(IdentityProviderMgtUtil.getEncodedIdPCertFromAlias(issuer, tenantId, tenantDomain));
            }
            trustedIdPDTO.setRoles(trustedIdPDO.getRoles().toArray(new String[trustedIdPDO.getRoles().size()]));
            List<String> appendedRoleMappings = new ArrayList<String>();
            for(Map.Entry<String,String> entry:trustedIdPDO.getRoleMappings().entrySet()){
                String idpRole = entry.getKey();
                String tenantRole = entry.getValue();
                appendedRoleMappings.add(idpRole+":"+tenantRole);
            }
            trustedIdPDTO.setRoleMappings(appendedRoleMappings.toArray(new String[appendedRoleMappings.size()]));
            trustedIdPDTO.setPrimary(trustedIdPDO.isPrimary());
            trustedIdPDTO.setAudience(trustedIdPDO.getAudience().toArray(new String[trustedIdPDO.getAudience().size()]));
        }
        return trustedIdPDTO;
    }

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @param issuer Issuer Id of the IdP whose roles need to be mapped
     * @param tenantDomain The tenant domain of of roles to be mapped
     * @param idPRoles IdP Roles which need to be mapped to tenant's roles
     * @throws org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException
     */
    public String[] getMappedTenantRoles(String issuer, String tenantDomain, String[] idPRoles) throws IdentityProviderMgtException {
        List<String> mappedRoles = new ArrayList<String>();
        int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
        TrustedIdPDO trustedIdPDO = dao.getTenantIdP(issuer, tenantId, tenantDomain);
        Map<String, String> roleMappings = trustedIdPDO.getRoleMappings();
        if(roleMappings != null && !roleMappings.isEmpty()){
            if(idPRoles == null){
                for(Map.Entry<String,String> roleMapping: roleMappings.entrySet()){
                    mappedRoles.add(roleMapping.getKey() + ":" + roleMapping.getValue());
                }
            } else {
                for(String idPRole : idPRoles){
                    if(roleMappings.containsKey(idPRole)){
                        mappedRoles.add(idPRole + ":" + roleMappings.get(idPRole));
                    } else {
                        mappedRoles.add(idPRole+":");
                    }
                }
            }
        }
        return mappedRoles.toArray(new String[mappedRoles.size()]);
    }

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @param issuer Issuer Id of the IdP to which the given tenant roles need to be mapped
     * @param tenantDomain The tenant domain of of roles to be mapped
     * @param tenantRoles Tenant Roles which need to be mapped to trusted IdP's roles
     * @throws org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException
     */
    public String[] getMappedIdPRoles(String issuer, String tenantDomain, String[] tenantRoles) throws IdentityProviderMgtException {
        List<String> mappedRoles = new ArrayList<String>();
        int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
        TrustedIdPDO trustedIdPDO = dao.getTenantIdP(issuer, tenantId, tenantDomain);
        Map<String, String> roleMappings = trustedIdPDO.getRoleMappings();
        Map<String,String> mirrorMap = new HashMap<String,String>();
        if(roleMappings != null && !roleMappings.isEmpty()){
            for(Map.Entry<String,String> roleMapping : roleMappings.entrySet()){
                String key = roleMapping.getKey();
                String value = roleMapping.getValue();
                if(mirrorMap.containsKey(value)){
                    mirrorMap.put(value, mirrorMap.get(value) + "," + key);
                } else {
                    mirrorMap.put(value, key);
                }
            }
            if(tenantRoles == null){
                for(Map.Entry<String,String> mirrorRole: mirrorMap.entrySet()){
                    mappedRoles.add(mirrorRole.getKey() + ":" + mirrorRole.getValue());
                }
            } else {
                for(String tenantRole : tenantRoles){
                    if(mirrorMap.containsKey(tenantRole)){
                        mappedRoles.add(tenantRole + ":" + mirrorMap.get(tenantRole));
                    } else {
                        mappedRoles.add(tenantRole + ":");
                    }

                }
            }
        }
        return mappedRoles.toArray(new String[mappedRoles.size()]);
    }

    public String getPrimaryIdP(String tenantDomain) throws IdentityProviderMgtException {

        String[] tenantIdPs = getTenantIdPs(tenantDomain);
        for(String tenantIdP : tenantIdPs){
            TrustedIdPDTO trustedIdP = getTenantIdPMetaData(tenantIdP, tenantDomain);
            if(trustedIdP.isPrimary()){
                return tenantIdP;
            }
        }
        throw new IdentityProviderMgtException("Primary IdP not found");
    }

    public boolean validateSAMLResponse(String tenantDomain, String issuer, String samlResponseString, String[] audiences) throws IdentityProviderMgtException {

        return SAMLResponseValidator.validateSAMLResponse(getTenantIdPMetaData(issuer, tenantDomain),  samlResponseString, audiences);
    }
}
