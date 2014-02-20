<!DOCTYPE html>
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









    <!DOCTYPE html>
    <!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
    <!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
    <!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
    <!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
        <head>
            <meta charset="utf-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
            <title>Log Into WSO2 Cloud</title>
            <meta name="description" content="">
            <meta name="viewport" content="width=device-width">

            <link rel="stylesheet" href="../samlsso/assets/css/font-awesome.min.css">
            <!--[if IE 7]>
              <link rel="stylesheet" href="../samlsso/assets/css/font-awesome-ie7.min.css">
            <![endif]-->
            <link rel="stylesheet" href="../samlsso/assets/css/normalize.min.css">
    		<link type="text/css" rel="stylesheet" href="../samlsso/assets/css/jquery.qtip.min.css" />
            <link rel="stylesheet" href="../samlsso/assets/css/start.css">
            <link rel="stylesheet" href="../samlsso/assets/css/development.css">


        </head>
        <body>
            <!--[if lt IE 7]>
                <p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">activate Google Chrome Frame</a> to improve your experience.</p>
            <![endif]-->

        <div class="wrapper">
        			<div class="branding">
                        <h1><img src="../samlsso/assets/img/wso2-cloud-logo-2.png" alt="App Factory" /></h1>
                    </div>
     				<article class="start">
                            <header class="start_header">
                                     <h2 class="account">Use your <img src="../samlsso/assets/img/wso2-oxygen-tank.png" alt="wso2 oxygen tank" /> account.</h2>
                            </header>
                            <%@ include file="../authenticator-pages/ot_basicauth.jsp" %>

                     </article>
     		</div><!-- /wrapper -->
            <footer></footer>
            <script type="text/javascript" src="../samlsso/assets/js/vendor/jquery-1.7.1.min.js"></script>
       		<script type="text/javascript"  src="../samlsso/assets/js/start.js"></script>
         </body>
    </html>




</fmt:bundle>
