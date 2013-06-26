<!--
/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->
<%@ page import="org.w3c.dom.Attr" %>
<%@ page import="org.w3c.dom.Element" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreMgtDataKeeper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="javax.xml.parsers.ParserConfigurationException" %>
<%@ page import="javax.xml.transform.Transformer" %>
<%@ page import="javax.xml.transform.TransformerException" %>
<%@ page import="javax.xml.transform.TransformerFactory" %>
<%@ page import="javax.xml.transform.dom.DOMSource" %>
<%@ page import="javax.xml.transform.stream.StreamResult" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Set" %>

<%
    String editingOrder = CharacterEncoder.getSafeText(request.getParameter("order")).trim();

    int order = 0;

    Map<String, String> properties = new HashMap<String, String>();
    String forwardTo = "index.jsp";
    String BUNDLE = "org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String className = request.getParameterValues("classApplied")[0];

    String description = request.getParameterValues("description")[0];
    String domainId = request.getParameterValues("domainId")[0];
    int defaultProperties = Integer.parseInt(CharacterEncoder.getSafeText(request.getParameter("defaultProperties")).replaceAll("[\\D]", ""));    //number of default properties
    if (!editingOrder.equals("0")) {
        order = Integer.parseInt(editingOrder);
    } else {
        order = UserStoreMgtDataKeeper.getNextRank();
    }


    Set map=request.getParameterMap().keySet();

    String property;
    String value = null;
    for (int i = 0; i < defaultProperties; i++) {


        if (request.getParameter("propertyName_" + i)!= null) {

            if (CharacterEncoder.getSafeText(request.getParameter("propertyValue_" + i)) == null) {
                value = "false";
            } else {
                value = CharacterEncoder.getSafeText(request.getParameter("propertyValue_" + i));
                if (value.equals("null")) {
                    value = "false";
                } else if (value.equals("on")) {
                    value = "true";
                } else {

                }
            }

            property = CharacterEncoder.getSafeText(request.getParameter("propertyName_" + i));
            properties.put(property, value);
        } else {

        }

    }
    properties.put("Class", className);
    properties.put("DomainName", domainId);
    properties.put("Description", description);
    properties.put("Disabled","true") ;                //by default newly added user store is disabled

    UserStoreMgtDataKeeper.addUserStoreManagers(properties, order);

%>

<script type="text/javascript">
    <%UserStoreMgtDataKeeper.addChangedUserStore(domainId);%>
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>