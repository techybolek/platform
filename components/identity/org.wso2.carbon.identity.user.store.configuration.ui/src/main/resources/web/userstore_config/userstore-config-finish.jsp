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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.dto.PropertyDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.dto.UserStoreDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
        <%@ page import="java.util.ResourceBundle" %>
        <%@ page import="java.util.ArrayList" %>

        <%
    Map<String, String> properties = new HashMap<String, String>();
    String forwardTo = "index.jsp";
    String BUNDLE = "org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String className = request.getParameterValues("classApplied")[0];
    String domain = request.getParameterValues("domainId")[0];
    String description = request.getParameterValues("description")[0];
    int defaultProperties = Integer.parseInt(CharacterEncoder.getSafeText(request.getParameter("defaultProperties")).replaceAll("[\\D]", ""));    //number of default properties

    UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient = null;
    try{if (session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE) != null) {
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

//    Set map = request.getParameterMap().keySet();

    UserStoreDTO userStoreDTO = new UserStoreDTO();
    ArrayList<PropertyDTO> propertyList = new ArrayList<PropertyDTO>();
    String value = null;
    for (int i = 0; i < defaultProperties; i++) {
        PropertyDTO propertyDTO = new PropertyDTO();

        if (request.getParameter("propertyName_" + i) != null) {

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
            propertyDTO.setName(CharacterEncoder.getSafeText(request.getParameter("propertyName_" + i)));
            propertyDTO.setValue(value);
            propertyList.add(propertyDTO);
        } else {

        }

    }
            userStoreDTO.setDomainId(domain);
            userStoreDTO.setDescription(description);
            userStoreDTO.setClassName(className);
            userStoreDTO.setDisabled(true);
            userStoreDTO.setProperties(propertyList.toArray(new PropertyDTO[propertyList.size()]));
            userStoreConfigAdminServiceClient.saveConfigurationToFile(userStoreDTO);
        String message = resourceBundle.getString("successful.update");
        CarbonUIMessage.sendCarbonUIMessage(message,CarbonUIMessage.INFO, request);
    } catch (Exception e) {
        String message = resourceBundle.getString("error.update");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
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