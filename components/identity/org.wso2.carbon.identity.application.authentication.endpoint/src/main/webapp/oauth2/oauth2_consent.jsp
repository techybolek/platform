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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
	String loggedInUser = request.getParameter("loggedInUser");
	String app =   request.getParameter("oidcApp");
	String scope = request.getParameter("scope");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>OAuth2 Login</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="assets/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/localstyles.css" rel="stylesheet">
    <!--[if lt IE 8]>
    <link href="css/localstyles-ie7.css" rel="stylesheet">
    <![endif]-->

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="assets/js/html5.js"></script>
    <![endif]-->
    <script src="assets/js/jquery-1.7.1.min.js"></script>
    <script src="js/scripts.js"></script>


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

    <div id="middle">
       <div class="header-text">
    		<strong><%=app%></strong> requests access to your profile information
		</div>
        <div id="workArea"  class="container">
         <div class="row">
        <div class="span5 offset3 content-section">
            <script type="text/javascript">
                function approved() {
                	 location.href = "../oauth2endpoints/authorize";
                }
                            
            </script>

            <form action="../../openidserver" id="profile" name="profile">
                <table width="100%" class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="buttonRow" colspan="2">
                        	<input type="button" class="btn btn-primary btn-large" id="approve" name="approve"
                                             onclick="javascript: approved(); return false;"
                                             value="Approve"/>
                       		
                       		<input class="btn btn-large" type="reset" value="Cancel"
                               onclick="javascript:document.location.href='../admin/login.jsp'"/>
                       </td>
                    </tr>
                    </tbody>
                </table>
            </form>
			</div>
        </div>
    </div>
</div>
</body>
</html>

