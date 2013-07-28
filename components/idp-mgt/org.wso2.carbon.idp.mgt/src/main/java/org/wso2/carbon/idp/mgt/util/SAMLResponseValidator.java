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
package org.wso2.carbon.idp.mgt.util;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.idp.mgt.dto.TrustedIdPDTO;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class SAMLResponseValidator {

    private static final Log log = LogFactory.getLog(SAMLResponseValidator.class);

    private static boolean bootstrapped = false;

    public static boolean validateSAMLResponse(TrustedIdPDTO trustedIdPDTO, String samlResponseString, String[] audiences)
            throws IdentityProviderMgtException {

        try {
            if(!bootstrapped){
                DefaultBootstrap.bootstrap();
            }
        } catch (ConfigurationException e) {
            String msg = "Error bootstrapping OpenSAML library";
            log.error(msg, e);
            throw new IdentityProviderMgtException(msg);
        }

        Response samlResponse = (Response) unmarshall(new String(Base64.decode(samlResponseString)));
        List<Assertion> assertions = samlResponse.getAssertions();
        Assertion assertion = null;
        if (assertions != null && assertions.size() > 0) {
            assertion = assertions.get(0);
        }
        if (assertion == null) {
            if (samlResponse.getStatus() != null &&
                    samlResponse.getStatus().getStatusCode() != null &&
                    samlResponse.getStatus().getStatusCode().getValue().equals(IdentityProviderMgtConstants.StatusCodes.IDENTITY_PROVIDER_ERROR) &&
                    samlResponse.getStatus().getStatusCode().getStatusCode() != null &&
                    samlResponse.getStatus().getStatusCode().getStatusCode().getValue().equals(IdentityProviderMgtConstants.StatusCodes.NO_PASSIVE)) {
                return false;
            }
            throw new IdentityProviderMgtException("SAML Assertion not found in the Response");
        }

        // Validate Issuer Id
        if(!assertion.getIssuer().getValue().equals(trustedIdPDTO.getIdPIssuerId())){
            throw new IdentityProviderMgtException("Invalid IssuerId");
        }

        String subject = null;
        if(assertion.getSubject() != null && assertion.getSubject().getNameID() != null){
            subject = assertion.getSubject().getNameID().getValue();
        }

        if(subject == null){
            throw new IdentityProviderMgtException("SAML Response does not contain the name of the subject");
        }

        // validate audience restriction
        validateAudienceRestriction(assertion, trustedIdPDTO, audiences);

        // validate signature this SP only looking for assertion signature
        validateSignature(samlResponse, trustedIdPDTO);

        return true;
    }

    private static XMLObject unmarshall(String samlString) throws IdentityProviderMgtException {

        String decodedString = decodeHTMLCharacters(samlString);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            ByteArrayInputStream is = new ByteArrayInputStream(decodedString.getBytes());
            Document document = docBuilder.parse(is);
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (ParserConfigurationException e) {
            throw new IdentityProviderMgtException("Error in unmarshalling SAML Request from the encoded String", e);
        } catch (UnmarshallingException e) {
            throw new IdentityProviderMgtException("Error in unmarshalling SAML Request from the encoded String", e);
        } catch (SAXException e) {
            throw new IdentityProviderMgtException("Error in unmarshalling SAML Request from the encoded String", e);
        } catch (IOException e) {
            throw new IdentityProviderMgtException("Error in unmarshalling SAML Request from the encoded String", e);
        }

    }

    /**
     * Validate the AudienceRestriction of SAML2 Response
     *
     * @param assertion SAML2 Assertion
     * @return validity
     */
    private static void validateAudienceRestriction(Assertion assertion, TrustedIdPDTO trustedIdPDTO, String[] audiences)
            throws IdentityProviderMgtException {

        List<String> requestedAudiences;
        if(audiences != null && audiences.length > 0){
            requestedAudiences = new ArrayList<String>(Arrays.asList(audiences));
        } else {
            requestedAudiences = new ArrayList<String>();
        }
        for(String registeredAudience : trustedIdPDTO.getAudience()){
            requestedAudiences.add(registeredAudience);
        }

        if(requestedAudiences != null && requestedAudiences.size() > 0){
            for(String requestedAudience : requestedAudiences){
                Conditions conditions = assertion.getConditions();
                if (conditions != null) {
                    List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
                    if (audienceRestrictions != null && !audienceRestrictions.isEmpty()) {
                        boolean audienceFound = false;
                        for (AudienceRestriction audienceRestriction : audienceRestrictions) {
                            if (audienceRestriction.getAudiences() != null && audienceRestriction.getAudiences().size() > 0) {
                                for(Audience audience: audienceRestriction.getAudiences()){
                                    if(audience.getAudienceURI().equals(requestedAudience)){
                                        audienceFound = true;
                                        break;
                                    }
                                }
                            } else {
                                throw new IdentityProviderMgtException("SAML Response's AudienceRestriction doesn't contain Audiences");
                            }
                            if(audienceFound){
                                break;
                            }
                        }
                        if(!audienceFound){
                            throw new IdentityProviderMgtException("SAML Assertion Audience Restriction validation failed");
                        }
                    } else {
                        throw new IdentityProviderMgtException("SAML Response doesn't contain AudienceRestrictions");
                    }
                } else {
                    throw new IdentityProviderMgtException("SAML Response doesn't contain Conditions");
                }
            }
        }
    }

    /**
     * Validate the signature of a SAML2 Response and Assertion
     *
     * @param response   SAML2 Response
     * @return true, if signature is valid.
     */
    private static void validateSignature(Response response, TrustedIdPDTO trustedIdPDTO) throws IdentityProviderMgtException {

        X509Certificate cert = (X509Certificate)IdentityProviderMgtUtil.getCertificate(trustedIdPDTO.getPublicCert());
        Credential credential = new X509CredentialImpl(cert);

        List<Assertion> assertions = response.getAssertions();
        Assertion assertion = null;
        if (assertions != null && assertions.size() > 0) {
            assertion = assertions.get(0);
        }

        if(assertion.getSignature() == null){
            throw new IdentityProviderMgtException("SAMLAssertion signing is enabled, but signature element not found in SAML Assertion element.");
        } else {
            try {
                SignatureValidator validator = new SignatureValidator(credential);
                validator.validate(assertion.getSignature());
            }  catch (ValidationException e) {
                throw new IdentityProviderMgtException("Signature validation failed for SAML Assertion");
            }
        }
    }

    private static String decodeHTMLCharacters(String encodedStr) {
        return encodedStr.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"").replaceAll("&apos;", "'");

    }

}
