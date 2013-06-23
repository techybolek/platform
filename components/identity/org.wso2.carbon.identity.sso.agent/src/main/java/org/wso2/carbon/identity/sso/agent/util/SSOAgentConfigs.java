/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.sso.agent.util;

import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;

import javax.servlet.FilterConfig;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class SSOAgentConfigs {

    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    private static Boolean samlSSOLoginEnabled;
    private static Boolean openidLoginEnabled;
    private static String loginUrl;
    private static String samlSSOUrl;
    private static String openIdUrl;

    private static String issuerId;
    private static String consumerUrl;
	private static String idPUrl;
    private static String subjectIdSessionAttributeName;
    private static String attributeConsumingServiceIndex;
    private static String samlSSOAttributesMapName;
    private static Boolean isSLOEnabled;
    private static String logoutUrl;
    private static Boolean isResponseSigned;
    private static Boolean isAssertionSigned;
    private static Boolean isRequestSigned;
    private static String ssoAgentCredentialImplClass;
    private static InputStream keyStore;
    private static String keyStorePassword;
    private static String idPCertAlias;
    private static String privateKeyPassword;

    private static String returnTo;
    private static String claimedIdParameterName;
    private static String claimedIdSessionAttributeName;
    private static String discoverySessionAttributeName;
    private static String attributesRequestorImplClass;
    private static String openIdAttributesMapName;


    public static void initConfig(FilterConfig fConfigs) throws SSOAgentException {
        
        Properties properties = new Properties();
        try {
            if(fConfigs.getInitParameter("SSOAgentPropertiesFilePath") != null &&
                    !fConfigs.getInitParameter("SSOAgentPropertiesFilePath").equals("")){
                properties.load(new FileInputStream(fConfigs.getInitParameter("SSOAgentPropertiesFilePath")));
                initConfig(properties);
            } else {
                LOGGER.warning("\'SSOAgentPropertiesFilePath\' not configured");
            }
        } catch (FileNotFoundException e) {
            throw new SSOAgentException("Agent properties file not found");
        } catch (IOException e) {
            throw new SSOAgentException("Error occurred while reading Agent properties file");
        }

	}

    public static void initConfig(String propertiesFilePath) throws SSOAgentException{
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFilePath));
            initConfig(properties);
        } catch (FileNotFoundException e) {
            throw new SSOAgentException("Agent properties file not found at " + propertiesFilePath);
        } catch (IOException e) {
            throw new SSOAgentException("Error reading Agent properties file at " + propertiesFilePath);
        }
    }

    public static void initConfig(Properties properties) throws SSOAgentException{

        if(properties.getProperty("EnableSAMLSSOLogin") != null){
            samlSSOLoginEnabled = Boolean.parseBoolean(properties.getProperty("EnableSAMLSSOLogin"));
        } else {
            LOGGER.info("\'EnableSAMLSSOLogin\' not configured. Defaulting to \'true\'");
            samlSSOLoginEnabled = true;
        }

        if(properties.getProperty("EnableOpenIDLogin") != null){
            openidLoginEnabled = Boolean.parseBoolean(properties.getProperty("EnableOpenIDLogin"));
        } else {
            LOGGER.info("\'EnableOpenIDLogin\' not configured. Defaulting to \'true\'");
            openidLoginEnabled = true;
        }

        loginUrl = properties.getProperty("LoginUrl");
        samlSSOUrl = properties.getProperty("SAMLSSOUrl");
        openIdUrl = properties.getProperty("OpenIDUrl");

        issuerId = properties.getProperty("SAML.IssuerID");
        consumerUrl = properties.getProperty("SAML.ConsumerUrl");
        idPUrl = properties.getProperty("SAML.IdPUrl");
        subjectIdSessionAttributeName = properties.getProperty("SAML.SubjectIDSessionAttributeName");
        attributeConsumingServiceIndex = properties.getProperty("SAML.AttributeConsumingServiceIndex");
        samlSSOAttributesMapName = properties.getProperty("SAML.AttributesMapName");

        if(properties.getProperty("SAML.EnableSLO") != null){
            isSLOEnabled = Boolean.parseBoolean(properties.getProperty("SAML.EnableSLO"));
        } else {
            LOGGER.info("\'SAML.EnableSLO\' not configured. Defaulting to \'false\'");
            isSLOEnabled = false;
        }

        logoutUrl = properties.getProperty("SAML.LogoutUrl");

        if(properties.getProperty("SAML.EnableResponseSigning") != null){
            isResponseSigned = Boolean.parseBoolean(properties.getProperty("SAML.EnableResponseSigning"));
        } else {
            LOGGER.info("\'SAML.EnableResponseSigning\' not configured. Defaulting to \'false\'");
            isResponseSigned = false;
        }

        if(properties.getProperty("SAML.EnableAssertionSigning") != null){
            isAssertionSigned = Boolean.parseBoolean(properties.getProperty("SAML.EnableAssertionSigning"));
        } else {
            LOGGER.info("\'SAML.EnableAssertionSigning\' not configured. Defaulting to \'true\'");
            isAssertionSigned = true;
        }

        if(properties.getProperty("SAML.EnableRequestSigning") != null){
            isRequestSigned = Boolean.parseBoolean(properties.getProperty("SAML.EnableRequestSigning"));
        } else {
            LOGGER.info("\'SAML.EnableRequestSigning\' not configured. Defaulting to \'false\'");
            isRequestSigned = false;
        }

        ssoAgentCredentialImplClass = properties.getProperty("SAML.SSOAgentCredentialImplClass");
        if(properties.getProperty("SAML.KeyStore") != null){
            try {
                keyStore = new FileInputStream(properties.getProperty("SAML.KeyStore"));
            } catch (FileNotFoundException e) {
                throw new SSOAgentException("Cannot find file " + properties.getProperty("SAML.KeyStore"));
            }
        }
        keyStorePassword = properties.getProperty("SAML.KeyStorePassword");
        idPCertAlias = properties.getProperty("SAML.IdPCertAlias");
        privateKeyPassword = properties.getProperty("SAML.PrivateKeyPassword");

        returnTo = properties.getProperty("OpenID.ReturnToUrl");
        claimedIdParameterName = properties.getProperty("OpenID.ClaimedIDParameterName");
        claimedIdSessionAttributeName = properties.getProperty("OpenID.ClaimedIDSessionAttributeName");
        discoverySessionAttributeName = properties.getProperty("OpenID.DiscoverySessionAttributeName");
        attributesRequestorImplClass = properties.getProperty("OpenID.AttributesRequestorImplClass");
        openIdAttributesMapName = properties.getProperty("OpenID.AttributesMapName");
    }

    public static void initCheck() throws SSOAgentException{

        if((SSOAgentConfigs.isSAMLSSOLoginEnabled() || SSOAgentConfigs.isOpenIDLoginEnabled()) &&
                SSOAgentConfigs.getLoginUrl() == null){
            throw new SSOAgentException("\'LoginUrl\' not configured");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.getSAMLSSOUrl() == null){
            throw new SSOAgentException("\'SAMLSSOUrl\' not configured");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.getIssuerId() == null){
            throw new SSOAgentException("\'SAML.IssuerId\' not configured");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.getConsumerUrl() == null){
            throw new SSOAgentException("\'SAML.ConsumerUrl\' not configured");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.getIdPUrl() == null){
            throw new SSOAgentException("\'SAML.IdPUrl\' not configured");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.getSubjectIdSessionAttributeName() == null){
            LOGGER.info("\'SAML.SubjectIDSessionAttributeName\' not configured. Defaulting to \'Subject\'");
            SSOAgentConfigs.setSubjectIdSessionAttributeName("Subject");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.getAttributeConsumingServiceIndex() == null){
            LOGGER.info("\'SAML.AttributeConsumingServiceIndex\' not configured. " +
                    "No attributes of the Subject will be requested");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.getAttributeConsumingServiceIndex() != null &&
                SSOAgentConfigs.getSAMLSSOAttributesMapName() == null){
            LOGGER.info("SAML.AttributesMapName not configured. Defaulting to \'SubjectAttributes\'");
            SSOAgentConfigs.setSAMLSSOAttributesMapName("SubjectAttributes");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.isSLOEnabled() &&
                SSOAgentConfigs.getLogoutUrl() == null){
            throw new SSOAgentException("Single Logout enabled, but SAML.LogoutUrl not configured");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() &&
                (SSOAgentConfigs.isResponseSigned() || SSOAgentConfigs.isAssertionSigned()|| SSOAgentConfigs.isRequestSigned()) &&
                SSOAgentConfigs.getSSOAgentCredentialImplClass() == null){
            LOGGER.info("SAML.SSOAgentCredentialImplClass not configured." +
                    " Defaulting to \'org.wso2.carbon.identity.sso.agent.saml.SSOAgentKeyStoreCredential\'");
            SSOAgentConfigs.setSSOAgentCredentialImplClass("org.wso2.carbon.identity.sso.agent.saml.SSOAgentKeyStoreCredential");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() &&
                (SSOAgentConfigs.isResponseSigned() || SSOAgentConfigs.isAssertionSigned() || SSOAgentConfigs.isRequestSigned()) &&
                SSOAgentConfigs.getSSOAgentCredentialImplClass() != null && SSOAgentConfigs.getKeyStore() == null){
            throw new SSOAgentException("SAML.KeyStore not configured");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() &&
                (SSOAgentConfigs.isResponseSigned() || SSOAgentConfigs.isAssertionSigned() || SSOAgentConfigs.isRequestSigned()) &&
                SSOAgentConfigs.getSSOAgentCredentialImplClass() != null && SSOAgentConfigs.getKeyStore() != null &&
                SSOAgentConfigs.getKeyStorePassword() == null){
            LOGGER.info("SAML.KeyStorePassword not configured." +
                    " Defaulting to \'wso2carbon\'");
            SSOAgentConfigs.setKeyStorePassword("wso2carbon");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() &&
                (SSOAgentConfigs.isResponseSigned() || SSOAgentConfigs.isAssertionSigned()) &&
                SSOAgentConfigs.getSSOAgentCredentialImplClass() != null && SSOAgentConfigs.getIdPCertAlias() == null){
            throw new SSOAgentException("\'SAML.IdPCertAlias\' not configured." +
                    " Defaulting to \'wso2carbon\'");
        }

        if(SSOAgentConfigs.isSAMLSSOLoginEnabled() && SSOAgentConfigs.isRequestSigned() &&
                SSOAgentConfigs.getSSOAgentCredentialImplClass() != null && SSOAgentConfigs.getPrivateKeyPassword() == null){
            LOGGER.info("SAML.PrivateKeyPassword not configured. Defaulting to \'wso2carbon\'");
            SSOAgentConfigs.setPrivateKeyPassword("wso2carbon");
        }

        if(SSOAgentConfigs.isOpenIDLoginEnabled() && SSOAgentConfigs.getOpenIdUrl() == null){
            throw new SSOAgentException("\'OpenIDUrl\' not configured");
        }

        if(SSOAgentConfigs.isOpenIDLoginEnabled() && SSOAgentConfigs.getReturnTo() == null){
            throw new SSOAgentException("OpenID.ReturnToUrl not configured");
        }

        if(SSOAgentConfigs.isOpenIDLoginEnabled() && SSOAgentConfigs.getClaimedIdParameterName() == null){
            LOGGER.info("OpenID.ClaimIDParameterName not configured. Defaulting to \'claimed_id\'");
            SSOAgentConfigs.setClaimedIdParameterName("claimed_id");
        }

        if(SSOAgentConfigs.isOpenIDLoginEnabled() && SSOAgentConfigs.getClaimedIdSessionAttributeName() == null){
            LOGGER.info("OpenID.ClaimedIdSessionAttributeName not configured. Defaulting to \'ClaimedID\'");
            SSOAgentConfigs.setClaimedIdSessionAttributeName("ClaimedID");
        }

        if(SSOAgentConfigs.isOpenIDLoginEnabled() && SSOAgentConfigs.getDiscoverySessionAttributeName() == null){
            LOGGER.info("OpenID.DiscoverySessionAttributeName not configured. Defaulting to \'openid-disc\'");
            SSOAgentConfigs.setDiscoverySessionAttributeName("openid-disc");
        }

        if(SSOAgentConfigs.isOpenIDLoginEnabled() && SSOAgentConfigs.getAttributesRequestorImplClass() == null){
            LOGGER.info("OpenID.AttributesRequestorImplClass not configured. No attributes of the subject will be fetched");
        }

        if(SSOAgentConfigs.isOpenIDLoginEnabled() && SSOAgentConfigs.getOpenIdAttributesMapName() == null){
            LOGGER.info("OpenID.AttributesMapName not configured. Defaulting to OpenIDAttributes");
            SSOAgentConfigs.setOpenIdAttributesMapName("OpenIDAttributes");
        }

    }

    public static boolean isSAMLSSOLoginEnabled() {
        return samlSSOLoginEnabled;
    }

    public static boolean isOpenIDLoginEnabled() {
        return openidLoginEnabled;
    }

    public static String getLoginUrl() {
        return loginUrl;
    }

    public static String getSAMLSSOUrl() {
        return samlSSOUrl;
    }

    public static String getOpenIdUrl() {
        return openIdUrl;
    }

    public static String getIssuerId() {
        return issuerId;
    }

    public static String getConsumerUrl() {
        return consumerUrl;
    }

    public static String getIdPUrl() {
        return idPUrl;
    }

    public static String getSubjectIdSessionAttributeName() {
        return subjectIdSessionAttributeName;
    }

    public static String getAttributeConsumingServiceIndex() {
        return attributeConsumingServiceIndex;
    }

    public static String getSAMLSSOAttributesMapName() {
        return samlSSOAttributesMapName;
    }

    public static boolean isSLOEnabled() {
        return isSLOEnabled;
    }

    public static String getLogoutUrl() {
        return logoutUrl;
    }

    public static boolean isResponseSigned() {
        return isResponseSigned;
    }

    public static boolean isAssertionSigned() {
        return isAssertionSigned;
    }

    public static boolean isRequestSigned() {
        return isRequestSigned;
    }

    public static String getSSOAgentCredentialImplClass() {
        return ssoAgentCredentialImplClass;
    }

    public static InputStream getKeyStore() {
        return keyStore;
    }

    public static String getKeyStorePassword() {
        return keyStorePassword;
    }

    public static String getIdPCertAlias() {
        return idPCertAlias;
    }

    public static String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public static String getReturnTo() {
        return returnTo;
    }

    public static String getClaimedIdParameterName() {
        return claimedIdParameterName;
    }

    public static String getClaimedIdSessionAttributeName() {
        return claimedIdSessionAttributeName;
    }

    public static String getDiscoverySessionAttributeName() {
        return discoverySessionAttributeName;
    }

    public static String getAttributesRequestorImplClass() {
        return attributesRequestorImplClass;
    }

    public static String getOpenIdAttributesMapName() {
        return openIdAttributesMapName;
    }

    public static void setSAMLSSOLoginEnabled(Boolean samlSSOLoginEnabled) {
        SSOAgentConfigs.samlSSOLoginEnabled = samlSSOLoginEnabled;
    }

    public static void setOpenidLoginEnabled(Boolean openidLoginEnabled) {
        SSOAgentConfigs.openidLoginEnabled = openidLoginEnabled;
    }

    public static void setLoginUrl(String loginUrl) {
        SSOAgentConfigs.loginUrl = loginUrl;
    }

    public static void setSAMLSSOUrl(String samlSSOUrl) {
        SSOAgentConfigs.samlSSOUrl = samlSSOUrl;
    }

    public static void setOpenIdUrl(String openIdUrl) {
        SSOAgentConfigs.openIdUrl = openIdUrl;
    }

    public static void setIssuerId(String issuerId) {
        SSOAgentConfigs.issuerId = issuerId;
    }

    public static void setConsumerUrl(String consumerUrl) {
        SSOAgentConfigs.consumerUrl = consumerUrl;
    }

    public static void setIdPUrl(String idPUrl) {
        SSOAgentConfigs.idPUrl = idPUrl;
    }

    public static void setSubjectIdSessionAttributeName(String subjectIdSessionAttributeName) {
        SSOAgentConfigs.subjectIdSessionAttributeName = subjectIdSessionAttributeName;
    }

    public static void setAttributeConsumingServiceIndex(String attributeConsumingServiceIndex) {
        SSOAgentConfigs.attributeConsumingServiceIndex = attributeConsumingServiceIndex;
    }

    public static void setSAMLSSOAttributesMapName(String samlSSOAttributesMapName) {
        SSOAgentConfigs.samlSSOAttributesMapName = samlSSOAttributesMapName;
    }

    public static void setSLOEnabled(Boolean SLOEnabled) {
        isSLOEnabled = SLOEnabled;
    }

    public static void setLogoutUrl(String logoutUrl) {
        SSOAgentConfigs.logoutUrl = logoutUrl;
    }

    public static void setResponseSigned(Boolean responseSigned) {
        isResponseSigned = responseSigned;
    }

    public static void setAssertionSigned(Boolean assertionSigned) {
        isAssertionSigned = assertionSigned;
    }

    public static void setRequestSigned(Boolean requestSigned) {
        isRequestSigned = requestSigned;
    }

    public static void setSSOAgentCredentialImplClass(String ssoAgentCredentialImplClass) {
        SSOAgentConfigs.ssoAgentCredentialImplClass = ssoAgentCredentialImplClass;
    }

    public static void setKeyStore(String keyStore) throws SSOAgentException{
        try {
            SSOAgentConfigs.keyStore = new FileInputStream(keyStore);
        } catch (FileNotFoundException e) {
            throw new SSOAgentException("Cannot find file " + keyStore);
        }
    }

    public static void setKeyStore(InputStream keyStore) {
        SSOAgentConfigs.keyStore = keyStore;
    }

    public static void setKeyStorePassword(String keyStorePassword) {
        SSOAgentConfigs.keyStorePassword = keyStorePassword;
    }

    public static void setIdPCertAlias(String idPCertAlias) {
        SSOAgentConfigs.idPCertAlias = idPCertAlias;
    }

    public static void setPrivateKeyPassword(String privateKeyPassword) {
        SSOAgentConfigs.privateKeyPassword = privateKeyPassword;
    }

    public static void setReturnTo(String returnTo) {
        SSOAgentConfigs.returnTo = returnTo;
    }

    public static void setClaimedIdParameterName(String claimedIdParameterName) {
        SSOAgentConfigs.claimedIdParameterName = claimedIdParameterName;
    }

    public static void setClaimedIdSessionAttributeName(String claimedIdSessionAttributeName) {
        SSOAgentConfigs.claimedIdSessionAttributeName = claimedIdSessionAttributeName;
    }

    public static void setDiscoverySessionAttributeName(String discoverySessionAttributeName) {
        SSOAgentConfigs.discoverySessionAttributeName = discoverySessionAttributeName;
    }

    public static void setAttributesRequestorImplClass(String attributesRequestorImplClass) {
        SSOAgentConfigs.attributesRequestorImplClass = attributesRequestorImplClass;
    }

    public static void setOpenIdAttributesMapName(String openIdAttributesMapName) {
        SSOAgentConfigs.openIdAttributesMapName = openIdAttributesMapName;
    }

}
