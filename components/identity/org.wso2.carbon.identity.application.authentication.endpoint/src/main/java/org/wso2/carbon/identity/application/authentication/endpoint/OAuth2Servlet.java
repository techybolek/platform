/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authentication.endpoint;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OAuth2Servlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Map<String, String> pages = new HashMap<String, String>();

	public void init(ServletConfig config) throws ServletException {
		// load all of em
		Enumeration initParams = config.getInitParameterNames();
		while (initParams.hasMoreElements()) {
			String paramName = (String) initParams.nextElement();
			pages.put(paramName, config.getInitParameter(paramName));
		}

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	                                                                              IOException {

		String loginPage = null;
		String errorPage = null;
		String consentPage = null;

		// loading configured pages
		String applicationName = request.getParameter("application");

		if (applicationName != null) {
			String loginPageParam = applicationName.trim() + "-LoginPage";
			String errorPageParam = applicationName.trim() + "-ErrorPage";
			String consentPageParam = applicationName.trim() + "-ConsentPage";

			String value = pages.get(loginPageParam);
			if (value != null) {
				loginPage = value;
			}
			value = pages.get(errorPageParam);
			if (value != null) {
				errorPage = value;
			}
			value = pages.get(consentPageParam);
			if (value != null) {
				consentPage = value;
			}
		}

		// setting the login page
		if (request.getRequestURI().contains("/oauth2_login.do")) {
			if (loginPage != null) {
				response.sendRedirect(loginPage + "?" + request.getQueryString());
			} else if (pages.get("Global-LoginPage") != null) {
				response.sendRedirect(pages.get("Global-LoginPage") + "?" + request.getQueryString());
			} else {
				request.getRequestDispatcher("oauth2/oauth2_login.jsp").forward(request, response);
			}

		// setting the error page	
		} else if (request.getRequestURI().contains("/oauth2_error.do")) {
			if (errorPage != null) {
				response.sendRedirect(errorPage + "?" + request.getQueryString());
			} else if (pages.get("Global-ErrorPage") != null) {
				response.sendRedirect(pages.get("Global-ErrorPage") + "?" + request.getQueryString());
			} else {
				request.getRequestDispatcher("oauth2/oauth2_error.jsp").forward(request, response);
			}

		// setting the consent page	
		} else if (request.getRequestURI().contains("/oauth2_consent.do")) {
			if (consentPage != null) {
				response.sendRedirect(consentPage + "?" + request.getQueryString());
			} else if (pages.get("Global-ConsentPage") != null) {
				response.sendRedirect(pages.get("Global-ConsentPage") + "?" + request.getQueryString());
			} else {
				request.getRequestDispatcher("oauth2/oauth2_consent.jsp").forward(request, response);
			}

		} else {
			request.getRequestDispatcher("oauth2/oauth2_error.jsp").forward(request, response);

		}

	}

}
