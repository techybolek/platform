package org.wso2.carbon.mediation.secure.vault.cipher.tool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.wso2.carbon.base.ServerConfiguration;

import sun.misc.BASE64Encoder;

public class CipherTool {

	private static final String WSO2CARBON_DEFAULT_PASSWORD = "wso2carbon";
	private static Cipher cipher = null;
	private static CipherTool instance = null;

	private CipherTool() {
		cipher = initCipher();
	}

	public static CipherTool getInstance() {
		if (instance == null) {
			instance = new CipherTool();
		}
		return instance;
	}

	/**
	 * init the Cipher for encryption using the primary key store of carbon
	 * server
	 * 
	 * @return cipher
	 */

	private static Cipher initCipher() {

		String keyStoreFile = null;
		String keyType = null;
		String aliasName = null;
		String password = null;
		String provider = null;
		Cipher cipher = null;

		keyStoreFile = ServerConfiguration.getInstance().getProperties(
				CipherToolConstants.SECURITY_KEY_STORE_LOCATION)[0];

		File keyStore = new File(keyStoreFile);

		if (!keyStore.exists()) {
			handleException("Primary Key Store Can not be found at Default location");
		}
		keyType = ServerConfiguration.getInstance().getProperties(
				CipherToolConstants.SECURITY_KEY_STORE_TYPE)[0];
		aliasName = ServerConfiguration.getInstance().getProperties(
				CipherToolConstants.SECURITY_KEY_STORE_KEY_ALIAS)[0];
		password = WSO2CARBON_DEFAULT_PASSWORD; // carbonKeyPasswordReader();

		try {
			KeyStore primaryKeyStore = getKeyStore(keyStoreFile, password, keyType,
					provider);
			java.security.cert.Certificate certs = primaryKeyStore
					.getCertificate(aliasName);
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, certs);
		} catch (InvalidKeyException e) {
			handleException("Error initializing Cipher ", e);
		} catch (NoSuchAlgorithmException e) {
			handleException("Error initializing Cipher ", e);
		} catch (KeyStoreException e) {
			handleException("Error initializing Cipher ", e);
		} catch (NoSuchPaddingException e) {
			handleException("Error initializing Cipher ", e);
		}

		return cipher;
	}

	/**
	 * encrypt the plain text password
	 * 
	 * @param cipher
	 *            init cipher
	 * @param plainTextPass
	 *            plain text password
	 * @return encrypted password
	 */
	public static String doEncryption(String plainTextPass) {
		String encodedValue = null;
		try {
			byte[] plainTextPassByte = plainTextPass.getBytes();
			byte[] encryptedPassword = cipher.doFinal(plainTextPassByte);
			BASE64Encoder encoder = new BASE64Encoder();
			encodedValue = encoder.encode(encryptedPassword);
		} catch (BadPaddingException e) {
			handleException("Error encrypting password ", e);
		} catch (IllegalBlockSizeException e) {
			handleException("Error encrypting password ", e);
		}
		return encodedValue;
	}

	/**
	 * get the primary key store instant
	 * 
	 * @param location
	 *            location of key store
	 * @param storePassword
	 *            password of key store
	 * @param storeType
	 *            key store type
	 * @param provider
	 *            key store provider
	 * @return KeyStore instant
	 */
	private static KeyStore getKeyStore(String location, String storePassword,
			String storeType, String provider) {

		File keyStoreFile = new File(location);
		if (!keyStoreFile.exists()) {
			handleException("KeyStore can not be found at ' " + keyStoreFile + " '");
		}
		if (storePassword == null) {
			handleException("KeyStore password can not be null");
		}
		if (storeType == null) {
			handleException("KeyStore Type can not be null");
		}
		BufferedInputStream bufferedInputStream = null;
		try {
			bufferedInputStream = new BufferedInputStream(new FileInputStream(
					keyStoreFile));
			KeyStore keyStore;
			if (provider != null) {
				keyStore = KeyStore.getInstance(storeType, provider);
			} else {
				keyStore = KeyStore.getInstance(storeType);
			}
			keyStore.load(bufferedInputStream, storePassword.toCharArray());
			return keyStore;
		} catch (KeyStoreException e) {
			handleException("Error loading keyStore from ' " + location + " ' ", e);
		} catch (IOException e) {
			handleException("IOError loading keyStore from ' " + location + " ' ", e);
		} catch (NoSuchAlgorithmException e) {
			handleException("Error loading keyStore from ' " + location + " ' ", e);
		} catch (CertificateException e) {
			handleException("Error loading keyStore from ' " + location + " ' ", e);
		} catch (NoSuchProviderException e) {
			handleException("Error loading keyStore from ' " + location + " ' ", e);
		} finally {
			if (bufferedInputStream != null) {
				try {
					bufferedInputStream.close();
				} catch (IOException ignored) {
					System.err.println("Error while closing input stream");
				}
			}
		}
		return null;
	}

	protected static void handleException(String msg, Exception e) {
		throw new CipherToolException(msg, e);
	}

	protected static void handleException(String msg) {
		throw new CipherToolException(msg);
	}

}
