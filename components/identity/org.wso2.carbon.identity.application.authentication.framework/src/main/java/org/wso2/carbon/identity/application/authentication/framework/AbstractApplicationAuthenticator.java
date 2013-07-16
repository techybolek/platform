package org.wso2.carbon.identity.application.authentication.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractApplicationAuthenticator implements ApplicationAuthenticator {

	@Override
    public abstract int doAuthentication(HttpServletRequest request, HttpServletResponse response);

	@Override
	public boolean isDisabled() {
		if (getAuthenticatorConfig() != null){
			return getAuthenticatorConfig().isDisabled();
		}
		return true;
	}

	@Override
    public int getFactor() {
		if (getAuthenticatorConfig() != null){
			return getAuthenticatorConfig().getFactor();
		}
		return -1;
    }
	
	@Override
    public int getStatus(HttpServletRequest request) {
		if (getAuthenticatorConfig() != null && getAuthenticatorConfig().getStatusMap() != null){
			return Integer.valueOf(getAuthenticatorConfig().getStatusMap().entrySet().iterator().next().getKey());
		}
		return -1;
	}
	
	protected ApplicationAuthenticatorsConfiguration.AuthenticatorConfig getAuthenticatorConfig() {
		return ApplicationAuthenticatorsConfiguration.getInstance().getAuthenticatorConfig(getAuthenticatorName());
    }
	
	protected boolean isSingleFactorMode(){
		return ApplicationAuthenticatorsConfiguration.getInstance().isSingleFactor();
	}
}