/**
 *
 */
package org.wso2.carbon.mediation.security.vault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Entry point for manage secrets
 */
public class SecretCipherHander {

	private static Log log = LogFactory.getLog(SecretCipherHander.class);

	/* Root Secret Repository */
	private RegistrySecretRepository parentRepository = new RegistrySecretRepository();

	private org.apache.synapse.MessageContext synCtx;

	CipherInitializer ciperInitializer = CipherInitializer.getInstance();

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
		return parentRepository.getEncryptedData(alias);

	}

	public void shoutDown() {
		this.parentRepository = null;

	}

	public org.apache.synapse.MessageContext getSynCtx() {
		return synCtx;
	}

	public void setSynCtx(org.apache.synapse.MessageContext synCtx) {
		this.synCtx = synCtx;
	}

}