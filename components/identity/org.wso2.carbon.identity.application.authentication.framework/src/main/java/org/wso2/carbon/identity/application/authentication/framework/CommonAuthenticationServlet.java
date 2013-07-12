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
public class CommonAuthenticationServlet extends HttpServlet {
	
	private static Log log = LogFactory.getLog(CommonAuthenticationServlet.class);
	
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
		
		for(ApplicationAuthenticator authenticator : authenticators){
			int status = authenticator.getStatus(request);
			
			//Authenticator is called if it's not already authenticated and if it's status is not cannot handle.
			if(status != ApplicationAuthenticator.STATUS_AUTHENTICATION_PASS 
					|| status != ApplicationAuthenticator.STATUS_AUTHENTICATION_CANNOT_HANDLE) {
				
				//"canHandle" session attribute is set to indicate atleast one Authenticator can handle the request.
				if(request.getSession().getAttribute("canHandle") == null){
					request.getSession().setAttribute("canHandle", Boolean.TRUE);
				}
				
				status = authenticator.doAuthentication(request, response);
				
				//If Authenticator has set a custom status, it means it's job is not done.  It's sending to a login page.
				if(status != ApplicationAuthenticator.STATUS_AUTHENTICATION_PASS
						&& status != ApplicationAuthenticator.STATUS_AUTHENTICATION_FAIL
						&& status != ApplicationAuthenticator.STATUS_AUTHENTICATION_CANNOT_HANDLE){
					return;
				}
				
				//In single or multi factor modes, one Authenticator failed means whole authentication is failed.
				if(status == ApplicationAuthenticator.STATUS_AUTHENTICATION_FAIL){
		            sendResponseToCallingServlet(request, response, Boolean.FALSE);
		            return;
				}
				
				//If in single-factor mode, no need to check the other Authenticators. Send the response back.
				if(status == ApplicationAuthenticator.STATUS_AUTHENTICATION_PASS && isSingleFactor){
					sendResponseToCallingServlet(request, response, Boolean.TRUE);
					return;
				}
			} 
		}
		
		//If all the Authenticators failed to handle the request
		if(request.getSession().getAttribute("canHandle") == null){
			sendResponseToCallingServlet(request, response, Boolean.FALSE);
		} 
		//Otherwise, authentication has PASSED in multi-factor mode
		else { 
			sendResponseToCallingServlet(request, response, Boolean.TRUE);
		}
	}
	
	private void sendResponseToCallingServlet(HttpServletRequest request, 
	                                          HttpServletResponse response, 
	                                          Boolean isAuthenticated) 
	                                        		  throws ServletException, IOException{
		
		request.getSession().setAttribute(ApplicationAuthenticator.AUTHENTICATED, isAuthenticated);
		
		String callingServlet = (String)request.getSession().getAttribute(ApplicationAuthenticator.CALLING_SERVLET_PATH);
		RequestDispatcher dispatcher = request.getRequestDispatcher(callingServlet);
        dispatcher.forward(request, response);
	}
}