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
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreMgtDataKeeper" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreUIUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.ResourceBundle" %>

<%
    String[] checkedList = request.getParameter("checkedList").split(","); //delete these
    String[] orderList = request.getParameter("orderList").split(",");
    LinkedList<String> linkedList = new LinkedList<String>();

    String forwardTo;
    String BUNDLE = "org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    int order;
    for (int i = 0; i < checkedList.length; i++) {
        order = Integer.parseInt(checkedList[i]);
        UserStoreMgtDataKeeper.removeUserStoreManagers(order);
    }
    UserStoreMgtDataKeeper.setEdited(true);

    UserStoreUIUtils userStoreUIUtils = new UserStoreUIUtils();
    userStoreUIUtils.saveConfigurationToFile(orderList);
    String message = resourceBundle.getString("user.stores.deleted");
    String message2 = resourceBundle.getString("wait");
    CarbonUIMessage.sendCarbonUIMessage(message+" "+message2, CarbonUIMessage.INFO, request);
    forwardTo = "index.jsp?";

%>
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