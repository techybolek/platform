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

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.provider.mgt.dao.IdPMgtDAO;
import org.wso2.carbon.identity.provider.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.identity.provider.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.identity.provider.mgt.model.TrustedIdPDO;
import org.wso2.carbon.identity.provider.mgt.util.IdentityProviderMgtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IdPMetadataService {

    private static IdPMgtDAO dao = new IdPMgtDAO();

    /**
     * Retrieves registered IdPs for a given tenant
     *
     * @throws org.wso2.carbon.identity.provider.mgt.exception.IdentityProviderMgtException
     */
    public String[] getTenantIdPs() throws IdentityProviderMgtException {
        String tenantDomain = CarbonContext.getCurrentContext().getTenantDomain();
        int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
        List<String> tenantIdPs = dao.getTenantIdPs(tenantId, tenantDomain);
        return tenantIdPs.toArray(new String[tenantIdPs.size()]);
    }

    /**
     * Retrieves trusted IdP information about a given tenant
     *
     * @param tenantDomain Tenant domain whose information is requested
     * @throws org.wso2.carbon.identity.provider.mgt.exception.IdentityProviderMgtException
     */
    public TrustedIdPDTO getTenantIdPMetaData(String issuer, String tenantDomain) throws IdentityProviderMgtException {
        int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
        TrustedIdPDO trustedIdPDO = dao.getTenantIdP(issuer, tenantId, tenantDomain);
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
     * Retrieves trusted IdP URL for a given tenant
     *
     * @param tenantDomain Tenant domain whose information is requested
     * @throws IdentityProviderMgtException
     */
    public String getTenantIdPUrl(String issuer, String tenantDomain) throws IdentityProviderMgtException{
        int tenantId = IdentityProviderMgtUtil.getTenantIdOfDomain(tenantDomain);
        TrustedIdPDO trustedIdPDO = dao.getTenantIdP(issuer, tenantId, tenantDomain);
        if(trustedIdPDO != null){
            return trustedIdPDO.getIdPUrl();
        }
        return null;
    }
}
