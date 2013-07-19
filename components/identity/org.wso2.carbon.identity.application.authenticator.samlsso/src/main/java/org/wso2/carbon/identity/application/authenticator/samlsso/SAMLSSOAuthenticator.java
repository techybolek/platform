package org.wso2.carbon.identity.application.authenticator.samlsso;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticatorConstants;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticatorsConfiguration;
import org.wso2.carbon.identity.application.authenticator.samlsso.exception.SAMLSSOException;
import org.wso2.carbon.identity.application.authenticator.samlsso.manager.SAMLSSOManager;
import org.wso2.carbon.identity.application.authenticator.samlsso.util.SSOConstants;

public class SAMLSSOAuthenticator extends AbstractApplicationAuthenticator {

	private static Log log = LogFactory.getLog(SAMLSSOAuthenticator.class);
	
	@Override
    public int doAuthentication(HttpServletRequest request, HttpServletResponse response) {
		int status = getStatus(request);
		
		if (status == SSOConstants.CUSTOM_STATUS_AUTHENTICATE
				|| request.getSession().getAttribute(ApplicationAuthenticatorConstants.DO_AUTHENTICATION) != null) {
			
			if(canHandle(request)){
				try {
    				status = authenticate(request) ? ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_PASS 
    				                               : ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_FAIL;
				} catch (Exception e) {
		            String msg = "Error on SAMLSSOAuthenticator authentication";
		            log.error(msg, e);
		            status = ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_FAIL;
		        }
			} else {
				status = ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_CANNOT_HANDLE;
			}
		}
		
		else if(status == SSOConstants.CUSTOM_STATUS_SEND_TO_LOGIN){
			try {
				String loginPage = new SAMLSSOManager(getAuthenticatorConfig().getParameterMap()).buildRequest(request,
				                                                      false,
				                                                      false,
				                                                      getAuthenticatorConfig().getStatusMap()
				                                                                              .get(String.valueOf(status)),
				                                                      getAuthenticatorConfig().getParameterMap());
    			status = SSOConstants.CUSTOM_STATUS_AUTHENTICATE;
    			
    			if(ApplicationAuthenticatorsConfiguration.getInstance().isSingleFactor()){
    				request.getSession().setAttribute(ApplicationAuthenticatorConstants.DO_AUTHENTICATION, Boolean.TRUE);
    			}
    			
    			response.sendRedirect(response.encodeRedirectURL(loginPage));
            } catch (Exception e) {
	            e.printStackTrace();
            }
		} 
		
		if (status != ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_PASS
				&& status != ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_FAIL
				&& status != ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_CANNOT_HANDLE) {
			request.getSession().setAttribute(SSOConstants.AUTHENTICATOR_STATUS, status);
		} else {
			request.getSession().removeAttribute(SSOConstants.AUTHENTICATOR_STATUS);
		}
		
		return status;
    }

	@Override
    public int getStatus(HttpServletRequest request) {
		Integer status = (Integer)request.getSession().getAttribute(SSOConstants.AUTHENTICATOR_STATUS);
		if (status == null){
			status = super.getStatus(request);
		} 
	    return status;
    }
	
    public boolean canHandle(HttpServletRequest request) {
        String samlResponse = request.getParameter("SAMLResponse");

        if (samlResponse != null) {
            return true;
        }

        return false;
    }
    
    public boolean authenticate(HttpServletRequest request) {
    	try {
	        new SAMLSSOManager(getAuthenticatorConfig().getParameterMap()).processResponse(request, getAuthenticatorConfig().getParameterMap());
        } catch (SAMLSSOException e) {
	        e.printStackTrace();
	        return false;
        }
    	return true;
    }

	@Override
    public String getAuthenticatorName() {
	    return SSOConstants.AUTHENTICATOR_NAME;
    }
}
