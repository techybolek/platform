/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mediation.secure.vault;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class SecureVaultLookupHandlerImpl implements SecureVaultLookupHandler {

	private static Log log = LogFactory.getLog(SecureVaultLookupHandlerImpl.class);

	public enum LookupType {
		FILE, REGISTRY
	}

	private static SecureVaultLookupHandlerImpl instance = null;

	private ServerConfigurationService serverConfigService;

	private RegistryService registryService;

	UserRegistry registry = null;

	private SecureVaultLookupHandlerImpl(
			ServerConfigurationService serverConfigurationService,
			RegistryService registryService) throws RegistryException {
		this.serverConfigService = serverConfigurationService;
		this.registryService = registryService;
		try {
			init();
		} catch (RegistryException e) {
			throw new RegistryException("Error while intializing the registry");
		}

	}

	public static SecureVaultLookupHandlerImpl getDefaultSecurityService()
			throws RegistryException {
		return getDefaultSecurityService(SecurityServiceHolder.getInstance()
				.getServerConfigurationService(), SecurityServiceHolder.getInstance()
				.getRegistryService());
	}

	private static SecureVaultLookupHandlerImpl getDefaultSecurityService(
			ServerConfigurationService serverConfigurationService,
			RegistryService registryService) throws RegistryException {
		if (instance == null) {
			instance = new SecureVaultLookupHandlerImpl(serverConfigurationService,
					registryService);
		}
		return instance;
	}

	private void init() throws RegistryException {
		try {
			registry = registryService.getConfigSystemRegistry();

			// creating vault-specific storage repository (this happens only if
			// not resource not existing)
			initRegistryRepo();
		} catch (RegistryException e) {
			throw new RegistryException("Error while intializing the registry");
		}
	}

	/**
	 * Initializing the repository which requires to store the secure vault
	 * cipher text
	 * 
	 * @throws RegistryException
	 */
	private void initRegistryRepo() throws RegistryException {

		if (!isRepoExists()) {
			org.wso2.carbon.registry.core.Collection secureVaultCollection = registry
					.newCollection();
			registry.put(SecureVaultConstants.CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY,
					secureVaultCollection);
		}
	}

	/**
	 * Checks whether the given repository already existing.
	 * 
	 * @return
	 */
	protected boolean isRepoExists() {
		try {
			registry.get(SecureVaultConstants.CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY);
		} catch (RegistryException e) {
			return false;
		}
		return true;
	}

	public String getProviderClass() {
		return this.getClass().getName();
	}

	@Override
	public void init(Properties arg0) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wso2.carbon.mediation.secure.vault.MediationSecurity#evaluate(java
	 * .lang.String,
	 * org.wso2.carbon.mediation.secure.vault.MediationSrecurtyClient
	 * .LookupType)
	 */
	@Override
	public String evaluate(String aliasPasword, LookupType lookupType,
			MessageContext synCtx) throws RegistryException {
		if (lookupType.equals(LookupType.FILE)) {
			return lookupFileRepositry(aliasPasword, synCtx);
		} else {
			return lookupRegistry(aliasPasword, synCtx);
		}

	}

	/**
	 * Lookup in the register to retrieve value
	 * 
	 * @param aliasPasword
	 * @return
	 * @throws RegistryException
	 */
	private String lookupRegistry(String aliasPasword, MessageContext synCtx)
			throws RegistryException {
		if (log.isDebugEnabled()) {
			log.info("processing evaluating registry based lookup");
		}
		SecretCipherHander secretManager = new SecretCipherHander(synCtx);
		return secretManager.getSecret(aliasPasword);

	}

	/**
	 * Lookup credentials via the FileRepository configuration
	 * 
	 * @param aliasPasword
	 * @return
	 */
	private String lookupFileRepositry(String aliasPasword, MessageContext synCtx) {
		if (log.isDebugEnabled()) {
			log.info("processing evaluating file based lookup");
		}
		SecretCipherHander secretManager = new SecretCipherHander(synCtx);
		if (secretManager.isInitialized()) {
			return secretManager.getSecret(aliasPasword);
		}
		return null;
	}

}
