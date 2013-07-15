<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<%@ page import="java.net.URLEncoder" %>
<%

    String scopeString = request.getParameter("scope");

    String authStatus = getSafeText(request.getParameter("auth_status"));
%>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>OAuth2 Login</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="oauth2/assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="oauth2/css/localstyles.css" rel="stylesheet">
    <!--[if lt IE 8]>
    <link href="oauth2/css/localstyles-ie7.css" rel="stylesheet">
    <![endif]-->

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="oauth2/assets/js/html5.js"></script>
    <![endif]-->
    <script src="oauth2/assets/js/jquery-1.7.1.min.js"></script>
    <script src="oauth2/js/scripts.js"></script>


</head>

<body>

<div class="header-strip">&nbsp;</div>
<div class="header-back">
    <div class="container">
        <div class="row">
            <div class="span4 offset3">
                <a class="logo">&nbsp</a>
            </div>
        </div>
    </div>
</div>
<%
    if(!"true".equals(request.getParameter("oidcRequest"))) {
%>
<div class="header-text">
    <strong><%=request.getParameter("application")%></strong> requests access to <strong><%=scopeString%></strong>
</div>
<%
} else {
%>
<div class="header-text">
    <strong>Please login to continue</strong>
</div>
<%
    }
%>

<div class="container">
    <div class="row">
        <div class="span5 offset3 content-section">
            <p class="download-info">
                <a class="btn btn-primary btn-large" id="authorizeLink"><i
                        class="icon-ok icon-white"></i> Authorize</a>
                <a class="btn btn-large" id="denyLink"><i class=" icon-exclamation-sign"></i>
                    Deny</a>
            </p>

            <form class="well form-horizontal" id="loginForm"
                  <% if(!("failed".equals(authStatus))) { %>style="display:none"<% } %>
                  action="../oauth2endpoints/authorize">

                <div class="alert alert-error"
                     id="errorMsg" <% if (!("failed".equals(authStatus))) { %>
                     style="display:none" <% } %>>
                    <% if ("failed".equals(authStatus)) { %>Authentication Failure! Please check the
                    username and password.<% } %>
                </div>
                <!--Username-->
                <div class="control-group">
                    <label class="control-label" for="oauth_user_name">Username:</label>

                    <div class="controls">
                        <input type="text" class="input-large" id='oauth_user_name'
                               name="oauth_user_name">
                    </div>
                </div>

                <!--Password-->
                <div class="control-group">
                    <label class="control-label" for="oauth_user_password">Password:</label>

                    <div class="controls">
                        <input type="password" class="input-large" id='oauth_user_password'
                               name="oauth_user_password">
                    </div>
                </div>

                <div class="form-actions">
                    <button type="button" class="btn btn-primary" id="loginBtn">Login</button>
                    <button class="btn">Cancel</button>
                </div>
            </form>
        </div>
    </div>
</div>
<!-- /container -->


</body>
</html>

<%!
public static String getSafeText(String text) {
    if (text == null) {
        return text;
    }
    text = text.trim();
    if (text.indexOf('<') > -1) {
        text = text.replace("<", "&lt;");
    }
    if (text.indexOf('>') > -1) {
        text = text.replace(">", "&gt;");
    }
    return text;
}
%>