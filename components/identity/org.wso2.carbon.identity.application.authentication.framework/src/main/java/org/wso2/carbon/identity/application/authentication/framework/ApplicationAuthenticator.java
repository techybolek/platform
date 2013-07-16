package org.wso2.carbon.identity.application.authentication.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ApplicationAuthenticator {
	
	public int doAuthentication(HttpServletRequest request, HttpServletResponse response);
	
	public String getAuthenticatorName();
	
	public boolean isDisabled();
	
	public int getFactor();
	
	public int getStatus(HttpServletRequest request);
}