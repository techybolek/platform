package org.wso2.carbon.identity.authenticator.openid.ui;

public class OpenIDAuthenticationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -1166218403556534473L;

    public OpenIDAuthenticationException(String message) {
        super(message);
    }

    public OpenIDAuthenticationException(String message, Throwable e) {
        super(message, e);
    }

}
