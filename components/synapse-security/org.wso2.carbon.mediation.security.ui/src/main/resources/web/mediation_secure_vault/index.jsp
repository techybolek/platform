<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>

<link type="text/css" href="../dialog/js/jqueryui/tabs/ui.all.css"
	rel="stylesheet" />
<script type="text/javascript"
	src="../dialog/js/jqueryui/tabs/jquery-1.2.6.min.js"></script>
<script type="text/javascript"
	src="../dialog/js/jqueryui/tabs/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript"
	src="../dialog/js/jqueryui/tabs/jquery.cookie.js"></script>
<!-- This page is included to display messages which are set to request scope or session scope -->
<jsp:include page="../dialog/display_messages.jsp" />

<%
  

%>

<fmt:bundle
	basename="org.wso2.carbon.mediation.secure.vault.ui.i18n.Resources">
	<carbon:breadcrumb label="libs.list.headertext"
		resourceBundle="org.wso2.carbon.mediation.secure.vault.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediation.secure.vault.ui.i18n.JSResources"
		request="<%=request%>" />

	<script type="text/javascript"></script>


	<div id="middle">THIS IS A MEIDATION SECUIRTY CONFIGURATION TEST.</div>




</fmt:bundle>