<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>

<%@page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%
    String forwardTo = null;
    String action = request.getParameter("action");
    String domain = request.getParameter("domain");
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient = null;
    if (session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE) != null) {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(cookie, backendServerURL, configContext);

    } else {
        String message = resourceBundle.getString("try.again");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/index.jsp";
    }
    if ((request.getParameter("domain") != null)) {

        try {
            if (action.equals("enable")) {
                userStoreConfigAdminServiceClient.updateUserStore(domain, "false");
            }
            if (action.equals("disable")) {
                userStoreConfigAdminServiceClient.updateUserStore(domain, "true");
            }
            forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
        } catch (Exception e) {
            String message = resourceBundle.getString("invalid.user.store.not.updated");
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
        }
    } else {
        forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>