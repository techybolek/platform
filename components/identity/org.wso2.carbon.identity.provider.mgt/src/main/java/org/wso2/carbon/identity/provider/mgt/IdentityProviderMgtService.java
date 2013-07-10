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
package org.wso2.carbon.identity.provider.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.provider.mgt.dao.IdPMgtDAO;
import org.wso2.carbon.identity.provider.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.identity.provider.mgt.model.TrustedIdPDO;
import org.wso2.carbon.identity.provider.mgt.util.IdentityProviderMgtUtil;

import java.util.*;

public class IdentityProviderMgtService {

    private static final Log log = LogFactory.getLog(IdentityProviderMgtService.class);

    private static IdPMgtDAO dao = new IdPMgtDAO();

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderMgtException
     */
    public TrustedIdPDTO getTenantIdP(String tenantDomain) throws IdentityProviderMgtException{
        int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
        TrustedIdPDO trustedIdPDO = dao.getTenantIdP(tenantId, tenantDomain);
        TrustedIdPDTO trustedIdPDTO = null;
        if(trustedIdPDO != null){
            trustedIdPDTO = new TrustedIdPDTO();
            trustedIdPDTO.setIdPIssuerId(trustedIdPDO.getIdPIssuerId());
            trustedIdPDTO.setIdPUrl(trustedIdPDO.getIdPUrl());
            if(trustedIdPDO.getPublicCertThumbPrint() != null){
                trustedIdPDTO.setPublicCert(IdentityProviderMgtUtil.getEncodedIdPCertFromAlias(tenantId, tenantDomain));
            }
            trustedIdPDTO.setRoles(trustedIdPDO.getRoles().toArray(new String[trustedIdPDO.getRoles().size()]));
            List<String> appendedRoleMappings = new ArrayList<String>();
            for(Map.Entry<String,String> entry:trustedIdPDO.getRoleMappings().entrySet()){
                String idpRole = entry.getKey();
                String tenantRole = entry.getValue();
                appendedRoleMappings.add(idpRole+":"+tenantRole);
            }
            trustedIdPDTO.setRoleMappings(appendedRoleMappings.toArray(new String[appendedRoleMappings.size()]));
        }
        return trustedIdPDTO;
    }

