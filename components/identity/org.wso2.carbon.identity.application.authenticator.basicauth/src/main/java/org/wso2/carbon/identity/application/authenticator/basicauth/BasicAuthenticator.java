package org.wso2.carbon.identity.application.authenticator.basicauth;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticatorsConfiguration;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticatorsConfiguration.AuthenticatorConfig;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class BasicAuthenticator implements ApplicationAuthenticator {

	private static Log log = LogFactory.getLog(BasicAuthenticator.class);
	
	private final int CUSTOM_STATUS_SEND_TO_LOGIN = 10;
	private final int CUSTOM_STATUS_AUTHENTICATE = 11;
	
	private final String AUTHENTICATOR_NAME = "BasicAuthenticator";
	private final String AUTHENTICATOR_STATUS = "BasicAuthenticatorStatus";
	
	private RegistryService registryService;
	private RealmService realmService;
	
	AuthenticatorConfig authenticatorConfig;
	private Map<String, String> statusMap;
	
	public BasicAuthenticator(RegistryService registryService, RealmService realmService) {
		this.registryService = registryService;
		this.realmService = realmService;
		
		authenticatorConfig = ApplicationAuthenticatorsConfiguration.getInstance().getAuthenticatorConfig(AUTHENTICATOR_NAME);
		statusMap = authenticatorConfig.getStatusMap();
	}
	
	@Override
    public int doAuthentication(HttpServletRequest request, HttpServletResponse response) {
		int status = getStatus(request);
		
		if (status == CUSTOM_STATUS_AUTHENTICATE
				|| (Boolean)request.getSession().getAttribute(ApplicationAuthenticator.DO_AUTHENTICATION)) {
			if(canHandle(request)){
				try {
    				status = authenticate(request) ? ApplicationAuthenticator.STATUS_AUTHENTICATION_PASS 
    				                               : ApplicationAuthenticator.STATUS_AUTHENTICATION_FAIL;
				} catch (Exception e) {
		            String msg = "Error on BasicAuthenticator authentication";
		            log.error(msg, e);
		            status = ApplicationAuthenticator.STATUS_AUTHENTICATION_FAIL;
		        }
			} else {
				status = ApplicationAuthenticator.STATUS_AUTHENTICATION_CANNOT_HANDLE;
			}
		}
		
		else if(status == CUSTOM_STATUS_SEND_TO_LOGIN){
			String loginPage = statusMap.get(String.valueOf(status));
			status = CUSTOM_STATUS_AUTHENTICATE;
			
			if(ApplicationAuthenticatorsConfiguration.getInstance().isSingleFactor()){
				request.getSession().setAttribute(DO_AUTHENTICATION, Boolean.TRUE);
			}
			
			try {
	            response.sendRedirect(loginPage);
            } catch (IOException e) {
	            e.printStackTrace();
            }
		} 
		
		request.getSession().setAttribute(AUTHENTICATOR_STATUS, status);
		return status;
    }

	@Override
    public int getStatus(HttpServletRequest request) {
		Integer status = (Integer)request.getSession().getAttribute(AUTHENTICATOR_STATUS);
		
		//Read from the configuration , if this is the first time this method is called, 
		if (status == null){
			status = Integer.valueOf(statusMap.entrySet().iterator().next().getKey());
		} 
		
	    return status;
    }
	
	@Override
	public int getFactor(){
	    return authenticatorConfig.getFactor();
	}

    public boolean canHandle(HttpServletRequest request) {

        String userName = request.getParameter("username");
        String password = request.getParameter("password");

        if (userName != null && password != null) {
            return true;
        }

        // This is to login with Remember Me.
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if (cookie.getName().equals(CarbonConstants.REMEMBER_ME_COOKE_NAME)) {
//                    return true;
//                }
//            }
//        }

        return false;
    }
    
    public boolean authenticate(HttpServletRequest request) throws Exception {
    	
    	String username = request.getParameter("username");
        String password = request.getParameter("password");

        boolean isAuthenticated = false;
        UserRealm realm = AnonymousSessionUtil.getRealmByUserName(registryService, realmService, username);

        if (realm == null) {
            log.warn("Realm creation failed. Tenant may be inactive or invalid.");
            return false;
        }

        UserStoreManager userStoreManager = realm.getUserStoreManager();

        // Check the authentication
        isAuthenticated = userStoreManager.authenticate(
                MultitenantUtils.getTenantAwareUsername(username), password);
        if (!isAuthenticated) {
            if (log.isDebugEnabled()) {
                log.debug("user authentication failed due to invalid credentials.");
            }
            return false;
        }
        
        int index = username.indexOf("/");
        
        if (index < 0) {
            String domain = UserCoreUtil.getDomainFromThreadLocal();
            
            if (domain != null) {
                username = domain + "/" + username;
            }
        }

        // Check the authorization
        boolean isAuthorized = realm.getAuthorizationManager().
                isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username),
                        "/permission/admin/login",
                        CarbonConstants.UI_PERMISSION_ACTION);
        
        if (!isAuthorized) {
            if (log.isDebugEnabled()) {
                log.debug("Authorization Failure when performing log-in action");
            }
            return false;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("User is successfully authenticated.");
        }
        
        return true;
    }
}
