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

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.provider.mgt.dao.IdPMgtDAO;
import org.wso2.carbon.identity.provider.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.identity.provider.mgt.model.TrustedIdPDO;
import org.wso2.carbon.identity.provider.mgt.util.IdentityProviderMgtUtil;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
                trustedIdPDTO.setPublicCert(getEncodedPublicCertForX509CertThumb(trustedIdPDO.getPublicCertThumbPrint(), tenantId, tenantDomain));
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

        if(oldTrustedIdP.getIdPIssuerId() == null){
            throw new IdentityProviderMgtException("Invalid arguments: IssuerId value is NULL");
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
        Map<String,String> mappings = new HashMap<String, String>();
        if(oldTrustedIdP.getRoleMappings() != null){
            for(String mapping:oldTrustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                mappings.put(split[0],split[1]);
            }
        }
        oldTrustedIdPDO.setRoleMappings(mappings);

        if(newTrustedIdP.getIdPIssuerId() == null){
            throw new IdentityProviderMgtException("Invalid arguments: IssuerId value is NULL");
        }
        newTrustedIdPDO.setIdPIssuerId(newTrustedIdP.getIdPIssuerId());
        newTrustedIdPDO.setIdPUrl(newTrustedIdP.getIdPUrl());
        if(newTrustedIdP.getPublicCert() != null){
            newTrustedIdPDO.setPublicCertThumbPrint(IdentityProviderMgtUtil.generatedThumbPrint(newTrustedIdP.getPublicCert()));
        }
        if(newTrustedIdP.getRoles() != null){
            newTrustedIdPDO.setRoles(new ArrayList<String>(Arrays.asList(newTrustedIdP.getRoles())));
            if(newTrustedIdPDO.getRoles().get(0).equals("SKIP")){
                newTrustedIdPDO.getRoles().remove(0);
            }
        } else {
            newTrustedIdPDO.setRoles(new ArrayList<String>());
        }
        mappings = new HashMap<String, String>();
        if(newTrustedIdP.getRoleMappings() != null){
            for(String mapping:newTrustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                mappings.put(split[0],split[1]);
            }
        }
        newTrustedIdPDO.setRoleMappings(mappings);

        dao.updateTenantIdP(oldTrustedIdPDO, newTrustedIdPDO, tenantId);

        if(oldTrustedIdPDO.getPublicCertThumbPrint() != null &&
                newTrustedIdPDO.getPublicCertThumbPrint() != null &&
                !oldTrustedIdPDO.getPublicCertThumbPrint().equals(newTrustedIdPDO.getPublicCertThumbPrint())){
            updateCertInStore(newTrustedIdP.getPublicCert(), tenantId, tenantDomain);
        }
    }

    private void doAddIdP(TrustedIdPDTO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        TrustedIdPDO trustedIdPDO = new TrustedIdPDO();
        if(trustedIdP.getIdPIssuerId() == null){
            throw new IdentityProviderMgtException("Invalid arguments: IssuerId value is NULL");
        }
        trustedIdPDO.setIdPIssuerId(trustedIdP.getIdPIssuerId());
        trustedIdPDO.setIdPUrl(trustedIdP.getIdPUrl());
        if(trustedIdP.getPublicCert() != null){
            trustedIdPDO.setPublicCertThumbPrint(IdentityProviderMgtUtil.generatedThumbPrint(trustedIdP.getPublicCert()));
        }
        if(trustedIdP.getRoles() != null && trustedIdP.getRoles().length > 0){
            trustedIdPDO.setRoles(new ArrayList<String>(Arrays.asList(trustedIdP.getRoles())));
        }
        Map<String,String> mappings = new HashMap<String, String>();
        if(trustedIdP.getRoleMappings() != null && trustedIdP.getRoleMappings().length > 0){
            for(String mapping:trustedIdP.getRoleMappings()){
                String[] split = mapping.split(":");
                mappings.put(split[0],split[1]);
            }
        }
        trustedIdPDO.setRoleMappings(mappings);

        dao.addTenantIdP(trustedIdPDO, tenantId);

        if(trustedIdP.getPublicCert() != null){
            importCertToStore(trustedIdP.getPublicCert(), tenantId, tenantDomain);
        }
    }

    private void doDeleteIdP(TrustedIdPDTO trustedIdP, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        TrustedIdPDO trustedIdPDO = new TrustedIdPDO();
        if(trustedIdP.getIdPIssuerId() == null){
            throw new IdentityProviderMgtException("Invalid arguments: IssuerId value is NULL");
        }
        trustedIdPDO.setIdPIssuerId(trustedIdP.getIdPIssuerId());
        trustedIdPDO.setIdPUrl(trustedIdP.getIdPUrl());

        dao.deleteTenantIdP(trustedIdPDO, tenantId);

        deleteCertFromStore(tenantId, tenantDomain);
    }

    private String getEncodedPublicCertForX509CertThumb(String thumb, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        String keyStoreName = null;
        try {
            KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                ServerConfigurationService config = ServerConfiguration.getInstance();
                String keyStorePath = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
                keyStoreName = IdentityProviderMgtUtil.getKeyStoreFileName(keyStorePath);
            } else {
                keyStoreName = tenantDomain + ".jks";
            }
            Certificate cert = null;
            MessageDigest sha = null;
            KeyStore ks = null;
            ks = keyMan.getKeyStore(keyStoreName);
            sha = MessageDigest.getInstance("SHA-1");
            for (Enumeration e = ks.aliases(); e.hasMoreElements();) {
                String alias = (String) e.nextElement();
                Certificate[] certs = ks.getCertificateChain(alias);
                if (certs == null || certs.length == 0) {
                    // no cert chain, so lets check if getCertificate gives us a result.
                    cert = ks.getCertificate(alias);
                    if (cert == null) {
                        return null;
                    }
                } else {
                    cert = certs[0];
                }
                if (!(cert instanceof X509Certificate)) {
                    continue;
                }
                sha.reset();
                sha.update(cert.getEncoded());
                byte[] data = sha.digest();
                if (thumb.equals(IdentityProviderMgtUtil.hexify(data))) {
                    return Base64.encode(cert.getEncoded());
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving certificate from thumbPrint from key store " + keyStoreName);
            throw new IdentityProviderMgtException("Error occurred while retrieving certificate from thumbPrint from key store");
        }
        return null;
    }

    private void updateCertInStore(String certData, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        try {

            KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            String keyStoreName = null;
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                ServerConfigurationService config = ServerConfiguration.getInstance();
                keyStoreName = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
            } else {
                keyStoreName = tenantDomain + ".jks";
            }
            KeyStore ks = keyMan.getKeyStore(keyStoreName);

            byte[] bytes = Base64.decode(certData);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert;
            try {
                cert = (X509Certificate) factory
                        .generateCertificate(new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                throw new IdentityProviderMgtException("Invalid format of the provided certificate file");
            }

            String alias = tenantDomain+"-idp";
            if (ks.getCertificate(alias) == null) {
                log.warn("Certificate with alias " + alias + " does not exist in tenant key store " + keyStoreName);
            } else {
                ks.deleteEntry(alias);
            }
            ks.setCertificateEntry(alias, cert);
            keyMan.updateKeyStore(keyStoreName, ks);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IdentityProviderMgtException(e.getMessage() ,e);
        }
    }

    private void importCertToStore(String certData, int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        try {

            KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            String keyStoreName = null;
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                ServerConfigurationService config = ServerConfiguration.getInstance();
                String keyStorePath = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
                keyStoreName = IdentityProviderMgtUtil.getKeyStoreFileName(keyStorePath);
            } else {
                keyStoreName = tenantDomain + ".jks";
            }
            KeyStore ks = keyMan.getKeyStore(keyStoreName);

            byte[] bytes = Base64.decode(certData);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert;
            try {
                cert = (X509Certificate) factory
                        .generateCertificate(new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                throw new IdentityProviderMgtException("Invalid format of the provided certificate file");
            }

            String alias = tenantDomain+"-idp";
            if (ks.getCertificate(alias) != null) {
                log.error("Certificate with alias " + alias + " already exists in tenant key store " + keyStoreName);
                throw new IdentityProviderMgtException("Certificate with alias " + alias + " already exists for tenant");
            }
            ks.setCertificateEntry(alias, cert);
            keyMan.updateKeyStore(keyStoreName, ks);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IdentityProviderMgtException(e.getMessage() ,e);
        }
    }

    private void deleteCertFromStore(int tenantId, String tenantDomain) throws IdentityProviderMgtException {

        try {

            KeyStoreManager keyMan = KeyStoreManager.getInstance(tenantId);
            String keyStoreName = null;
            if(MultitenantConstants.SUPER_TENANT_ID == tenantId){
                ServerConfigurationService config = ServerConfiguration.getInstance();
                String keyStorePath = config.getFirstProperty(RegistryResources.SecurityManagement.SERVER_PRIMARY_KEYSTORE_FILE);
                keyStoreName  = IdentityProviderMgtUtil.getKeyStoreFileName(keyStorePath);
            } else {
                keyStoreName = tenantDomain + ".jks";
            }
            KeyStore ks = keyMan.getKeyStore(keyStoreName);

            String alias = tenantDomain+"-idp";
            if (ks.getCertificate(alias) == null) {
                log.warn("Certificate with alias " + alias + " does not exist in tenant key store " + keyStoreName);
            } else {
                ks.deleteEntry(alias);
            }
            keyMan.updateKeyStore(keyStoreName, ks);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IdentityProviderMgtException(e.getMessage() ,e);
        }
    }

}
