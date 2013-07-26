package org.wso2.carbon.idp.mgt.util;

import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialContextSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;

public class X509CredentialImpl implements X509Credential {

    PublicKey publicKey = null;
    X509Certificate certificate = null;

    public X509CredentialImpl(X509Certificate cert){
        publicKey = cert.getPublicKey();
        certificate = cert;
    }

    @Override
    public X509Certificate getEntityCertificate() {
        return certificate;
    }

    @Override
    public Collection<X509Certificate> getEntityCertificateChain() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<X509CRL> getCRLs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getEntityId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public UsageType getUsageType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> getKeyNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SecretKey getSecretKey() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CredentialContextSet getCredentalContextSet() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class<? extends Credential> getCredentialType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
