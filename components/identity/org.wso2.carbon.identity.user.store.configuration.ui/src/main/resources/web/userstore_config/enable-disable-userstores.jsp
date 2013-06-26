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
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>

<%@page import="java.util.ResourceBundle"%>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreMgtDataKeeper" %>
<%
    String forwardTo = null;
    String action = request.getParameter("action");
    int order = Integer.parseInt(request.getParameter("order"));
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    if ((request.getParameter("order") != null)) {

        try {
            if(action.equals("enable")){
                UserStoreMgtDataKeeper.getUserStoreManager(order).put("Disabled", String.valueOf(false));
            }
            if(action.equals("disable")){
                UserStoreMgtDataKeeper.getUserStoreManager(order).put("Disabled", String.valueOf(true));
            }
            forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
        } catch (Exception e) {
            String message = resourceBundle.getString("invalid.user.store.not.updated");
            CarbonUIMessage.sendCarbonUIMessage(message,	CarbonUIMessage.ERROR, request);
            forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
        }
    } else {
        forwardTo = "index.jsp?region=region1&item=userstores_mgt_menu";
    }
%>

<script
        type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>