package org.wso2.carbon.identity.application.authentication.framework;

public abstract class ApplicationAuthenticatorConstants {
	
	public static final int STATUS_AUTHENTICATION_PASS = 1;
	public static final int STATUS_AUTHENTICATION_FAIL = 0;
	public static final int STATUS_AUTHENTICATION_CANNOT_HANDLE = -1;
	
	public static final String DO_AUTHENTICATION = "doAuthentication";
	public static final String AUTHENTICATED = "commonAuthAuthenticated";
	public static final String CALLER_PATH = "commonAuthCallerPath";
	public static final String QUERY_PARAMS = "commonAuthQueryParams";
}
