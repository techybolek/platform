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
import java.util.logging.Logger;

public class SSOConfigs {

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
    private static String keyStore;
    private static String keyStorePassword;
    private static String idPCertAlias;
    private static String privateKeyPassword;

    private static String returnTo;
    private static String claimedIdParameterName;
    private static String claimedIdSessionAttributeName;
    private static String discoverySessionAttributeName;
    private static String attributesRequestorImplClass;
    private static String openIdAttributesMapName;


    public static void initConfigs(FilterConfig fConfigs) throws SSOAgentException {

        samlSSOLoginEnabled = Boolean.parseBoolean(fConfigs.getInitParameter("EnableSAMLSSOLogin"));
        openidLoginEnabled = Boolean.parseBoolean(fConfigs.getInitParameter("EnableOpenIDLogin"));
        loginUrl = fConfigs.getInitParameter("LoginUrl");
        samlSSOUrl = fConfigs.getInitParameter("SAMLSSOUrl");
        openIdUrl = fConfigs.getInitParameter("OpenIDUrl");

        issuerId = fConfigs.getInitParameter("SAML.IssuerID");
        consumerUrl = fConfigs.getInitParameter("SAML.ConsumerUrl");
		idPUrl = fConfigs.getInitParameter("SAML.IdPUrl");
        subjectIdSessionAttributeName = fConfigs.getInitParameter("SAML.SubjectIDSessionAttributeName");
        attributeConsumingServiceIndex = fConfigs.getInitParameter("SAML.AttributeConsumingServiceIndex");
        samlSSOAttributesMapName = fConfigs.getInitParameter("SAML.AttributesMapName");
        isSLOEnabled = Boolean.parseBoolean(fConfigs.getInitParameter("SAML.EnableSLO"));
        logoutUrl = fConfigs.getInitParameter("SAML.LogoutUrl");
        isResponseSigned = Boolean.parseBoolean(fConfigs.getInitParameter("SAML.EnableResponseSigning"));
        isAssertionSigned = Boolean.parseBoolean(fConfigs.getInitParameter("SAML.EnableAssertionSigning"));
        isRequestSigned = Boolean.parseBoolean(fConfigs.getInitParameter("SAML.EnableRequestSigning"));
        ssoAgentCredentialImplClass = fConfigs.getInitParameter("SAML.SSOAgentCredentialImplClass");
        keyStore = fConfigs.getInitParameter("SAML.KeyStore");
        keyStorePassword = fConfigs.getInitParameter("SAML.KeyStorePassword");
        idPCertAlias = fConfigs.getInitParameter("SAML.IdPCertAlias");
        privateKeyPassword = fConfigs.getInitParameter("SAML.PrivateKeyPassword");

        returnTo = fConfigs.getInitParameter("OpenID.ReturnToUrl");
        claimedIdParameterName = fConfigs.getInitParameter("OpenID.ClaimedIDParameterName");
        claimedIdSessionAttributeName = fConfigs.getInitParameter("OpenID.ClaimedIDSessionAttributeName");
        discoverySessionAttributeName = fConfigs.getInitParameter("OpenID.DiscoverySessionAttributeName");
        attributesRequestorImplClass = fConfigs.getInitParameter("OpenID.AttributesRequestorImplClass");
        openIdAttributesMapName = fConfigs.getInitParameter("OpenID.AttributesMapName");


        if(samlSSOLoginEnabled == null){
            throw new SSOAgentException("\'EnableSAMLSSOLogin\' not configured");
        }

        if(openidLoginEnabled == null){
            throw new SSOAgentException("\'EnableOpenIDLogin\' not configured");
        }

        if((samlSSOLoginEnabled || openidLoginEnabled) && loginUrl == null){
            throw new SSOAgentException("\'LoginUrl\' not configured");
        }

        if(samlSSOLoginEnabled && samlSSOUrl == null){
            throw new SSOAgentException("\'SAMLSSOUrl\' not configured");
        }

        if(samlSSOLoginEnabled && issuerId == null){
            throw new SSOAgentException("\'SAML.IssuerId\' not configured");
        }

        if(samlSSOLoginEnabled && consumerUrl == null){
            throw new SSOAgentException("\'SAML.ConsumerUrl\' not configured");
        }

        if(samlSSOLoginEnabled && idPUrl == null){
            throw new SSOAgentException("\'SAML.IdPUrl\' not configured");
        }

        if(samlSSOLoginEnabled && subjectIdSessionAttributeName == null){
            LOGGER.info("\'SAML.SubjectIDSessionAttributeName\' not configured. Defaulting to \'Subject\'");
            subjectIdSessionAttributeName = "Subject";
        }

        if(samlSSOLoginEnabled && attributeConsumingServiceIndex == null){
            LOGGER.info("\'SAML.AttributeConsumingServiceIndex\' not configured. No attributes of the Subject will be requested");
        }

        if(samlSSOLoginEnabled && attributeConsumingServiceIndex != null && samlSSOAttributesMapName == null){
            LOGGER.info("SAML.AttributesMapName not configured. Defaulting to \'SubjectAttributes\'");
            samlSSOAttributesMapName = "SubjectAttributes";
        }

        if(samlSSOLoginEnabled && isSLOEnabled == null){
            LOGGER.info("SAML.EnableSLO not configured. Defaulting to \'false\'");
            isSLOEnabled = false;
        }

        if(samlSSOLoginEnabled && isSLOEnabled && logoutUrl == null){
            throw new SSOAgentException("Single Logout enabled, but SAML.LogoutUrl not configured");
        }

        if(samlSSOLoginEnabled && isResponseSigned == null){
            LOGGER.info("SAML.EnableResponseSigning not configured. Defaulting to \'false\'");
            isResponseSigned = false;
        }

        if(samlSSOLoginEnabled && isAssertionSigned == null){
            LOGGER.info("SAML.EnableAssertionSigning not configured. Defaulting to \'false\'");
            isAssertionSigned = false;
        }

        if(samlSSOLoginEnabled && isRequestSigned == null){
            LOGGER.info("SAML.EnableRequestSigning not configured. Defaulting to \'false\'");
            isRequestSigned = false;
        }

        if(samlSSOLoginEnabled && (isResponseSigned || isAssertionSigned || isRequestSigned) &&
                ssoAgentCredentialImplClass == null){
            LOGGER.info("SAML.SSOAgentCredentialImplClass not configured." +
                    " Defaulting to \'org.wso2.carbon.identity.sso.agent.saml.SSOAgentKeyStoreCredential\'");
            ssoAgentCredentialImplClass = "org.wso2.carbon.identity.sso.agent.saml.SSOAgentKeyStoreCredential";
        }

        if(samlSSOLoginEnabled && (isResponseSigned || isAssertionSigned || isRequestSigned) &&
                ssoAgentCredentialImplClass == null && keyStore == null){
            throw new SSOAgentException("SAML.KeyStore not configured");
        }

        if(samlSSOLoginEnabled && (isResponseSigned || isAssertionSigned || isRequestSigned) &&
                ssoAgentCredentialImplClass == null && keyStore != null && keyStorePassword == null){
            LOGGER.info("SAML.KeyStorePassword not configured." +
                    " Defaulting to \'wso2carbon\'");
            keyStorePassword = "wso2carbon";
        }

        if(samlSSOLoginEnabled && (isResponseSigned || isAssertionSigned) &&
                ssoAgentCredentialImplClass == null && idPCertAlias == null){
            throw new SSOAgentException("\'SAML.IdPCertAlias\' not configured." +
                    " Defaulting to \'wso2carbon\'");
        }

        if(samlSSOLoginEnabled && isRequestSigned && privateKeyPassword == null && ssoAgentCredentialImplClass == null){
            LOGGER.info("SAML.PrivateKeyPassword not configured. Defaulting to \'wso2carbon\'");
            privateKeyPassword = "wso2carbon";
        }

        if(openidLoginEnabled && openIdUrl == null){
            throw new SSOAgentException("\'OpenIDUrl\' not configured");
        }

        if(openidLoginEnabled && returnTo == null){
            throw new SSOAgentException("OpenID.ReturnToUrl not configured");
        }

        if(openidLoginEnabled && claimedIdParameterName == null){
            LOGGER.info("OpenID.ClaimIDParameterName not configured. Defaulting to \'claimed_id\'");
            claimedIdParameterName = "claimed_id";
        }

        if(openidLoginEnabled && claimedIdSessionAttributeName == null){
            LOGGER.info("OpenID.ClaimedIdSessionAttributeName not configured. Defaulting to \'ClaimedID\'");
            claimedIdSessionAttributeName = "ClaimedID";
        }

        if(openidLoginEnabled && discoverySessionAttributeName == null){
            LOGGER.info("OpenID.DiscoverySessionAttributeName not configured. Defaulting to \'openid-disc\'");
            discoverySessionAttributeName = "openid-disc";
        }

        if(openidLoginEnabled && attributesRequestorImplClass == null){
            LOGGER.info("OpenID.AttributesRequestorImplClass not configured. No attributes of the subject will be fetched");
        }

        if(openidLoginEnabled && openIdAttributesMapName == null){
            LOGGER.info("OpenID.AttributesMapName not configured. Defaulting to OpenIDAttributes");
            openIdAttributesMapName = "OpenIDAttributes";
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

    public static String getSamlSSOAttributesMapName() {
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

    public static String getKeyStore() {
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

}
