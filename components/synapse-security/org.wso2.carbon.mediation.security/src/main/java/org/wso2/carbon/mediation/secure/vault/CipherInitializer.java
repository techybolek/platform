/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.mediation.secure.vault;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.CipherFactory;
import org.wso2.securevault.CipherOperationMode;
import org.wso2.securevault.DecryptionProvider;
import org.wso2.securevault.EncodingType;
import org.wso2.securevault.SecureVaultException;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.securevault.definition.CipherInformation;
import org.wso2.securevault.definition.IdentityKeyStoreInformation;
import org.wso2.securevault.definition.KeyStoreInformationFactory;
import org.wso2.securevault.definition.TrustKeyStoreInformation;
import org.wso2.securevault.keystore.IdentityKeyStoreWrapper;
import org.wso2.securevault.keystore.KeyStoreWrapper;
import org.wso2.securevault.keystore.TrustKeyStoreWrapper;
import org.wso2.securevault.secret.SecretRepository;

public class CipherInitializer {

	private static final String LOCATION = "location";
	private static final String KEY_STORE = "keyStore";
	private static final String DOT = ".";
	private static final String SECRET = "secret";
	private static final String ALIAS = "alias";
	private static final String ALIASES = "aliases";
	private static final String ALGORITHM = "algorithm";
	private static final String DEFAULT_ALGORITHM = "RSA";
	private static final String TRUSTED = "trusted";
	private static final String DEFAULT_CONF_LOCATION = "cipher-text.properties";

	private static CipherInitializer cipherInitializer = null;

	private static Log log = LogFactory.getLog(CipherInitializer.class);

	// global password provider implementation class if defined in secret
	// manager conf file
	private String globalSecretProvider = null;

	private IdentityKeyStoreWrapper identityKeyStoreWrapper;

	private TrustKeyStoreWrapper trustKeyStoreWrapper;

	private DecryptionProvider decryptionProvider = null;

	private CipherInitializer() {
		super();
		init();
		initCipherProvider();
	}

	public static CipherInitializer getInstance() {
		if (cipherInitializer == null) {
			cipherInitializer = new CipherInitializer();
		}
		return cipherInitializer;
	}

	private static void handleException(String msg) {
		log.error(msg);
		throw new SecureVaultException(msg);
	}