    /**
     * Updates a given tenant with trusted IDP information
     *
     * @param oldTrustedIdP existing tenant IdP information
     * @param newTrustedIdP new tenant IdP information
     * @throws IdentityProviderMgtException
     */
    public void updateTenantIdP(TrustedIdPDTO oldTrustedIdP, TrustedIdPDTO newTrustedIdP) throws IdentityProviderMgtException {

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        String tenantDomain = CarbonContext.getCurrentContext().getTenantDomain();

        if(oldTrustedIdP == null && newTrustedIdP == null){
            log.error("Arguments are NULL");
            throw new IdentityProviderMgtException("Invalid arguments");
        } else if (oldTrustedIdP != null && newTrustedIdP == null){
            doDeleteIdP(oldTrustedIdP, tenantId, tenantDomain);
            return;
        } else if(oldTrustedIdP == null && newTrustedIdP !=null){
            doAddIdP(newTrustedIdP, tenantId, tenantDomain);
            return;
        }

        TrustedIdPDO newTrustedIdPDO = new TrustedIdPDO();
        TrustedIdPDO oldTrustedIdPDO = new TrustedIdPDO();

        if(oldTrustedIdP.getIdPIssuerId() == null || oldTrustedIdP.getIdPIssuerId().equals("")){
            String msg = "Invalid arguments: IssuerId value is empty";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        oldTrustedIdPDO.setIdPIssuerId(oldTrustedIdP.getIdPIssuerId());
        oldTrustedIdPDO.setIdPUrl(oldTrustedIdP.getIdPUrl());
        if(oldTrustedIdP.getPublicCert() != null){
            oldTrustedIdPDO.setPublicCertThumbPrint(IdentityProviderMgtUtil.generatedThumbPrint(oldTrustedIdP.getPublicCert()));
        }
        if(oldTrustedIdP.getRoles() != null){
            oldTrustedIdPDO.setRoles(new ArrayList<String>(Arrays.asList(oldTrustedIdP.getRoles())));
        } else {
            oldTrustedIdPDO.setRoles(new ArrayList<String>());
        }
        for(int i = 0; i < oldTrustedIdPDO.getRoles().size(); i++){
            if(oldTrustedIdPDO.getRoles().get(i) == null){
                String msg = "Invalid arguments: role names cannot be \'NULL\'";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }else if(oldTrustedIdPDO.getRoles().get(i).equals("")){
                String msg = "Invalid arguments: role names cannot be strings of zero length in \'oldTrustedIdP\' argument";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
        Map<String,String> mappings = new HashMap<String, String>();
        if(oldTrustedIdP.getRoleMappings() != null){
            for(String mapping:oldTrustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                mappings.put(split[0],split[1]);
            }
        }
        oldTrustedIdPDO.setRoleMappings(mappings);

        if(newTrustedIdP.getIdPIssuerId() == null || newTrustedIdP.getIdPIssuerId().equals("")){
            String msg = "Invalid arguments: IssuerId value is empty";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        newTrustedIdPDO.setIdPIssuerId(newTrustedIdP.getIdPIssuerId());
        newTrustedIdPDO.setIdPUrl(newTrustedIdP.getIdPUrl());
        if(newTrustedIdP.getPublicCert() != null){
            newTrustedIdPDO.setPublicCertThumbPrint(IdentityProviderMgtUtil.generatedThumbPrint(newTrustedIdP.getPublicCert()));
        }
        if(newTrustedIdP.getRoles() != null){
            newTrustedIdPDO.setRoles(new ArrayList<String>(Arrays.asList(newTrustedIdP.getRoles())));
        } else {
            newTrustedIdPDO.setRoles(new ArrayList<String>());
        }
        for(int i = 0; i < newTrustedIdPDO.getRoles().size(); i++){
            if(newTrustedIdPDO.getRoles().get(i) == null){
                String msg = "Invalid arguments: role names cannot be \'NULL\'";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
            if(newTrustedIdPDO.getRoles().get(i).equals("")){
                newTrustedIdPDO.getRoles().remove(i);
                newTrustedIdPDO.getRoles().add(i, null);
            }
        }
        mappings = new HashMap<String, String>();
        if(newTrustedIdP.getRoleMappings() != null){
            for(String mapping:newTrustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                mappings.put(split[0],split[1]);
            }
        }
        newTrustedIdPDO.setRoleMappings(mappings);

        dao.updateTenantIdP(oldTrustedIdPDO, newTrustedIdPDO, tenantId, tenantDomain);

        if(oldTrustedIdPDO.getPublicCertThumbPrint() != null &&
                newTrustedIdPDO.getPublicCertThumbPrint() != null &&
                !oldTrustedIdPDO.getPublicCertThumbPrint().equals(newTrustedIdPDO.getPublicCertThumbPrint()) ||
                oldTrustedIdPDO.getPublicCertThumbPrint() != null && newTrustedIdPDO.getPublicCertThumbPrint() == null ||
                oldTrustedIdPDO.getPublicCertThumbPrint() == null && newTrustedIdPDO.getPublicCertThumbPrint() != null){
            IdentityProviderMgtUtil.updateCertToStore(newTrustedIdP.getPublicCert(), tenantId, tenantDomain);
        }
    }

    private void doAddIdP(TrustedIdPDTO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        TrustedIdPDO trustedIdPDO = new TrustedIdPDO();
        if(trustedIdP.getIdPIssuerId() == null || trustedIdP.getIdPIssuerId().equals("")){
            String msg = "Invalid arguments: IssuerId value is empty";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        trustedIdPDO.setIdPIssuerId(trustedIdP.getIdPIssuerId());
        trustedIdPDO.setIdPUrl(trustedIdP.getIdPUrl());
        if(trustedIdP.getPublicCert() != null){
            trustedIdPDO.setPublicCertThumbPrint(IdentityProviderMgtUtil.generatedThumbPrint(trustedIdP.getPublicCert()));
        }
        if(trustedIdP.getRoles() != null && trustedIdP.getRoles().length > 0){
            trustedIdPDO.setRoles(new ArrayList<String>(Arrays.asList(trustedIdP.getRoles())));
        } else {
            trustedIdPDO.setRoles(new ArrayList<String>());
        }
        for(String role:trustedIdPDO.getRoles()){
            if(role.equals("")){
                String msg = "Invalid arguments: role name strings cannot be of zero length";
                log.error(msg);
                throw new IdentityProviderMgtException(msg);
            }
        }
        Map<String,String> mappings = new HashMap<String, String>();
        if(trustedIdP.getRoleMappings() != null && trustedIdP.getRoleMappings().length > 0){
            for(String mapping:trustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                mappings.put(split[0],split[1]);
            }
        }
        trustedIdPDO.setRoleMappings(mappings);

        dao.addTenantIdP(trustedIdPDO, tenantId, tenantDomain);

        if(trustedIdP.getPublicCert() != null){
            IdentityProviderMgtUtil.importCertToStore(trustedIdP.getPublicCert(), tenantId, tenantDomain);
        }
    }

    private void doDeleteIdP(TrustedIdPDTO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        TrustedIdPDO trustedIdPDO = new TrustedIdPDO();
        if(trustedIdP.getIdPIssuerId() == null || trustedIdP.getIdPIssuerId().equals("")){
            String msg = "Invalid arguments: IssuerId value is empty";
            log.error(msg);
            throw new IdentityProviderMgtException(msg);
        }
        trustedIdPDO.setIdPIssuerId(trustedIdP.getIdPIssuerId());
        trustedIdPDO.setIdPUrl(trustedIdP.getIdPUrl());

        dao.deleteTenantIdP(trustedIdPDO, tenantId, tenantDomain);

        if(trustedIdP.getPublicCert() != null){
            IdentityProviderMgtUtil.deleteCertFromStore(tenantId, tenantDomain);
        }
    }

}
