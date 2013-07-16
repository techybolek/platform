package org.wso2.carbon.identity.application.authentication.framework;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.internal.ApplicationAuthenticationFrameworkServiceComponent;

@SuppressWarnings("serial")
public class CommonApplicationAuthenticationServlet extends HttpServlet {
	
	private static Log log = LogFactory.getLog(CommonApplicationAuthenticationServlet.class);
	
	public ApplicationAuthenticator[] authenticators;
	private final boolean isSingleFactor = ApplicationAuthenticatorsConfiguration.getInstance().isSingleFactor();
	
	@Override
	public void init(){
		authenticators = ApplicationAuthenticationFrameworkServiceComponent.authenticators;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		for (ApplicationAuthenticator authenticator : authenticators) {
			
			if (!authenticator.isDisabled()){
				int status = authenticator.getStatus(request);
				
				//Authenticator is called if it's not already AUTHENTICATED and if its status is not CONNOT HANDLE.
				if(status != ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_PASS 
						|| status != ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_CANNOT_HANDLE) {
					
					//"canHandle" session attribute is set to indicate atleast one Authenticator can handle the request.
					if(request.getSession().getAttribute("canHandle") == null){
						request.getSession().setAttribute("canHandle", Boolean.TRUE);
					}
					
					status = authenticator.doAuthentication(request, response);
					
					//Authenticator setting a custom status means, its job is not completed yet.
					if(status != ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_PASS
							&& status != ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_FAIL
							&& status != ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_CANNOT_HANDLE){
						if (log.isDebugEnabled()) {
							log.debug(authenticator.getAuthenticatorName() + " has set custom status code: " + String.valueOf(status));
						}
						return;
					}
					
					//In single or multi factor modes, one Authenticator failing means whole authentication chain is failed.
					if(status == ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_FAIL){
						if (log.isDebugEnabled()) {
							log.debug("Authentication failed for " + authenticator.getAuthenticatorName());
						}
			            sendResponseToCallingServlet(request, response, Boolean.FALSE);
			            return;
					}
					
					//If in single-factor mode, no need to check the other Authenticators. Send the response back.
					if(status == ApplicationAuthenticatorConstants.STATUS_AUTHENTICATION_PASS && isSingleFactor){
						if (log.isDebugEnabled()) {
							log.debug("Authenticaticated by " + authenticator.getAuthenticatorName() + " in single-factor mode");
						}
						sendResponseToCallingServlet(request, response, Boolean.TRUE);
						return;
					}
				} 
			}
		}
		
		//If all the Authenticators failed to handle the request
		if(request.getSession().getAttribute("canHandle") == null){
			if (log.isDebugEnabled()) {
				log.debug("No Authenticator can handle the request");
			}
			sendResponseToCallingServlet(request, response, Boolean.FALSE);
		} 
		//Otherwise, authentication has PASSED in multi-factor mode
		else { 
			if (log.isDebugEnabled()) {
				log.debug("Authenticared passed in multi-factor mode");
			}
			sendResponseToCallingServlet(request, response, Boolean.TRUE);
		}
	}
	
	private void sendResponseToCallingServlet(HttpServletRequest request, 
	                                          HttpServletResponse response, 
	                                          Boolean isAuthenticated) 
	                                        		  throws ServletException, IOException{
		request.getSession().setAttribute(ApplicationAuthenticatorConstants.AUTHENTICATED, isAuthenticated);
		String callingServlet = (String)request.getSession().getAttribute(ApplicationAuthenticatorConstants.CALLING_SERVLET_PATH);
		
		if (log.isDebugEnabled()) {
			log.debug("Sending response back to: " + callingServlet);
		}
		
		RequestDispatcher dispatcher = request.getRequestDispatcher(callingServlet);
        dispatcher.forward(request, response);
	}
}