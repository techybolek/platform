<!--
 ~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.samlsso.SAMLSSOConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.samlsso.Resources">
    <%
        String errorMessage = "login.fail.message";
        boolean loginFailed = false;
        if (request.getParameter(SAMLSSOConstants.AUTH_FAILURE) != null &&
            "true".equals(request.getParameter(SAMLSSOConstants.AUTH_FAILURE))) {
            loginFailed = true;
            if(request.getParameter(SAMLSSOConstants.AUTH_FAILURE_MSG) != null){
                errorMessage = (String) request.getParameter(SAMLSSOConstants.AUTH_FAILURE_MSG);
            }
        }
    %>

    <script type="text/javascript">
        function doLogin() {
            var loginForm = document.getElementById('loginForm');
            loginForm.submit();
        }
    </script>

    <link media="all" type="text/css" rel="stylesheet" href="css/main.css"/>
<table id="main-table" border="0" cellspacing="0">
    <tbody><tr>
        <td id="header" colspan="3">
	    	<div id="header-div">
			<div class="right-logo">Management Console</div>
			<div class="left-logo">
		    		<div  class="header-home">&nbsp;</div>
			</div>
		
			<div class="header-links">
				<div class="right-links">            
			
				</div>
			</div>
	    	</div>

        </td>
    </tr>
    <tr>
	<td colspan="3" class="content">

		    <div id="middle">
        <h2><fmt:message key='saml.sso'/></h2>

        <div id="workArea">
            <%@ include file="../authenticator-pages/basicauth.jsp" %>
        </div>
    </div>
	</td>		
    </tr>

    <tr>
        <td id="footer" colspan="3">

		<div id="footer-div">
			<div class="footer-content">
				<div class="copyright">
				    © 2008 - 2013 WSO2 Inc. All Rights Reserved.
				</div>
			</div>
		</div>
                        
	</td>
    </tr>
</tbody></table>



</fmt:bundle>
