/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.oauth.OAuthConstants;
import org.wso2.carbon.identity.oauth.preprocessor.TokenPersistencePreprocessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.validators.OAuth2TokenValidator;
import org.wso2.carbon.identity.oauth2.validators.TokenValidationHandler;
import org.wso2.carbon.identity.openidconnect.IDTokenBuilder;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.concurrent.ConcurrentHashMap;
/**
 * Runtime representation of the OAuth Configuration as configured through
 * identity.xml
 */
public class OAuthServerConfiguration {

	private static Log log = LogFactory.getLog(OAuthServerConfiguration.class);

	private static final String CONFIG_ELEM_OAUTH = "OAuth";

	/**
	 * Localpart names for the OAuth configuration in identity.xml.
	 */
	private class ConfigElements {
		// Callback handler related configuration elements
		private static final String OAUTH_CALLBACK_HANDLERS = "OAuthCallbackHandlers";
		private static final String OAUTH_CALLBACK_HANDLER = "OAuthCallbackHandler";
		private static final String CLAIM_URI = "ClaimUri";
		private static final String REQUIRED_CLAIM_URIS = "RequiredRespHeaderClaimUris";
		private static final String CALLBACK_CLASS = "Class";
		private static final String CALLBACK_PRIORITY = "Priority";
		private static final String CALLBACK_PROPERTIES = "Properties";
		private static final String CALLBACK_PROPERTY = "Property";
		private static final String CALLBACK_ATTR_NAME = "Name";
		private static final String TOKEN_VALIDATORS = "TokenValidators";
		private static final String TOKEN_VALIDATOR = "TokenValidator";
		private static final String TOKEN_TYPE_ATTR = "type";
		private static final String TOKEN_CLASS_ATTR = "class";

		// Default timestamp skew
		public static final String TIMESTAMP_SKEW = "TimestampSkew";

		// Default validity periods
		private static final String AUTHORIZATION_CODE_DEFAULT_VALIDITY_PERIOD =
		                                                                         "AuthorizationCodeDefaultValidityPeriod";

		private static final String CUSTOM_LOGIN_PAGE_URL = "CustomLoginPageUrl";

		private static final String CUSTOM_ERROR_PAGE_URL = "CustomErrorPageUrl";
		
		private static final String CUSTOM_CONSENT_PAGE_URL = "CustomConsentPageUrl";

		private static final String ACCESS_TOKEN_DEFAULT_VALIDITY_PERIOD = "AccessTokenDefaultValidityPeriod";
		
		private static final String APPLICATION_ACCESS_TOKEN_VALIDATION_PERIOD =
                "ApplicationAccessTokenDefaultValidityPeriod";
				
		// Enable/Disable cache
		public static final String ENABLE_CACHE = "EnableOAuthCache";

		// TokenStoragePreprocessor
		public static final String TOKEN_PERSISTENCE_PREPROCESSOR = "TokenPersistencePreprocessor";

		// Supported Grant Types
		public static final String SUPPORTED_GRANT_TYPES = "SupportedGrantTypes";

		// Supported Response Types
		public static final String SUPPORTED_RESP_TYPES = "SupportedResponseTypes";

		// OpenID Connect Configuration
		public static final String SUPPORTED_CLAIMS = "OpenIDConnectClaims";

		// Supported Client Authentication Methods
		public static final String SUPPORTED_CLIENT_AUTH_METHODS = "SupportedClientAuthMethods";

		public static final String SAML2_GRANT = "SAML2Grant";
		public static final String ISSUERS = "Issuers";
		public static final String ISSUER = "Issuer";
		public static final String AUDIENCE = "Audience";
		public static final String TOKEN_END_POINT = "TokenEndPoint";
		public static final String TOKEN_END_POINT_ALIASES = "TokenEndPointAliases";

		// JWT Generator
		public static final String AUTHORIZATION_CONTEXT_TOKEN_GENERATION =
		                                                                    "AuthorizationContextTokenGeneration";
		public static final String ENABLED = "Enabled";
		public static final String TOKEN_GENERATOR_IMPL_CLASS = "TokenGeneratorImplClass";
		public static final String CLAIMS_RETRIEVER_IMPL_CLASS = "ClaimsRetrieverImplClass";
		public static final String CONSUMER_DIALECT_URI = "ConsumerDialectURI";
		public static final String SIGNATURE_ALGORITHM = "SignatureAlgorithm";
		public static final String SECURITY_CONTEXT_TTL = "AuthorizationContextTTL";

		public static final String ENABLE_ASSERTIONS = "EnableAssertions";
		public static final String ENABLE_ASSERTIONS_USERNAME = "UserName";
		public static final String ENABLE_ACCESS_TOKEN_PARTITIONING = "EnableAccessTokenPartitioning";
		public static final String ACCESS_TOKEN_PARTITIONING_DOMAINS = "AccessTokenPartitioningDomains";

		// OpenIDConnect confiurations
		public static final String OPENID_CONNECT = "OpenIDConnect";
		public static final String OPENID_CONNECT_IDTOKEN_BUILDER = "IDTokenBuilder";
		public static final String OPENID_CONNECT_IDTOKEN_ISSUER_ID = "IDTokenIssuerID";
		public static final String OPENID_CONNECT_IDTOKEN_EXPIRATION = "IDTokenExpiration";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_DIALECT =
		                                                                            "UserInfoEndpointClaimDialect";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_RETRIEVER =
		                                                                              "UserInfoEndpointClaimRetriever";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_REQUEST_VALIDATOR = "UserInfoEndpointRequestValidator";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_ACCESS_TOKEN_VALIDATOR = "UserInfoEndpointAccessTokenValidator";
		public static final String OPENID_CONNECT_USERINFO_ENDPOINT_RESPONSE_BUILDER = "UserInfoEndpointResponseBuilder";
		
