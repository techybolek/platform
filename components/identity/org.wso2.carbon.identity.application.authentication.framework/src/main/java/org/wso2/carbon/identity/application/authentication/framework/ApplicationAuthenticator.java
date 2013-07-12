package org.wso2.carbon.identity.application.authentication.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ApplicationAuthenticator {
	
	public static final int STATUS_AUTHENTICATION_PASS = 1;
	public static final int STATUS_AUTHENTICATION_FAIL = 0;
	public static final int STATUS_AUTHENTICATION_CANNOT_HANDLE = -1;
	
	public static final String DO_AUTHENTICATION = "doAuthentication";
	public static final String AUTHENTICATED = "commonAuthAuthenticated";
	public static final String CALLING_SERVLET_PATH = "commonAuthCallingServletPath";
	
	public int doAuthentication(HttpServletRequest request, HttpServletResponse response);
	
	public int getStatus(HttpServletRequest request);
	
	public int getFactor();
}