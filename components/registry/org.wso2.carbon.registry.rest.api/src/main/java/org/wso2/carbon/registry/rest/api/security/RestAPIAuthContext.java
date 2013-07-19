package org.wso2.carbon.registry.rest.api.security;

public class RestAPIAuthContext {
	
	private boolean isAuthorized;
	
	private String userName;
	
	private int tenantId;

	public boolean isAuthorized() {
		return isAuthorized;
	}

	public void setAuthorized(boolean isAuthenticated) {
		this.isAuthorized = isAuthenticated;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}
	
	

}