		// Primary/Secondary Login conifguration
		private static final String LOGIN_CONFIG = "LoginConfig";
		private static final String USERID_LOGIN = "UserIdLogin";
		private static final String EMAIL_LOGIN = "EmailLogin";
		private static final String PRIMARY_LOGIN = "primary";

	}

	private static OAuthServerConfiguration instance;

	private String loginPageUrl = null;

	private String errorPageUrl = null;
	
	private String consentPageUrl = null;

	private long defaultAuthorizationCodeValidityPeriodInSeconds = 300;

	private long defaultAccessTokenValidityPeriodInSeconds = 3600;

	private long defaultApplicationAccessTokenValidityPeriodInSeconds = 3600;
	
	private long defaultTimeStampSkewInSeconds = 300;

	private boolean cacheEnabled = true;

	private boolean assertionsUserNameEnabled = false;

	private boolean accessTokenPartitioningEnabled = false;

	private String accessTokenPartitioningDomains = null;

	private String tokenPersistencePreProcessorClassName =
	                                                       "org.wso2.carbon.identity.oauth.preprocessor.PlainTokenPersistencePreprocessor";

	private TokenPersistencePreprocessor tokenPersistencePreprocessor = null;

	private Set<OAuthCallbackHandlerMetaData> callbackHandlerMetaData =
	                                                                    new HashSet<OAuthCallbackHandlerMetaData>();

	private List<String> supportedGrantTypes = new ArrayList<String>();

	private List<String> supportedResponseTypes = new ArrayList<String>();

	private String[] supportedClaims = null;

	private List<String> supportedClientAuthMethods = new ArrayList<String>();

	private Map<String, String> saml2Issuers = new HashMap<String, String>();

	private List<String> saml2Audience = new ArrayList<String>();
	
	private Map<String,Map<String,String>> loginConfiguration = new ConcurrentHashMap<String, Map<String,String>>();

	private String tokenEP = null;

	private List<String> tokenEPAliases = new ArrayList<String>();

	private List<String> requiredHeaderClaimUris = new ArrayList<String>();

	private boolean isAuthContextTokGenEnabled = false;

	private String tokenGeneratorImplClass = "org.wso2.carbon.identity.oauth2.token.JWTTokenGenerator";

	private String claimsRetrieverImplClass = "org.wso2.carbon.identity.oauth2.token.DefaultClaimsRetriever";

	private String consumerDialectURI = "http://wso2.org/claims";

	private String signatureAlgorithm = "SHA256withRSA";

	private String authContextTTL = "15L";

	// OpenID Connect configurations
	private String openIDConnectIDTokenBuilderClassName = "org.wso2.carbon.identity.openidconnect.DefaultIDTokenBuilder";
	
	private IDTokenBuilder openIDConnectIDTokenBuilder = null;
	
	private String openIDConnectIDTokenIssuer = "OIDCAuthzServer";

	private String openIDConnectIDTokenExpiration = "300";

	private String openIDConnectUserInfoEndpointClaimDialect = "http://wso2.org/claims";

	private String openIDConnectUserInfoEndpointClaimRetriever = "org.wso2.carbon.identity.oauth.endpoint.user.impl.UserInfoUserStoreClaimRetriever";
	
	private String openIDConnectUserInfoEndpointRequestValidator = "org.wso2.carbon.identity.oauth.endpoint.user.impl.UserInforRequestDefaultValidator";
	
	private String openIDConnectUserInfoEndpointAccessTokenValidator = "org.wso2.carbon.identity.oauth.endpoint.user.impl.UserInfoISAccessTokenValidator";
	
	private String openIDConnectUserInfoEndpointResponseBuilder = "org.wso2.carbon.identity.oauth.endpoint.user.impl.UserInfoJSONResponseBuilder";

	private OAuthServerConfiguration() {
		buildOAuthServerConfiguration();
	}

	public static OAuthServerConfiguration getInstance() {
		CarbonUtils.checkSecurity();
		if (instance == null) {
			synchronized (OAuthServerConfiguration.class) {
				if (instance == null) {
					instance = new OAuthServerConfiguration();
				}
			}
		}
		return instance;
	}

	private void buildOAuthServerConfiguration() {
		try {

			IdentityConfigParser configParser = IdentityConfigParser.getInstance();
			OMElement oauthElem = configParser.getConfigElement(CONFIG_ELEM_OAUTH);

			if (oauthElem == null) {
				warnOnFaultyConfiguration("OAuth element is not available.");
				return;
			}

			// read callback handler configurations
			parseOAuthCallbackHandlers(oauthElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OAUTH_CALLBACK_HANDLERS)));

