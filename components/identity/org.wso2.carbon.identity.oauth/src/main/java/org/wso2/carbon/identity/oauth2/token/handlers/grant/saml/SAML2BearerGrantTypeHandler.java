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

package org.wso2.carbon.identity.oauth2.token.handlers.grant.saml;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.credential.CredentialResolver;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCriteria;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AbstractAuthorizationGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Constants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.util.*;

/**
 * This implements SAML 2.0 Bearer Assertion Profile for OAuth 2.0 -
 * http://tools.ietf.org/html/draft-ietf-oauth-saml2-bearer-14.
 */
public class SAML2BearerGrantTypeHandler extends AbstractAuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(SAML2BearerGrantTypeHandler.class);

    SAMLSignatureProfileValidator profileValidator = null;

    public SAML2BearerGrantTypeHandler() throws IdentityOAuth2Exception {
        super();
        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            log.error(e.getMessage(),e);
            throw new IdentityOAuth2Exception("Error in bootstrapping the OpenSAML2 library");
        }
        profileValidator =  new SAMLSignatureProfileValidator();

    }

    /**
     * We're validating the SAML token that we receive from the request. Through the assertion parameter is the POST
     * request. A request format that we handle here looks like,
     * <p/>
     * POST /token.oauth2 HTTP/1.1
     * Host: as.example.com
     * Content-Type: application/x-www-form-urlencoded
     * <p/>
     * grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Asaml2-bearer&
     * assertion=PHNhbWxwOl...[omitted for brevity]...ZT4
     *
     * @param tokReqMsgCtx Token message request context
     * @return true if validation is successful, false otherwise
     * @throws IdentityOAuth2Exception
     */
    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        // Logging the SAML token
        if (log.isDebugEnabled()) {
            log.debug("Received SAML assertion : " +
                    new String(Base64.decodeBase64(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAssertion()))
            );
        }

        XMLObject samlObject = unmarshall(new String(Base64.decodeBase64(
                tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAssertion())));
        Assertion assertion = (Assertion) samlObject;

        if (assertion == null) {
            log.debug("Assertion is null, cannot continue");
            throw new IdentityOAuth2Exception("Assertion is null, cannot continue");
        }

        /**
         * Validating SAML request according to criteria specified in "SAML 2.0 Bearer Assertion Profiles for
         * OAuth 2.0 - http://tools.ietf.org/html/draft-ietf-oauth-saml2-bearer-14
         */

        /**
         * The Assertion's <Issuer> element MUST contain a unique identifier for the entity that issued
         * the Assertion.
         */
        if (assertion.getIssuer() == null || assertion.getIssuer().getValue().equals("")) {
            log.debug("Issuer is empty in the SAML assertion");
            throw new IdentityOAuth2Exception("Issuer is empty in the SAML assertion");
        } else if (!OAuthServerConfiguration.getInstance().getSAML2Issuers().containsKey(assertion.getIssuer().getValue())){
            log.debug("SAML2 Issuers not registered");
            throw new IdentityOAuth2Exception("SAML2 Issuers not registered");
        }

        /**
         * The Assertion MUST contain <Conditions> element with an <AudienceRestriction> element with an <Audience>
         * element containing a URI reference that identifies the authorization server, or the service provider
         * SAML entity of its controlling domain, as an intended audience.  The token endpoint URL of the
         * authorization server MAY be used as an acceptable value for an <Audience> element.  The authorization
         * server MUST verify that it is an intended audience for the Assertion.
         */
        boolean isAudienceValid = false;
        Conditions conditions = assertion.getConditions();
        if (conditions != null) {
            List<AudienceRestriction> restrictions = conditions.getAudienceRestrictions();
            if (restrictions != null && restrictions.size() > 0) {
                for(AudienceRestriction audienceRestriction:restrictions){
                    List<Audience> audiences = audienceRestriction.getAudiences();
                    for (Audience audience : audiences) {
                        String audienceURI = audience.getAudienceURI();
                        if(OAuthServerConfiguration.getInstance().getSAML2Audience().contains(audienceURI) ||
                                OAuthServerConfiguration.getInstance().getTokenEndPoint().equals(audienceURI) ||
                                OAuthServerConfiguration.getInstance().getTokenEndPointAliases().contains(audienceURI)){
                            isAudienceValid = true;
                            break;
                        }
                    }
                }
                if(!isAudienceValid){
                    log.debug("Valid Audience not found in SAML2 token");
                    throw new IdentityOAuth2Exception("Valid Audience not found in SAML2 token");
                }
            } else {
                log.debug("Cannot find any AudienceRestrictions in the Assertion");
                throw new IdentityOAuth2Exception("Cannot find any AudienceRestrictions in the Assertion");
            }
        } else {
            log.debug("Cannot find any Conditions in the Assertion");
            throw new IdentityOAuth2Exception("Cannot find any Conditions in the Assertion");
        }

        /**
         * The Assertion MUST contain a <Subject> element.  The subject MAY identify the resource owner for whom
         * the access token is being requested.  For client authentication, the Subject MUST be the "client_id"
         * of the OAuth client.  When using an Assertion as an authorization grant, the Subject SHOULD identify
         * an authorized accessor for whom the access token is being requested (typically the resource owner, or
         * an authorized delegate).  Additional information identifying the subject/principal of the transaction
         * MAY be included in an <AttributeStatement>.
         */
        if (assertion.getSubject() != null) {
            String resourceOwnerUserName = assertion.getSubject().getNameID().getValue();
            if (resourceOwnerUserName == null || resourceOwnerUserName.equals("")) {
                log.debug("NameID in Assertion cannot be empty");
                throw new IdentityOAuth2Exception("NameID in Assertion cannot be empty");
            }
            tokReqMsgCtx.setAuthorizedUser(resourceOwnerUserName);
        } else {
            log.debug("Cannot find a Subject in the Assertion");
            throw new IdentityOAuth2Exception("Cannot find a Subject in the Assertion");
        }

        /**
         * The Assertion MUST have an expiry that limits the time window during which it can be used.  The expiry
         * can be expressed either as the NotOnOrAfter attribute of the <Conditions> element or as the NotOnOrAfter
         * attribute of a suitable <SubjectConfirmationData> element.
         */

        /**
         * The <Subject> element MUST contain at least one <SubjectConfirmation> element that allows the
         * authorization server to confirm it as a Bearer Assertion.  Such a <SubjectConfirmation> element MUST
         * have a Method attribute with a value of "urn:oasis:names:tc:SAML:2.0:cm:bearer".  The
         * <SubjectConfirmation> element MUST contain a <SubjectConfirmationData> element, unless the Assertion
         * has a suitable NotOnOrAfter attribute on the <Conditions> element, in which case the
         * <SubjectConfirmationData> element MAY be omitted. When present, the <SubjectConfirmationData> element
         * MUST have a Recipient attribute with a value indicating the token endpoint URL of the authorization
         * server (or an acceptable alias).  The authorization server MUST verify that the value of the Recipient
         * attribute matches the token endpoint URL (or an acceptable alias) to which the Assertion was delivered.
         * The <SubjectConfirmationData> element MUST have a NotOnOrAfter attribute that limits the window during
         * which the Assertion can be confirmed.  The <SubjectConfirmationData> element MAY also contain an Address
         * attribute limiting the client address from which the Assertion can be delivered.  Verification of the
         * Address is at the discretion of the authorization server.
         */

        DateTime notOnOrAfterFromConditions = null;
        Map<String,DateTime> notOnOrAfterFromSubjectConfirmations = new HashMap<String,DateTime>();
        boolean bearerFound = false;
        ArrayList<String> recipientURLS = new ArrayList<String>();

        if (assertion.getConditions().getNotOnOrAfter() != null) {
            notOnOrAfterFromConditions = assertion.getConditions().getNotOnOrAfter();
        }

        List<SubjectConfirmation> subjectConfirmations = assertion.getSubject().getSubjectConfirmations();
        if (subjectConfirmations != null && !subjectConfirmations.isEmpty()) {
            for (SubjectConfirmation s : subjectConfirmations) {
                if(s.getMethod() != null){
                    if (s.getMethod().equals(OAuth2Constants.OAUTH_SAML2_BEARER_METHOD)) {
                        bearerFound = true;
                    }
                } else {
                    log.debug("Cannot find Method attribute in SubjectConfirmation " + s.toString());
                    throw new IdentityOAuth2Exception("Cannot find Method attribute in SubjectConfirmation " + s.toString());
                }

                if(s.getSubjectConfirmationData() != null) {
                    if(s.getSubjectConfirmationData().getRecipient() != null){
                        recipientURLS.add(s.getSubjectConfirmationData().getRecipient());
                    } else {
                        log.debug("Cannot find Recipient attribute in SubjectConfirmationData " + s.getSubjectConfirmationData());
                        throw new IdentityOAuth2Exception("Cannot find Recipient attribute in SubjectConfirmationData " + s.getSubjectConfirmationData());
                    }
                    if(s.getSubjectConfirmationData().getNotOnOrAfter() != null){
                        notOnOrAfterFromSubjectConfirmations.put(s.getMethod(),s.getSubjectConfirmationData().getNotOnOrAfter());
                    } else {
                        log.debug("Cannot find NotOnOrAfter attribute in SubjectConfirmationData " +
                                s.getSubjectConfirmationData().toString());
                        throw new IdentityOAuth2Exception("Cannot find NotOnOrAfter attribute in SubjectConfirmationData " +
                                s.getSubjectConfirmationData().toString());
                    }
                } else if (s.getSubjectConfirmationData() == null && notOnOrAfterFromConditions == null) {
                    log.debug("Neither can find NotOnOrAfter attribute in Conditions nor SubjectConfirmationData" +
                            "in SubjectConfirmation " + s.toString());
                    throw new IdentityOAuth2Exception("Neither can find NotOnOrAfter attribute in Conditions nor" +
                            "SubjectConfirmationData in SubjectConfirmation " + s.toString());
                }
            }
        } else {
            log.debug("No SubjectConfirmation exist in Assertion");
            throw new IdentityOAuth2Exception("No SubjectConfirmation exist in Assertion");
        }

        if (!bearerFound) {
            log.debug("Failed to find a SubjectConfirmation with a Method attribute having : " +
                    OAuth2Constants.OAUTH_SAML2_BEARER_METHOD);
            throw new IdentityOAuth2Exception("Failed to find a SubjectConfirmation with a Method attribute having : " +
                    OAuth2Constants.OAUTH_SAML2_BEARER_METHOD);
        }

        if(recipientURLS.size() > 0){
            if(!recipientURLS.contains(OAuthServerConfiguration.getInstance().getTokenEndPoint())){
                boolean isAliasFound = false;
                for(String alias : OAuthServerConfiguration.getInstance().getTokenEndPointAliases()){
                    if(recipientURLS.contains(alias)){
                        isAliasFound = true;
                        break;
                    }
                }
                if(!isAliasFound){
                    log.debug("None of the recipient URLs match the token endpoint or an acceptable alias");
                    throw new IdentityOAuth2Exception("None of the recipient URLs match the token endpoint or an acceptable alias");
                }
            }
        }


        /**
         * The authorization server MUST verify that the NotOnOrAfter instant has not passed, subject to allowable
         * clock skew between systems.  An invalid NotOnOrAfter instant on the <Conditions> element invalidates
         * the entire Assertion.  An invalid NotOnOrAfter instant on a <SubjectConfirmationData> element only
         * invalidates the individual <SubjectConfirmation>.  The authorization server MAY reject Assertions with
         * a NotOnOrAfter instant that is unreasonably far in the future.  The authorization server MAY ensure
         * that Bearer Assertions are not replayed, by maintaining the set of used ID values for the length of
         * time for which the Assertion would be considered valid based on the applicable NotOnOrAfter instant.
         */
        if (notOnOrAfterFromConditions.compareTo(new DateTime()) < 1) {
            // notOnOrAfter is an expired timestamp
            log.debug("NotOnOrAfter is having an expired timestamp in Conditions element");
            throw new IdentityOAuth2Exception("NotOnOrAfter is having an expired timestamp in Conditions element");
        }
        Map<String,DateTime> validSubjectConfirmations = new HashMap<String,DateTime>();
        if(!notOnOrAfterFromSubjectConfirmations.isEmpty()){
            Set<String> confirmationMethods = notOnOrAfterFromSubjectConfirmations.keySet();
            for(String confirmationMethod : confirmationMethods){
                DateTime value = notOnOrAfterFromSubjectConfirmations.get(confirmationMethod);
                if(value.compareTo(new DateTime()) >= 1){
                    validSubjectConfirmations.put(confirmationMethod,value);
                }
            }
        }
        if(notOnOrAfterFromConditions == null && validSubjectConfirmations.isEmpty()){
            log.debug("No valid NotOnOrAfter element found in SubjectConfirmations");
            throw new IdentityOAuth2Exception("No valid NotOnOrAfter element found in SubjectConfirmations");
        }

        /**
         * The Assertion MUST be digitally signed by the issuer and the authorization server MUST verify the
         * signature.
         */

        try {
            profileValidator.validate(assertion.getSignature());
        } catch (ValidationException e) {
            // Indicates signature did not conform to SAML Signature profile
            log.debug(e.getMessage());
            throw new IdentityOAuth2Exception("Signature element does not conform to SAML Signature Profile");
        }

        String userName = tokReqMsgCtx.getAuthorizedUser();
        int tenantID = getTenantId(userName);
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantID);
        KeyStore keyStore;

        if(!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)){
            // derive key store name
            String ksName = tenantDomain.trim().replace(".", "-");
            // derive JKS name
            String jksName = ksName + ".jks";
            try {
                keyStore = tenantKSM.getKeyStore(jksName);
            } catch (Exception e) {
                log.error(e);
                throw new IdentityOAuth2Exception("Error retrieving key store: " + jksName);
            }
        }else{
            try {
                keyStore = tenantKSM.getPrimaryKeyStore();
            } catch (Exception e) {
                log.error(e);
                throw new IdentityOAuth2Exception("Error retrieving primary key store");
            }
        }

        CredentialResolver resolver = new CarbonKeyStoreCredentialResolver(keyStore, new HashMap<String, String>());
        SignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(resolver,
                Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver());
        CriteriaSet criteriaSet = new CriteriaSet();
        if(OAuthServerConfiguration.getInstance().getSAML2Issuers().get(assertion.getIssuer().getValue()) != null){
            criteriaSet.add(new EntityIDCriteria(OAuthServerConfiguration.getInstance().getSAML2Issuers().
                    get(assertion.getIssuer().getValue())));
        }
        criteriaSet.add(new KeyInfoCriteria(assertion.getSignature().getKeyInfo()));
        try {
            if (!trustEngine.validate(assertion.getSignature(), criteriaSet)) {
                log.debug("Signature was either invalid or signing key could not be established as trusted");
                throw new IdentityOAuth2Exception("Signature was either invalid or signing key could not be established as trusted");
            }
        } catch (org.opensaml.xml.security.SecurityException e) {
            log.error(e);
            throw new IdentityOAuth2Exception("Error occurred while validating signature");
        }


        /**
         * The authorization server MUST verify that the Assertion is valid in all other respects per
         * [OASIS.saml-core-2.0-os], such as (but not limited to) evaluating all content within the Conditions
         * element including the NotOnOrAfter and NotBefore attributes, rejecting unknown condition types, etc.
         *
         * [OASIS.saml-core-2.0-os] - http://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf
         */

        // TODO: Throw the SAML request through the general SAML2 validation routines

        tokReqMsgCtx.setScope(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getScope());
        
        // Storing the Assertion. This will be used in OpenID Connect for example
        tokReqMsgCtx.addProperty(OAuth2Constants.OAUTH_SAML2_ASSERTION, assertion);
        
		// Invoking extension
		SAML2TokenCallbackHandler callback =
		                                     OAuthServerConfiguration.getInstance()
		                                                             .getSAML2TokenCallbackHandler();
		if (callback != null) {
			log.debug("Invoking the SAML2 Token callback handler ");
			callback.handleSAML2Token(tokReqMsgCtx);
		}
        
        return true;
    }

    /**
     * Constructing the SAML or XACML Objects from a String
     * @param xmlString Decoded SAML or XACML String
     * @return SAML or XACML Object
     * @throws org.wso2.carbon.identity.base.IdentityException
     *
     */
    private XMLObject unmarshall(String xmlString) throws IdentityOAuth2Exception{
        Unmarshaller unmarshaller;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new ByteArrayInputStream(xmlString.trim().getBytes()));
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (Exception e) {
            log.error("Error in constructing XML Object from the encoded String", e);
            throw new IdentityOAuth2Exception("Error in constructing XML Object from the encoded String", e);
        }
    }

    /**
     * Helper method to get tenantId from userName
     *
     * @param userName
     * @return tenantId
     * @throws IdentityOAuth2Exception
     */
    private int getTenantId(String userName) throws IdentityOAuth2Exception {
        //get tenant domain from user name
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        RealmService realmService = OAuthComponentServiceHolder.getRealmService();
        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            return tenantId;
        } catch (UserStoreException e) {
            String error = "Error in obtaining tenantId from Domain";
            //do not log
            throw new IdentityOAuth2Exception(error);
        }
    }

}
