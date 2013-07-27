/**
 *
 */
package org.wso2.carbon.mediation.secure.vault;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.Entry;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.securevault.SecureVaultException;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.securevault.definition.IdentityKeyStoreInformation;
import org.wso2.securevault.definition.KeyStoreInformationFactory;
import org.wso2.securevault.definition.TrustKeyStoreInformation;
import org.wso2.securevault.keystore.IdentityKeyStoreWrapper;
import org.wso2.securevault.keystore.TrustKeyStoreWrapper;
import org.wso2.securevault.secret.SecretRepository;

/**
 * Entry point for manage secrets
 */
public class SecretCipherHander {

	private static Log log = LogFactory.getLog(SecretCipherHander.class);

	/* Root Secret Repository */
	private RegistrySecretRepository parentRepository = new RegistrySecretRepository();
	/*
	 * True , if secret manage has been started up properly- need to have a at
	 * least one Secret Repository
	 */
	private boolean initialized = false;


	private org.apache.synapse.MessageContext synCtx;
	
	
	CipherInitializer ciperInitializer =  CipherInitializer.getInstance();
	
	
	

	public SecretCipherHander(org.apache.synapse.MessageContext synCtx) {
		super();
		this.synCtx = synCtx;
		parentRepository.setSynCtx(synCtx);
	}


	/**
	 * Returns the secret corresponding to the given alias name
	 * 
	 * @param alias
	 *            The logical or alias name
	 * @return If there is a secret , otherwise , alias itself
	 */
	public String getSecret(String alias) {
		return parentRepository.getSecret(alias);
	}

	/**
	 * Returns the encrypted value corresponding to the given alias name
	 * 
	 * @param alias
	 *            The logical or alias name
	 * @return If there is a encrypted value , otherwise , alias itself
	 */
	public String getEncryptedData(String alias) {
		if (!initialized || parentRepository == null) {
			if (log.isDebugEnabled()) {
				log.debug("There is no secret repository. Returning alias itself");
			}
			return alias;
		}
		return parentRepository.getEncryptedData(alias);
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void shoutDown() {
		this.parentRepository = null;
		this.initialized = false;
	}

	public void resetInit() {
		this.initialized = false;
	}

	private static void handleException(String msg) {
		log.error(msg);
		throw new SecureVaultException(msg);
	}

	
//	public String getGlobalSecretProvider() {
//		return globalSecretProvider;
//	}

	

//	public void refresh() throws RegistryException {
//		try {
//			resetInit();
//			setup();
//		} catch (Exception e) {
//			throw new RegistryException("Error while refresh the registry");
//		}
//
//	}
	
//	public UserRegistry getRegistry(RegistryService registryService) {
//		UserRegistry registry = null;
//		PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getCurrentContext(synCtx.getConfiguration().getAxisConfiguration());
//		int tenantId = carbonContext.getTenantId();
//		String username = CurrentSession.getUser();
//		try {
//			registry = registryService.getRegistry(username, tenantId);
//		return registry;
//		} catch (Exception ignored) {
//
//			return null;
//		}
//	}

	public org.apache.synapse.MessageContext getSynCtx() {
		return synCtx;
	}

	public void setSynCtx(org.apache.synapse.MessageContext synCtx) {
		this.synCtx = synCtx;
	}
	
	

}