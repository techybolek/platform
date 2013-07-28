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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.idp.mgt.dao.IdPMgtDAO;
import org.wso2.carbon.idp.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.idp.mgt.model.TrustedIdPDO;
import org.wso2.carbon.idp.mgt.util.IdentityProviderMgtUtil;
import org.wso2.carbon.idp.mgt.util.SAMLResponseValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdPMetadataService {

    private static Log log = LogFactory.getLog(IdPMetadataService.class);

    private static IdPMgtDAO dao = new IdPMgtDAO();

    /**
     * Retrieves registered IdPs for a given tenant
     *
     * @param tenantDomain Tenant domain whole IdP names are requested
     * @throws org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException
     */
    public String[] getTenantIdPs(String tenantDomain) {
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            List<String> tenantIdPs = null;
            tenantIdPs = dao.getTenantIdPs(null, tenantId, tenantDomain);
            return tenantIdPs.toArray(new String[tenantIdPs.size()]);
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving registered IdPs for tenant " + tenantDomain);
            }
        }
        return new String[0];
    }

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @param idPName Unique name of the IdP of whose metadata is requested
     * @param tenantDomain Tenant domain whose information is requested
     * @throws org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException
     */
    public TrustedIdPDTO getTenantIdPMetaData(String idPName, String tenantDomain) {
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getTenantIdP(idPName, tenantId, tenantDomain);
            TrustedIdPDTO trustedIdPDTO = null;
            if(trustedIdPDO != null){
                trustedIdPDTO = new TrustedIdPDTO();
                trustedIdPDTO.setIdPName(idPName);
                trustedIdPDTO.setIdPIssuerId(trustedIdPDO.getIdPIssuerId());
                trustedIdPDO.setPrimary(trustedIdPDO.isPrimary());
                trustedIdPDTO.setIdPUrl(trustedIdPDO.getIdPUrl());
                if(trustedIdPDO.getPublicCertThumbPrint() != null){
                    trustedIdPDTO.setPublicCert(IdentityProviderMgtUtil.getEncodedIdPCertFromAlias(idPName, tenantId, tenantDomain));
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
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving metadata for IdP " + idPName + " for tenant " + tenantDomain);
            }
        }
        return null;
    }

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @param idPName Unique Name of the IdP to which the given IdP roles need to be mapped
     * @param tenantDomain The tenant domain of of roles to be mapped
     * @param idPRoles IdP Roles which need to be mapped to tenant's roles
     * @throws org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException
     */
    public String[] getMappedTenantRoles(String idPName, String tenantDomain, String[] idPRoles) {
        List<String> mappedRoles = new ArrayList<String>();
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getTenantIdP(idPName, tenantId, tenantDomain);
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
        } catch (IdentityProviderMgtException e) {
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving Tenant Role mappings for IdP " + idPName + " for tenant " + tenantDomain);
            }
        }
        return new String[0];
    }

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @param idPName Unique Name of the IdP to which the given tenant roles need to be mapped
     * @param tenantDomain The tenant domain of of roles to be mapped
     * @param tenantRoles Tenant Roles which need to be mapped to trusted IdP's roles
     * @throws org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException
     */
    public String[] getMappedIdPRoles(String idPName, String tenantDomain, String[] tenantRoles) {

        List<String> mappedRoles = new ArrayList<String>();
        try {
            int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
            TrustedIdPDO trustedIdPDO = dao.getTenantIdP(idPName, tenantId, tenantDomain);
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
        } catch (IdentityProviderMgtException e){
            if(log.isDebugEnabled()){
                log.debug("Error occurred while retrieving IdP Role mappings for IdP " + idPName + " for tenant " + tenantDomain);
            }
        }
        return new String[0];
    }

    public String getPrimaryIdP(String tenantDomain) {

        String[] tenantIdPs = getTenantIdPs(tenantDomain);
        for(String tenantIdP : tenantIdPs){
            TrustedIdPDTO trustedIdP = getTenantIdPMetaData(tenantIdP, tenantDomain);
            if(trustedIdP.isPrimary()){
                return tenantIdP;
            }
        }
        if(log.isDebugEnabled()){
            log.debug("No primary IdP found for tenant " + tenantDomain);
        }
        return null;
    }

    public boolean validateSAMLResponse(String tenantDomain, String idPName, String samlResponseString, String[] audiences) {

        try {
            return SAMLResponseValidator.validateSAMLResponse(getTenantIdPMetaData(idPName, tenantDomain),  samlResponseString, audiences);
        } catch (IdentityProviderMgtException e){
            if(log.isDebugEnabled()){
                log.debug("Error occurred while validating SAML2 Response message");
            }
            return false;
        }
    }
}
