/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.sso.agent;

import org.wso2.carbon.identity.sso.agent.exception.SSOAgentException;
import org.wso2.carbon.identity.sso.agent.openid.OpenIDManager;
import org.wso2.carbon.identity.sso.agent.saml.SAMLSSOManager;
import org.wso2.carbon.identity.sso.agent.util.SSOConfigs;
import org.wso2.carbon.identity.sso.agent.util.SSOConstants;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class SSOAgentFilter
 */
public class SSOAgentFilter implements Filter {

    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    private SAMLSSOManager samlSSOManager;
    private OpenIDManager openIdManager;

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException {
        try {
            //Initialize the configurations as 1st step.
            SSOConfigs.initConfigs(fConfig);

            // Initialize
            samlSSOManager = new SAMLSSOManager();
            openIdManager = new OpenIDManager();
            fConfig.getServletContext().addListener("org.wso2.carbon.identity.sso.agent.saml.SSOAgentHttpSessionListener");

        }  catch (SSOAgentException e) {
            throw new ServletException(e);
        }
    }

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        try {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            // This should be SLO SAML Request from IdP
            String samlRequest = request.getParameter(SSOConstants.HTTP_POST_PARAM_SAML2_AUTH_REQ);

            // This could be either SAML Response for a SSO SAML Request by the client application or
            // a SAML Response for a SLO SAML Request from a SP
            String samlResponse = request.getParameter(SSOConstants.HTTP_POST_PARAM_SAML2_RESP);

            String openid_mode = request.getParameter("openid.mode");

            String claimed_id = request.getParameter(SSOConfigs.getClaimedIdParameterName());

            if(SSOConfigs.isSAMLSSOLoginEnabled() && samlRequest != null){
                samlSSOManager.doSLO(request);
            } else if(SSOConfigs.isSAMLSSOLoginEnabled() && samlResponse != null){
                samlSSOManager.processResponse(request);
            } else if(SSOConfigs.isOpenIDLoginEnabled() && openid_mode != null &&
                    !openid_mode.equals("") && !openid_mode.equals("null")){
                openIdManager.processOpenIDLoginResponse(request, response);
            } else if (SSOConfigs.isSAMLSSOLoginEnabled() && request.getRequestURI().endsWith(SSOConfigs.getLogoutUrl())){
                if(request.getSession(false) != null){
                    response.sendRedirect(samlSSOManager.buildRequest(request, true));
                    return;
                }
            } else if(SSOConfigs.isSAMLSSOLoginEnabled() && request.getRequestURI().endsWith(SSOConfigs.getSAMLSSOUrl())){
                response.sendRedirect(samlSSOManager.buildRequest(request, false));
                return;
            } else if(SSOConfigs.isOpenIDLoginEnabled() && request.getRequestURI().endsWith(SSOConfigs.getOpenIdUrl()) &&
                    claimed_id != null && !claimed_id.equals("") && !claimed_id.equals("null")){
                response.sendRedirect(openIdManager.doOpenIDLogin(request, response));
                return;
            } else if (SSOConfigs.isOpenIDLoginEnabled() && !request.getRequestURI().endsWith(SSOConfigs.getLoginUrl()) &&
                    !request.getRequestURI().endsWith(request.getContextPath()) &&
                    !request.getRequestURI().endsWith(request.getContextPath() + "/") &&
                    request.getSession().getAttribute(SSOConfigs.getSubjectIdSessionAttributeName()) == null) {
                response.sendRedirect(samlSSOManager.buildRequest(request,false));
                return;
             }
            // pass the request along the filter chain
            chain.doFilter(request, response);

        } catch (SSOAgentException e){
            LOGGER.log(Level.SEVERE, "An error has occurred", e);
            throw e;
        }
	}


    /**
     * @see Filter#destroy()
     */
    public void destroy() {

    }

}