	private Properties loadProperties() {
		Properties properties = new Properties();
		String carbonHome = System.getProperty(SecureVaultConstants.CARBON_HOME);
		String filePath = carbonHome + File.separator
				+ SecureVaultConstants.REPOSITORY_DIR + File.separator
				+ SecureVaultConstants.CONF_DIR + File.separator
				+ SecureVaultConstants.SECURITY_DIR + File.separator
				+ SecureVaultConstants.SECRET_CONF;

		File dataSourceFile = new File(filePath);
		if (!dataSourceFile.exists()) {
			return properties;
		}

		InputStream in = null;
		try {
			in = new FileInputStream(dataSourceFile);
			properties.load(in);
		} catch (IOException e) {
			String msg = "Error loading properties from a file at :" + filePath;
			log.warn(msg, e);
			return properties;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {

				}
			}
		}
		return properties;
	}

	private void init() {
		Properties properties = loadProperties();

		if (properties == null) {
			if (log.isDebugEnabled()) {
				log.debug("KeyStore configuration properties cannot be found");
			}
			return;
		}

		String configurationFile = MiscellaneousUtil.getProperty(properties,
				SecureVaultConstants.PROP_SECRET_MANAGER_CONF,
				SecureVaultConstants.PROP_DEFAULT_CONF_LOCATION);

		Properties configurationProperties = MiscellaneousUtil
				.loadProperties(configurationFile);
		if (configurationProperties == null || configurationProperties.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("Configuration properties can not be loaded form : "
						+ configurationFile + " Will use synapse properties");
			}
			configurationProperties = properties;

		}

		globalSecretProvider = MiscellaneousUtil.getProperty(configurationProperties,
				SecureVaultConstants.PROP_SECRET_PROVIDER, null);
		if (globalSecretProvider == null || "".equals(globalSecretProvider)) {
			if (log.isDebugEnabled()) {
				log.debug("No global secret provider is configured.");
			}
		}

		String repositoriesString = MiscellaneousUtil.getProperty(
				configurationProperties, SecureVaultConstants.PROP_SECRET_REPOSITORIES,
				null);
		if (repositoriesString == null || "".equals(repositoriesString)) {
			if (log.isDebugEnabled()) {
				log.debug("No secret repositories have been configured");
			}
			return;
		}

		String[] repositories = repositoriesString.split(",");
		if (repositories == null || repositories.length == 0) {
			if (log.isDebugEnabled()) {
				log.debug("No secret repositories have been configured");
			}
			return;
		}

		// Create a KeyStore Information for private key entry KeyStore
		IdentityKeyStoreInformation identityInformation = KeyStoreInformationFactory
				.createIdentityKeyStoreInformation(properties);

		// Create a KeyStore Information for trusted certificate KeyStore
		TrustKeyStoreInformation trustInformation = KeyStoreInformationFactory
				.createTrustKeyStoreInformation(properties);

		String identityKeyPass = null;
		String identityStorePass = null;
		String trustStorePass = null;
		if (identityInformation != null) {
			identityKeyPass = identityInformation.getKeyPasswordProvider()
					.getResolvedSecret();
			identityStorePass = identityInformation.getKeyStorePasswordProvider()
					.getResolvedSecret();
		}

		if (trustInformation != null) {
			trustStorePass = trustInformation.getKeyStorePasswordProvider()
					.getResolvedSecret();
		}

		if (!validatePasswords(identityStorePass, identityKeyPass, trustStorePass)) {
			if (log.isDebugEnabled()) {
				log.info("Either Identity or Trust keystore password is mandatory"
						+ " in order to initialized secret manager.");
			}
			return;
		}

		identityKeyStoreWrapper = new IdentityKeyStoreWrapper();
		identityKeyStoreWrapper.init(identityInformation, identityKeyPass);

		trustKeyStoreWrapper = new TrustKeyStoreWrapper();
		if (trustInformation != null) {
			trustKeyStoreWrapper.init(trustInformation);
		}

		SecretRepository currentParent = null;
		for (String secretRepo : repositories) {

			StringBuffer sb = new StringBuffer();
			sb.append(SecureVaultConstants.PROP_SECRET_REPOSITORIES);
			sb.append(SecureVaultConstants.DOT);
			sb.append(secretRepo);
			String id = sb.toString();
			sb.append(SecureVaultConstants.DOT);
			sb.append(SecureVaultConstants.PROP_PROVIDER);

			String provider = MiscellaneousUtil.getProperty(configurationProperties,
					sb.toString(), null);
			if (provider == null || "".equals(provider)) {
				handleException("Repository provider cannot be null ");
			}

			if (log.isDebugEnabled()) {
				log.debug("Initiating a File Based Secret Repository");
			}

		}
	}

	private boolean validatePasswords(String identityStorePass, String identityKeyPass,
			String trustStorePass) {
		boolean isValid = false;
		if (trustStorePass != null && !"".equals(trustStorePass)) {
			if (log.isDebugEnabled()) {
				log.debug("Trust Store Password cannot be found.");
			}
			isValid = true;
		} else {
			if (identityStorePass != null && !"".equals(identityStorePass)
					&& identityKeyPass != null && !"".equals(identityKeyPass)) {
				if (log.isDebugEnabled()) {
					log.debug("Identity Store Password "
							+ "and Identity Store private key Password cannot be found.");
				}
				isValid = true;
			}
		}
		return isValid;
	}

	public DecryptionProvider getBaseCipher() {
		return decryptionProvider;
	}

	protected void initCipherProvider() {
		Properties properties = loadProperties();
		StringBuffer sb = new StringBuffer();
		// sb.append(id);
		sb.append(DOT);
		sb.append(LOCATION);

		StringBuffer sbTwo = new StringBuffer();
		// sbTwo.append(id);
		sbTwo.append(DOT);
		sbTwo.append(ALGORITHM);
		// Load algorithm
		String algorithm = MiscellaneousUtil.getProperty(properties, sbTwo.toString(),
				DEFAULT_ALGORITHM);
		StringBuffer buffer = new StringBuffer();
		buffer.append(DOT);
		buffer.append(KEY_STORE);

		// Load keyStore
		String keyStore = MiscellaneousUtil.getProperty(properties, buffer.toString(),
				null);

		KeyStoreWrapper keyStoreWrapper;

		if (TRUSTED.equals(keyStore)) {
			keyStoreWrapper = trustKeyStoreWrapper;

		} else {
			keyStoreWrapper = identityKeyStoreWrapper;
		}

		CipherInformation cipherInformation = new CipherInformation();
		cipherInformation.setAlgorithm(algorithm);
		cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
		cipherInformation.setInType(EncodingType.BASE64); // TODO
		decryptionProvider = CipherFactory.createCipher(cipherInformation,
				keyStoreWrapper);

	}

	public IdentityKeyStoreWrapper getIdentityKeyStoreWrapper() {
		return identityKeyStoreWrapper;
	}

	public void setIdentityKeyStoreWrapper(IdentityKeyStoreWrapper identityKeyStoreWrapper) {
		this.identityKeyStoreWrapper = identityKeyStoreWrapper;
	}

	public TrustKeyStoreWrapper getTrustKeyStoreWrapper() {
		return trustKeyStoreWrapper;
	}

	public void setTrustKeyStoreWrapper(TrustKeyStoreWrapper trustKeyStoreWrapper) {
		this.trustKeyStoreWrapper = trustKeyStoreWrapper;
	}

}