			// get the required claim uris - that needs to be included in the
			// response.
			parseRequiredHeaderClaimUris(oauthElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.REQUIRED_CLAIM_URIS)));

			// get the token validators by type
			parseTokenValidators(oauthElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_VALIDATORS)));

			// read default timeout periods
			parseDefaultValidityPeriods(oauthElem);

			// read caching configurations
			parseCachingConfiguration(oauthElem);

			// read token preprocessor config
			parseTokenPersistencePreProcessorConfig(oauthElem);

			// read supported grant types
			parseSupportedGrantTypesConfig(oauthElem);

			// read supported response types
			parseSupportedResponseTypesConfig(oauthElem);

			// read supported claims
			parseSupportedClaimsConfig(oauthElem);

			// read supported response types
			parseSupportedClientAuthMethodsConfig(oauthElem);

			// read SAML2 grant config
			parseSAML2GrantConfig(oauthElem);

			// read JWT generator config
			parseAuthorizationContextTokenGeneratorConfig(oauthElem);

			// read the assertions user name config
			parseEnableAssertionsUserNameConfig(oauthElem);

			// read access token partitioning config
			parseAccessTokenPartitioningConfig(oauthElem);

			// read access token partitioning domains config
			parseAccessTokenPartitioningDomainsConfig(oauthElem);

			// read openid connect configurations
			parseOpenIDConnectConfig(oauthElem);
			
			// read loginconfig
			parseLoginConfig(oauthElem);

		} catch (ServerConfigurationException e) {
			log.error("Error when reading the OAuth Configurations. "
			          + "OAuth related functionality might be affected.", e);
		}
	}

	/**
	 * Returns the custom login page Url
	 * 
	 * @return
	 */
	public String getCustomLoginPageUrl() {
		return loginPageUrl;
	}

	/**
	 * Returns the custom error page Url
	 * 
	 * @return
	 */
	public String getCustomErrorPageUrl() {
		return errorPageUrl;
	}
	
	public String getCustomConsentPageUrl() {
		return consentPageUrl;
	}

	public Set<OAuthCallbackHandlerMetaData> getCallbackHandlerMetaData() {
		return callbackHandlerMetaData;
	}

	public long getDefaultAuthorizationCodeValidityPeriodInSeconds() {
		return defaultAuthorizationCodeValidityPeriodInSeconds;
	}

	public long getDefaultAccessTokenValidityPeriodInSeconds() {
		return defaultAccessTokenValidityPeriodInSeconds;
	}

	
    public long getDefaultApplicationAccessTokenValidityPeriodInSeconds() {
        return defaultApplicationAccessTokenValidityPeriodInSeconds;
    }
	
	public long getDefaultTimeStampSkewInSeconds() {
		return defaultTimeStampSkewInSeconds;
	}

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public List<String> getSupportedGrantTypes() {
		return supportedGrantTypes;
	}

	public List<String> getSupportedResponseTypes() {
		return supportedResponseTypes;
	}

	public String[] getSupportedClaims() {
		return supportedClaims;
	}

	public List<String> getSupportedClientAuthMethods() {
		return supportedClientAuthMethods;
	}

	public Map<String, String> getSAML2Issuers() {
		return saml2Issuers;
	}

	public List<String> getSAML2Audience() {
		return saml2Audience;
	}

	public String getTokenEndPoint() {
		return tokenEP;
	}

	public List<String> getTokenEndPointAliases() {
		return tokenEPAliases;
	}

	public List<String> getRequiredHeaderClaimUris() {
		return requiredHeaderClaimUris;

	}

	public boolean isAccessTokenPartitioningEnabled() {
		return accessTokenPartitioningEnabled;
	}

	public boolean isUserNameAssertionEnabled() {
		return assertionsUserNameEnabled;
	}

	public String getAccessTokenPartitioningDomains() {
		return accessTokenPartitioningDomains;
	}

	private QName getQNameWithIdentityNS(String localPart) {
		return new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE, localPart);
	}

	public boolean isAuthContextTokGenEnabled() {
		return isAuthContextTokGenEnabled;
	}

	public String getTokenGeneratorImplClass() {
		return tokenGeneratorImplClass;
	}

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public String getConsumerDialectURI() {
		return consumerDialectURI;
	}

	public String getClaimsRetrieverImplClass() {
		return claimsRetrieverImplClass;
	}

	public String getAuthorizationContextTTL() {
		return authContextTTL;
	}

	public Map<String, Map<String, String>> getLoginConfiguration() {
		return loginConfiguration;
	}
	
	public TokenPersistencePreprocessor getTokenPersistencePreprocessor() throws IdentityOAuth2Exception {
		// create an instance of a TokenPersistencePreprocessor. This is a one
		// time operation
		// because there can be only on OAuthServerConfiguration in a given
		// runtime.
		if (tokenPersistencePreprocessor == null) {
			synchronized (this) {
				try {
					Class clazz =
					              this.getClass().getClassLoader()
					                  .loadClass(tokenPersistencePreProcessorClassName);
					tokenPersistencePreprocessor = (TokenPersistencePreprocessor) clazz.newInstance();

					if (log.isDebugEnabled()) {
						log.debug("An instance of " + tokenPersistencePreProcessorClassName +
						          " is created for OAuthServerConfiguration.");
					}

				} catch (Exception e) {
					String errorMsg =
					                  "Error when instantiating the TokenPersistencePreprocessor : " +
					                          tokenPersistencePreProcessorClassName;
					log.error(errorMsg, e);
					throw new IdentityOAuth2Exception(errorMsg, e);
				}
			}
		}
		return tokenPersistencePreprocessor;
	}
	
	/**
	 * Return an instance of the IDToken builder 
	 * @return
	 */
	public IDTokenBuilder getOpenIDConnectIDTokenBuilder() {
		if (openIDConnectIDTokenBuilder == null) {
			synchronized (IDTokenBuilder.class) {
				if (openIDConnectIDTokenBuilder == null) {
					try {
						Class clazz =
						              Thread.currentThread().getContextClassLoader()
						                    .loadClass(openIDConnectIDTokenBuilderClassName);
						openIDConnectIDTokenBuilder = (IDTokenBuilder) clazz.newInstance();
					} catch (ClassNotFoundException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					} catch (InstantiationException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					} catch (IllegalAccessException e) {
						log.error("Error while instantiating the IDTokenBuilder ", e);
					}
				}
			}
		}
		return openIDConnectIDTokenBuilder;
	}

	/**
	 * @return the openIDConnectIDTokenIssuer
	 */
	public String getOpenIDConnectIDTokenIssuer() {
		return openIDConnectIDTokenIssuer;
	}

	/**
	 * @return the openIDConnectIDTokenExpiration
	 */
	public String getOpenIDConnectIDTokenExpiration() {
		return openIDConnectIDTokenExpiration;
	}

	public String getOpenIDConnectUserInfoEndpointClaimDialect() {
		return openIDConnectUserInfoEndpointClaimDialect;
	}
	
	public String getOpenIDConnectUserInfoEndpointClaimRetriever() {
		return openIDConnectUserInfoEndpointClaimRetriever;
	}
	
	public String getOpenIDConnectUserInfoEndpointRequestValidator() {
		return openIDConnectUserInfoEndpointRequestValidator;
	}
	
	public String getOpenIDConnectUserInfoEndpointAccessTokenValidator() {
		return openIDConnectUserInfoEndpointAccessTokenValidator;
	}
	
	public String getOpenIDConnectUserInfoEndpointResponseBuilder() {
		return openIDConnectUserInfoEndpointResponseBuilder;
	}

	   	
	private void parseOAuthCallbackHandlers(OMElement callbackHandlersElem) {
		if (callbackHandlersElem == null) {
			warnOnFaultyConfiguration("AuthorizationCallbackHandlers element is not available.");
			return;
		}

		Iterator callbackHandlers =
		                            callbackHandlersElem.getChildrenWithLocalName(ConfigElements.OAUTH_CALLBACK_HANDLER);
		int callbackHandlerCount = 0;
		if (callbackHandlers != null) {
			for (; callbackHandlers.hasNext();) {
				OAuthCallbackHandlerMetaData cbHandlerMetadata =
				                                                 buildAuthzCallbackHandlerMetadata((OMElement) callbackHandlers.next());
				if (cbHandlerMetadata != null) {
					callbackHandlerMetaData.add(cbHandlerMetadata);
					if (log.isDebugEnabled()) {
						log.debug("OAuthAuthorizationCallbackHandleMetadata was added. Class : " +
						          cbHandlerMetadata.getClassName());
					}
					callbackHandlerCount++;
				}
			}
		}
		// if no callback handlers are registered, print a WARN
		if (!(callbackHandlerCount > 0)) {
			warnOnFaultyConfiguration("No AuthorizationCallbackHandler elements were found.");
		}
	}

	private void parseRequiredHeaderClaimUris(OMElement requiredClaimUrisElem) {
		if (requiredClaimUrisElem == null) {
			return;
		}

		Iterator claimUris = requiredClaimUrisElem.getChildrenWithLocalName(ConfigElements.CLAIM_URI);
		if (claimUris != null) {
			for (; claimUris.hasNext();) {
				OMElement claimUri = ((OMElement) claimUris.next());
				if (claimUri != null) {
					requiredHeaderClaimUris.add(claimUri.getText());
				}
			}
		}

	}

	private void parseTokenValidators(OMElement tokenValidators) {
		if (tokenValidators == null) {
			return;
		}

		Iterator validators = tokenValidators.getChildrenWithLocalName(ConfigElements.TOKEN_VALIDATOR);
		if (validators != null) {
			for (; validators.hasNext();) {
				OMElement validator = ((OMElement) validators.next());
				if (validator != null) {
					OAuth2TokenValidator tokenValidator = null;
					String clazzName = null;
					try {
						clazzName =
						            validator.getAttributeValue(getQNameWithIdentityNS(ConfigElements.TOKEN_CLASS_ATTR));
						Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
						tokenValidator = (OAuth2TokenValidator) clazz.newInstance();
					} catch (ClassNotFoundException e) {
						log.error("Class not in build path " + clazzName, e);
					} catch (InstantiationException e) {
						log.error("Class initialization error " + clazzName, e);
					} catch (IllegalAccessException e) {
						log.error("Class access error " + clazzName, e);

					}
					String type =
					              validator.getAttributeValue(getQNameWithIdentityNS(ConfigElements.TOKEN_TYPE_ATTR));
					TokenValidationHandler.getInstance().addTokenValidator(type, tokenValidator);
				}
			}
		}
	}

	private void warnOnFaultyConfiguration(String logMsg) {
		log.warn("Error in OAuth Configuration. " + logMsg);
	}

	private OAuthCallbackHandlerMetaData buildAuthzCallbackHandlerMetadata(OMElement omElement) {
		// read the class attribute which is mandatory
		String className = omElement.getAttributeValue(new QName(ConfigElements.CALLBACK_CLASS));

		if (className == null) {
			log.error("Mandatory attribute \"Class\" is not present in the "
			          + "AuthorizationCallbackHandler element. "
			          + "AuthorizationCallbackHandler will not be registered.");
			return null;
		}

		// read the priority element, if it is not there, use the default
		// priority of 1
		int priority = OAuthConstants.OAUTH_AUTHZ_CB_HANDLER_DEFAULT_PRIORITY;
		OMElement priorityElem =
		                         omElement.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CALLBACK_PRIORITY));
		if (priorityElem != null) {
			priority = Integer.parseInt(priorityElem.getText());
		}

		if (log.isDebugEnabled()) {
			log.debug("Priority level of : " + priority + " is set for the " +
			          "AuthorizationCallbackHandler with the class : " + className);
		}

		// read the additional properties.
		OMElement paramsElem =
		                       omElement.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CALLBACK_PROPERTIES));
		Properties properties = null;
		if (paramsElem != null) {
			Iterator paramItr = paramsElem.getChildrenWithLocalName(ConfigElements.CALLBACK_PROPERTY);
			properties = new Properties();
			if (log.isDebugEnabled()) {
				log.debug("Registering Properties for AuthorizationCallbackHandler class : " + className);
			}
			for (; paramItr.hasNext();) {
				OMElement paramElem = (OMElement) paramItr.next();
				String paramName = paramElem.getAttributeValue(new QName(ConfigElements.CALLBACK_ATTR_NAME));
				String paramValue = paramElem.getText();
				properties.put(paramName, paramValue);
				if (log.isDebugEnabled()) {
					log.debug("Property name : " + paramName + ", Property Value : " + paramValue);
				}
			}
		}
		return new OAuthCallbackHandlerMetaData(className, properties, priority);
	}

	private void parseDefaultValidityPeriods(OMElement oauthConfigElem) {

		// reads the custom login page url configuration
		OMElement loginPageUrlElem =
		                             oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CUSTOM_LOGIN_PAGE_URL));
		if (loginPageUrlElem != null) {
			loginPageUrl = loginPageUrlElem.getText();
		}

		// read the custom error page url configuration
		OMElement errorPageUrlElem =
		                             oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CUSTOM_ERROR_PAGE_URL));
		if (errorPageUrlElem != null) {
			errorPageUrl = errorPageUrlElem.getText();
		}
		
		// read the custom error page url configuration
		OMElement consentPageUrlElem =
		                             oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CUSTOM_CONSENT_PAGE_URL));
		if (consentPageUrlElem != null) {
			consentPageUrl = consentPageUrlElem.getText();
		}

		// set the authorization code default timeout
		OMElement authzCodeTimeoutElem =
		                                 oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.AUTHORIZATION_CODE_DEFAULT_VALIDITY_PERIOD));

		if (authzCodeTimeoutElem != null) {
			defaultAuthorizationCodeValidityPeriodInSeconds = Long.parseLong(authzCodeTimeoutElem.getText());
		}

		// set the access token default timeout
		OMElement accessTokTimeoutElem =
		                                 oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ACCESS_TOKEN_DEFAULT_VALIDITY_PERIOD));
		if (accessTokTimeoutElem != null) {
			defaultAccessTokenValidityPeriodInSeconds = Long.parseLong(accessTokTimeoutElem.getText());
		}

		 // set the application access token default timeout
        OMElement applicationAccessTokTimeoutElem = oauthConfigElem.getFirstChildWithName(
                getQNameWithIdentityNS(ConfigElements.APPLICATION_ACCESS_TOKEN_VALIDATION_PERIOD));
        if (applicationAccessTokTimeoutElem != null) {
            defaultApplicationAccessTokenValidityPeriodInSeconds = Long.parseLong(applicationAccessTokTimeoutElem.getText());
        }

		OMElement timeStampSkewElem =
		                              oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TIMESTAMP_SKEW));
		if (timeStampSkewElem != null) {
			defaultTimeStampSkewInSeconds = Long.parseLong(timeStampSkewElem.getText());
		}

		if (log.isDebugEnabled()) {
			if (authzCodeTimeoutElem == null) {
				log.debug("\"Authorization Code Default Timeout\" element was not available "
				          + "in identity.xml. Continuing with the default value.");
			}
			if (accessTokTimeoutElem == null) {
				log.debug("\"Access Token Default Timeout\" element was not available "
				          + "in from identity.xml. Continuing with the default value.");
			}
			if (timeStampSkewElem == null) {
				log.debug("\"Default Timestamp Skew\" element was not available "
				          + "in from identity.xml. Continuing with the default value.");
			}
			log.debug("Authorization Code Default Timeout is set to : " +
			          defaultAuthorizationCodeValidityPeriodInSeconds + "ms.");
			log.debug("Access Token Default Timeout is set to " + defaultAccessTokenValidityPeriodInSeconds +
			          "ms.");
			log.debug("Application Access Token Default Timeout is set to " +
                      defaultAccessTokenValidityPeriodInSeconds + "ms.");
			log.debug("Default TimestampSkew is set to " + defaultTimeStampSkewInSeconds + "ms.");
		}
	}

	private void parseCachingConfiguration(OMElement oauthConfigElem) {
		OMElement enableCacheElem =
		                            oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLE_CACHE));
		if (enableCacheElem != null) {
			cacheEnabled = Boolean.parseBoolean(enableCacheElem.getText());
		}

		if (log.isDebugEnabled()) {
			log.debug("Enable OAuth Cache was set to : " + cacheEnabled);
		}
	}

	private void parseAccessTokenPartitioningConfig(OMElement oauthConfigElem) {
		OMElement enableAccessTokenPartitioningElem =
		                                              oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLE_ACCESS_TOKEN_PARTITIONING));
		if (enableAccessTokenPartitioningElem != null) {
			accessTokenPartitioningEnabled =
			                                 Boolean.parseBoolean(enableAccessTokenPartitioningElem.getText());
		}

		if (log.isDebugEnabled()) {
			log.debug("Enable OAuth Access Token Partitioning was set to : " + accessTokenPartitioningEnabled);
		}
	}

	private void parseAccessTokenPartitioningDomainsConfig(OMElement oauthConfigElem) {
		OMElement enableAccessTokenPartitioningElem =
		                                              oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ACCESS_TOKEN_PARTITIONING_DOMAINS));
		if (enableAccessTokenPartitioningElem != null) {
			accessTokenPartitioningDomains = enableAccessTokenPartitioningElem.getText();
		}

		if (log.isDebugEnabled()) {
			log.debug("Enable OAuth Access Token Partitioning Domains was set to : " +
			          accessTokenPartitioningDomains);
		}
	}

	private void parseEnableAssertionsUserNameConfig(OMElement oauthConfigElem) {
		OMElement enableAssertionsElem =
		                                 oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLE_ASSERTIONS));
		if (enableAssertionsElem != null) {
			OMElement enableAssertionsUserNameElem =
			                                         enableAssertionsElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLE_ASSERTIONS_USERNAME));
			if (enableAssertionsUserNameElem != null) {
				assertionsUserNameEnabled = Boolean.parseBoolean(enableAssertionsUserNameElem.getText());
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Enable Assertions-UserName was set to : " + assertionsUserNameEnabled);
		}
	}

	private void parseTokenPersistencePreProcessorConfig(OMElement oauthConfigElem) {
		OMElement preprocessorConfigElem =
		                                   oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_PERSISTENCE_PREPROCESSOR));
		if (preprocessorConfigElem != null) {
			tokenPersistencePreProcessorClassName = preprocessorConfigElem.getText().trim();
		}

		if (log.isDebugEnabled()) {
			log.debug("Token Persistence Preprocessor was set to : " + tokenPersistencePreProcessorClassName);
		}
	}

	private void parseSupportedGrantTypesConfig(OMElement oauthConfigElem) {
		OMElement supportedGrantTypesElem =
		                                    oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_GRANT_TYPES));

		// These are the default grant types supported.
		List<String> validGrantTypes = new ArrayList<String>(4);
		validGrantTypes.add(GrantType.AUTHORIZATION_CODE.toString());
		validGrantTypes.add(GrantType.CLIENT_CREDENTIALS.toString());
		validGrantTypes.add(GrantType.PASSWORD.toString());
		validGrantTypes.add(GrantType.REFRESH_TOKEN.toString());
		validGrantTypes.add(org.wso2.carbon.identity.oauth.common.GrantType.SAML20_BEARER.toString());

		if (supportedGrantTypesElem != null) {
			String grantTypeStr = supportedGrantTypesElem.getText();
			if (grantTypeStr != null) {
				String[] grantTypes = grantTypeStr.split(",");
				// Check whether user provided grant types are valid
				if (grantTypes != null) {
					for (String providedGrantType : grantTypes) {
						providedGrantType = providedGrantType.trim();
						if (validGrantTypes.contains(providedGrantType)) {
							supportedGrantTypes.add(providedGrantType);
						} else {
							if (log.isDebugEnabled()) {
								log.debug("Invalid Grant Type provided : " + providedGrantType +
								          ". This will be ignored.");
							}
						}
					}
				}
			}
		} else {
			// if this element is not present, assume the default case.
			supportedGrantTypes.addAll(validGrantTypes);
		}

		if (log.isDebugEnabled()) {
			log.debug("Supported Grant Types : " + supportedGrantTypes);
		}
	}

	private void parseSupportedResponseTypesConfig(OMElement oauthConfigElem) {
		OMElement supportedRespTypesElem =
		                                   oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_RESP_TYPES));

		// These are the default grant types supported.
		List<String> validRespTypes = new ArrayList<String>(4);
		validRespTypes.add(ResponseType.CODE.toString());
		validRespTypes.add(ResponseType.TOKEN.toString());

		if (supportedRespTypesElem != null) {
			String respTypesStr = supportedRespTypesElem.getText();
			if (respTypesStr != null) {
				String[] respTypes = respTypesStr.split(",");
				// Check whether user provided grant types are valid
				if (respTypes != null) {
					for (String providedRespType : respTypes) {
						providedRespType = providedRespType.trim();
						if (validRespTypes.contains(providedRespType)) {
							supportedResponseTypes.add(providedRespType);
						} else {
							if (log.isDebugEnabled()) {
								log.debug("Invalid Response Type provided : " + providedRespType +
								          ". This will be ignored.");
							}
						}
					}
				}
			}
		} else {
			// if this element is not present, assume the default case.
			supportedResponseTypes.addAll(validRespTypes);
		}

		if (log.isDebugEnabled()) {
			log.debug("Supported Response Types : " + supportedResponseTypes);
		}
	}

	private void parseSupportedClaimsConfig(OMElement oauthConfigElem) {
		OMElement supportedClaimsElem =
		                                oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_CLAIMS));
		String claimsStr = null;
		if (supportedClaimsElem != null) {
			claimsStr = supportedClaimsElem.getText();
			if (claimsStr != null) {
				supportedClaims = claimsStr.split(",");
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Supported Claims : " + claimsStr);
		}
	}

	private void parseSupportedClientAuthMethodsConfig(OMElement oauthConfigElem) {
		OMElement supportedClientAuthMethodsElem =
		                                           oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SUPPORTED_CLIENT_AUTH_METHODS));

		// These are the default client authentication methods supported.
		List<String> validClientAuthMethods = new ArrayList<String>(4);
		validClientAuthMethods.add(org.wso2.carbon.identity.oauth2.util.OAuth2Constants.ClientAuthMethods.BASIC.toString());
		validClientAuthMethods.add(org.wso2.carbon.identity.oauth2.util.OAuth2Constants.ClientAuthMethods.SAML_20_BEARER.toString());

		if (supportedClientAuthMethodsElem != null) {
			String supportedClientAuthMethodsElemStr = supportedClientAuthMethodsElem.getText();
			if (supportedClientAuthMethodsElemStr != null) {
				String[] clientAuthMethods = supportedClientAuthMethodsElemStr.split(",");
				// Check whether user provided grant types are valid
				if (supportedClientAuthMethodsElem != null) {
					for (String providedClientAuthMethod : clientAuthMethods) {
						providedClientAuthMethod = providedClientAuthMethod.trim();
						if (validClientAuthMethods.contains(providedClientAuthMethod)) {
							supportedClientAuthMethods.add(providedClientAuthMethod);
						} else {
							if (log.isDebugEnabled()) {
								log.debug("Invalid Client Authentication Method provided : " +
								          providedClientAuthMethod + ". This will be ignored.");
							}
						}
					}
				}
			}
		} else {
			// if this element is not present, assume the default case.
			supportedClientAuthMethods.addAll(validClientAuthMethods);
		}

		if (log.isDebugEnabled()) {
			log.debug("Supported Client Authentication Methods : " + supportedClientAuthMethods);
		}
	}

	private void parseSAML2GrantConfig(OMElement oauthConfigElem) {
		OMElement validSAML2IssuersElem =
		                                  oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SAML2_GRANT))
		                                                 .getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ISSUERS));

		if (validSAML2IssuersElem != null) {
			// String[] issuers = validSAML2IssuersElem.getText().split(",");
			Iterator<OMElement> it =
			                         validSAML2IssuersElem.getChildrenWithName(getQNameWithIdentityNS(ConfigElements.ISSUER));
			while (it.hasNext()) {
				OMElement issuer = it.next();
				String issuerValue = issuer.getText();
				String entityID = issuer.getAttributeValue(new QName("trustEntityId"));
				if (saml2Issuers.containsKey(issuerValue)) {
					log.warn("Duplicate entry in SAML2.0 Issuers: " + issuer);
				} else {
					saml2Issuers.put(issuerValue, entityID);
				}
			}
		}
		if (log.isDebugEnabled()) {
			Set<Map.Entry<String, String>> entries = saml2Issuers.entrySet();
			for (Map.Entry<String, String> entry : entries) {
				log.debug("Valid SAML2Grant Issuer " + entry.getKey() + " : " + entry.getValue());
			}
		}

		OMElement validSAML2AudienceElem =
		                                   oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SAML2_GRANT))
		                                                  .getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.AUDIENCE));
		if (validSAML2AudienceElem != null) {
			String[] audiences = validSAML2AudienceElem.getText().split(",");
			for (String audience : audiences) {
				saml2Audience.add(audience);
			}
		}
		if (log.isDebugEnabled()) {
			for (int i = 0; i < saml2Audience.size(); i++) {
				log.debug("Valid SAML2Grant Audience " + i + " : " + saml2Audience.get(i));
			}
		}

		OMElement validTokenEPElem =
		                             oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SAML2_GRANT))
		                                            .getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_END_POINT));
		if (validTokenEPElem != null) {
			tokenEP = validSAML2AudienceElem.getText();
		}
		if (log.isDebugEnabled()) {
			for (int i = 0; i < saml2Audience.size(); i++) {
				log.debug("Token EndPoint : " + tokenEP);
			}
		}

		OMElement tokenEPAliasesElem =
		                               oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SAML2_GRANT))
		                                              .getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_END_POINT_ALIASES));
		if (tokenEPAliasesElem != null) {
			String[] aliases = tokenEPAliasesElem.getText().split(",");
			for (String alias : aliases) {
				tokenEPAliases.add(alias);
			}
		}
		if (log.isDebugEnabled()) {
			for (int i = 0; i < tokenEPAliases.size(); i++) {
				log.debug("Token EndPoint Alias" + i + " : " + tokenEPAliases.get(i));
			}
		}
	}

	private void parseAuthorizationContextTokenGeneratorConfig(OMElement oauthConfigElem) {
		OMElement authContextTokGenConfigElem =
		                                        oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.AUTHORIZATION_CONTEXT_TOKEN_GENERATION));
		if (authContextTokGenConfigElem != null) {
			OMElement enableJWTGenerationConfigElem =
			                                          authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.ENABLED));
			if (enableJWTGenerationConfigElem != null) {
				String enableJWTGeneration = enableJWTGenerationConfigElem.getText().trim();
				if (enableJWTGeneration != null && JavaUtils.isTrueExplicitly(enableJWTGeneration)) {
					isAuthContextTokGenEnabled = true;
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_GENERATOR_IMPL_CLASS)) != null) {
						tokenGeneratorImplClass =
						                          authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.TOKEN_GENERATOR_IMPL_CLASS))
						                                                     .getText().trim();
					}
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CLAIMS_RETRIEVER_IMPL_CLASS)) != null) {
						claimsRetrieverImplClass =
						                           authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CLAIMS_RETRIEVER_IMPL_CLASS))
						                                                      .getText().trim();
					}
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CONSUMER_DIALECT_URI)) != null) {
						consumerDialectURI =
						                     authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CONSUMER_DIALECT_URI))
						                                                .getText().trim();
					}
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SIGNATURE_ALGORITHM)) != null) {
						signatureAlgorithm =
						                     authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SIGNATURE_ALGORITHM))
						                                                .getText().trim();
					}
					if (authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SECURITY_CONTEXT_TTL)) != null) {
						authContextTTL =
						                 authContextTokGenConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.SECURITY_CONTEXT_TTL))
						                                            .getText().trim();
					}
				}
			}
		}
		if (log.isDebugEnabled()) {
			if (isAuthContextTokGenEnabled) {
				log.debug("JWT Generation is enabled");
			} else {
				log.debug("JWT Generation is disabled");
			}
		}
	}

	private void parseOpenIDConnectConfig(OMElement oauthConfigElem) {

		OMElement openIDConnectConfigElem =
		                                    oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT));

		if (openIDConnectConfigElem != null) {
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_BUILDER)) != null) {
				openIDConnectIDTokenBuilderClassName =
				                             openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_BUILDER))
				                                                    .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_ISSUER_ID)) != null) {
				openIDConnectIDTokenIssuer =
				                             openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_ISSUER_ID))
				                                                    .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_EXPIRATION)) != null) {
				openIDConnectIDTokenExpiration =
				                                 openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_IDTOKEN_EXPIRATION))
				                                                        .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_DIALECT)) != null) {
				openIDConnectUserInfoEndpointClaimDialect =
				                                            openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_DIALECT))
				                                                                   .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_RETRIEVER)) != null) {
				openIDConnectUserInfoEndpointClaimRetriever =
				                                              openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_CLAIM_RETRIEVER))
				                                                                     .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_REQUEST_VALIDATOR)) != null) {
				openIDConnectUserInfoEndpointRequestValidator =
				                                                openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_REQUEST_VALIDATOR))
				                                                                       .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_ACCESS_TOKEN_VALIDATOR)) != null) {
				openIDConnectUserInfoEndpointAccessTokenValidator =
				                                                    openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_ACCESS_TOKEN_VALIDATOR))
				                                                                           .getText().trim();
			}
			if (openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_RESPONSE_BUILDER)) != null) {
				openIDConnectUserInfoEndpointResponseBuilder =
				                                               openIDConnectConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.OPENID_CONNECT_USERINFO_ENDPOINT_RESPONSE_BUILDER))
				                                                                      .getText().trim();
			}
		}
	}
	
		/**
	 * Read the primary/secondary login configuration
	 * <OAuth>
	 *	....
	 *	<LoginConfig>
	 *		<UserIdLogin  primary="true">
	 *			<ClaimUri></ClaimUri>
	 *		</UserIdLogin>
	 *		<EmailLogin  primary="false">
	 *			<ClaimUri>http://wso2.org/claims/emailaddress</ClaimUri>
	 *		</EmailLogin>
	 *	</LoginConfig>
	 *	.....
	 *   </OAuth>
	 * @param oauthConfigElem
	 */
	private void parseLoginConfig(OMElement oauthConfigElem) {
		OMElement loginConfigElem =  oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.LOGIN_CONFIG));
		if (loginConfigElem != null) {
			if (log.isDebugEnabled()) {
				log.debug("Login configuration is set ");
			}
			// Primary/Secondary supported login mechanisms
			OMElement emailConfigElem = loginConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.EMAIL_LOGIN));

			OMElement userIdConfigElem =  loginConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.USERID_LOGIN));

			Map<String, String> emailConf = new HashMap<String, String>(2);
			emailConf.put(ConfigElements.PRIMARY_LOGIN,
			              emailConfigElem.getAttributeValue(new QName(ConfigElements.PRIMARY_LOGIN)));
			emailConf.put(ConfigElements.CLAIM_URI,
			              emailConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CLAIM_URI))
			                             .getText());

			Map<String, String> userIdConf = new HashMap<String, String>(2);
			userIdConf.put(ConfigElements.PRIMARY_LOGIN,
			               userIdConfigElem.getAttributeValue(new QName(ConfigElements.PRIMARY_LOGIN)));
			userIdConf.put(ConfigElements.CLAIM_URI,
			               userIdConfigElem.getFirstChildWithName(getQNameWithIdentityNS(ConfigElements.CLAIM_URI))
			                               .getText());

			loginConfiguration.put(ConfigElements.EMAIL_LOGIN, emailConf);
			loginConfiguration.put(ConfigElements.USERID_LOGIN, userIdConf);
		}
	}
}
