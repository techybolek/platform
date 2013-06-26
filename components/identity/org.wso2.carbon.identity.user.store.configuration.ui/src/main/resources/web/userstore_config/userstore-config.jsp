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
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.api.Property" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.UserStoreConfigConstantsKeeper" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreMgtDataKeeper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.sun.jmx.remote.util.OrderClassLoaders" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%!
    private String propertyValue;
    private String propertyName;
    private ArrayList<Property> properties;
    private String forwardTo;
    private String order;
    private String className;
    private Boolean isEditing;
    private ArrayList mandatory=new ArrayList();

    private int isBoolean(String value) {
        int i = -123;
        if (value.equalsIgnoreCase("true")) {
            i = 1;
        } else if (value.equalsIgnoreCase("false")) {
            i = 0;
        }
        return i;
    }

%><%
    if(request.getParameter("order")!=null){
    order = CharacterEncoder.getSafeText(request.getParameter("order"));
    }

    if(request.getParameter("className")!=null) {
    className = CharacterEncoder.getSafeText(request.getParameter("className"));
    }
    String selectedClassApplied = null;
    String description = null;
    String domainId = null;
    int rank;
    String[] classApplies = new String[0];

    if (className.equals("0")) {
        selectedClassApplied = request.getParameter("classApplied");       //add
        isEditing = false;
    } else {
        selectedClassApplied = className;                                  //edit
        isEditing = true;
    }


    if (selectedClassApplied == null || selectedClassApplied.trim().length() == 0) {
        selectedClassApplied = UserStoreConfigConstantsKeeper.RWLDAP_USERSTORE_MANAGER;
    } else {

    }

    if (session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE) == null) {
        CarbonUIMessage.sendCarbonUIMessage("Your session has timed out. Please try again.", CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/index.jsp";
    } else {

    }

    if (session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE) != null) {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(cookie, backendServerURL, configContext);
        classApplies = userStoreConfigAdminServiceClient.getAvailableUserStoreClasses();
        if (!order.equals("0")) {

            rank = Integer.parseInt(order);

            //Get the defined properties of user store manager
            Map<String, String> tempProperties = UserStoreMgtDataKeeper.getUserStoreManager(rank);
            className = tempProperties.get("Class");
            description = tempProperties.get("Description");
            domainId = tempProperties.get("DomainName");
            String forwardTo = "index.jsp";

            //Get the default user store properties
            properties = userStoreConfigAdminServiceClient.getUserStoreProperties(className);
            Property property;
            for (int i = 0; i < properties.size(); i++) {
                property = properties.get(i);
                if (tempProperties.get(property.getName()) != null) {
                    properties.get(i).setValue(tempProperties.get(property.getName()));
                    System.out.println("Order"+rank);
                }
            }


        } else {
            properties = userStoreConfigAdminServiceClient.getUserStoreProperties(selectedClassApplied);
        }

    } else {
        CarbonUIMessage.sendCarbonUIMessage("Your session has timed out. Please try again.", CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/index.jsp";
    }


%>


<fmt:bundle basename="org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources">
<carbon:breadcrumb
        label="user.store.manager"
        resourceBundle="org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<%--<script src="../entitlement/js/policy-editor.js" type="text/javascript"></script>--%>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>


<script type="text/javascript">
    jQuery(document).ready(function () {
        jQuery('#domainId').keyup(function(){
                    $('#userStoreTypeSub strong').html(
                            $(this).val()
                    );
                }
        );
    });




    var allPropertiesSelected = false;
    function doSubmit() {
        if (doValidationDomainNameOnly()) {
//            if(doValidationMandatoryProperties()){

            preSubmit();
            if (<%=Integer.parseInt(order)!=0%>) {
                document.dataForm.action = "userstore-config-finish.jsp?order= " +<%=order%>;
                document.dataForm.submit();
            } else {
                document.dataForm.action = "userstore-config-finish.jsp?order=0";
                document.dataForm.submit();
            }
            }

//        }
    }

    function preSubmit() {

        var propertiesTable = "";


        if (document.getElementById('propertiesTable') != null) {
            propertiesTable = jQuery(document.getElementById('propertiesTable').rows[document.
                    getElementById('propertiesTable').rows.length - 1]).attr('data-value');
        }


        jQuery('#mainTable > tbody:last').append('<tr><td><input type="hidden" name="exProperty" id="exProperty" value="' + propertiesTable + '"/></td></tr>');

    }

    function selectAllInThisPage(isSelected) {
        allPropertiesSelected = false;
        if (document.dataForm.userStores != null &&
                document.dataForm.userStores [0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.dataForm.userStores.length; j++) {
                    document.dataForm.userStores [j].checked = true;
                }
            } else {
                for (j = 0; j < document.dataForm.userStores.length; j++) {
                    document.dataForm.userStores [j].checked = false;
                }
            }
        } else if (document.dataForm.userStores != null) { // only 1 service
            document.dataForm.userStores.checked = isSelected;
        }
        return false;
    }

    function resetVars() {
        allPropertiesSelected = false;

        var isSelected = false;
        if (document.dataForm.userStores != null) { // there is more than 1 service
            for (var j = 0; j < document.dataForm.userStores.length; j++) {
                if (document.dataForm.userStores [j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.dataForm.userStores != null) { // only 1 service
            if (document.dataForm.userStores.checked) {
                isSelected = true;
            }
        }
        return false;
    }

    function deleteUserStores() {
        var selected = false;
        if (document.dataForm.userStores[0] != null) { // there is more than 1 policy
            for (var j = 0; j < document.dataForm.userStores.length; j++) {
                selected = document.dataForm.userStores[j].checked;
                if (selected) break;
            }
        } else if (document.dataForm.userStores != null) { // only 1 policy
            selected = document.dataForm.userStores.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.user.stores.to.be.deleted"/>');
            return;
        }
        if (allPropertiesSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.user.stores.prompt"/>", function () {
                document.dataForm.action = "remove-policy.jsp";
                document.dataForm.submit();
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.user.stores.on.page.prompt"/>", function () {
                document.dataForm.action = "remove-policy.jsp";
                document.dataForm.submit();
            });
        }
    }

    function doCancel() {
        location.href = "index.jsp";
    }

    function removeRow(link) {
        link.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.
                removeChild(link.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode);
    }

    function createNewPropertyRow(name, value) {
        var rowIndex = jQuery(document.getElementById('propertiesTable').rows[document.
                getElementById('propertiesTable').rows.length - 1]).attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#propertiesTable > tbody:last').append('<tr data-value="' + index + '"><td><table class="oneline-listing"><tr></tr><tr>' +
                '<td><input type="text" name="expropertyName_' + index + '" id="expropertyName_' + index + '" value="name"/></td>' +
                '<td><input type="text" name="expropertyValue_' + index + '" id="expropertyValue_' + index + '" value="value"/></td>' +
                '</tr><tr></tr></table></td></tr>');
    }

    function createNewMandatoryPropertyRow() {
        var rowIndex = jQuery(document.getElementById('mandatoryPropertiesTable').rows[document.
                getElementById('mandatoryPropertiesTable').rows.length - 1]).attr('data-value');
        var index = parseInt(rowIndex, 10) + 1;
        jQuery('#mandatoryPropertiesTable > tbody:last').append('<tr data-value="' + index + '"><td><table class="oneline-listing"><tr></tr><tr>' +
                '<td><input type="text" name="mpropertyName_' + index + '" id="mpropertyName_' + index + '" value="<%=""%>"/></td>' +
                '<td><input type="text" name="mpropertyValue_' + index + '" id="mpropertyValue_' + index + '" value="<%=""%>"/></td>' +
                '</tr><tr></tr></table></td></tr>');
    }


    function getCategoryType() {
        document.dataForm.submit();
    }

    function getDomainId() {
        document.dataForm.submit();
    }

    function doValidationDomainNameOnly() {

        var value = document.getElementsByName("domainId")[0].value;
        if (value == '') {
            CARBON.showWarningDialog('<fmt:message key="domain.name.is.required"/>');
            return false;
        }

        return true;
    }


</script>

<div id="middle">
<h2><fmt:message key="user.store.manager.configuration"/></h2>

<div id="workArea">
<form id="dataForm" name="dataForm" method="post" action="">
<div class="sectionSeperator"><fmt:message key="user.store.manager.configuration"/></div>
<div class="sectionSub">
    <table id="mainTable">
        <tr>
            <td class="leftCol-med"><fmt:message key="domain.name"/><span class="required">*</span></td>
            <%
                if (domainId != null && domainId.trim().length() > 0) {
            %>
            <td><input type="text" name="domainId" id="domainId" width="" value="<%=domainId%>"
                       onchange="getDomainId();"/></td>
            <%
            } else {
            %>
            <td><input type="text" name="domainId" id="domainId"/></td>
            <%
                }
            %>
        </tr>
        <tr>
            <td><fmt:message key="description"/></td>
            <%
                if (description != null && description.trim().length() > 0) {
            %>
            <td><textarea name="description" id="description" class="text-box-big"><%=description%>
            </textarea>
            </td>
            <%
            } else {
            %>
            <td><textarea type="text" name="description" id="description" class="text-box-big"></textarea>
            </td>
            <%
                }
            %>
        </tr>
        <tr>
            <td class="leftCol-small">
                <fmt:message key="user.store.manager.class"/>
            </td>
            <td>
                <select id="classApplied" name="classApplied" onchange="getCategoryType();">
                    <%
                        for (String classApply : classApplies) {
                            if (selectedClassApplied != null && classApply.equals(selectedClassApplied)) {     //${}
                    %>
                    <option value="<%=classApply%>" selected="selected"><%=classApply%>
                    </option>
                    <%
                    } else {
                    %>
                    <option value="<%=classApply%>"><%=classApply%>
                    </option>
                    <%
                            }
                        }
                    %>
                </select>


                <div class="sectionHelp">
                    <fmt:message key="user.store.manager.properties.define"/>.
                </div>
            </td>
        </tr>
    </table>
</div>
    <%--END Basic information section --%>

    <%--**********************--%>
    <%--**********************--%>
    <%--START properties--%>
    <%--**********************--%>
    <%--**********************--%>
<div class="sectionSeperator" id="userStoreTypeSub"><%="Define Properties For "%><strong></strong></div>
<div class="sectionSub">
        <%--MandatoryProperties--%>
    <table id="mandatoryPropertiesTable">
        <tr data-value="0">
            <td>
                <table class="oneline-listing" style="width: 100%;margin-top:10px;">
                    <thead>
                    <tr>
                        <th style="width:20%" style="text-align:left;"><fmt:message
                                key='property.name'/></th>
                        <th style="width:30%" style="text-align:left;"><fmt:message
                                key='property.value'/></th>
                        <th style="width:50%" style="text-align:left;"><fmt:message
                                key='description'/></th>

                    </tr>
                    </thead>
                    <tbody>
                    <tr></tr>
                    <%
                        int i = 1;
                        int length = properties.size();
                        int isBoolean = -123;

                        for (int j = 0; j < length; j++) {
                            propertyName = properties.get(j).getName();
                            propertyValue = properties.get(j).getValue();
                            if(properties.get(j).getDescription()!=null){
                                description = properties.get(j).getDescription();
                            }

                            Boolean isMandatory = properties.get(j).getMandatory();

                            if (!isMandatory) {

                                continue;
                            }

                            if(propertyValue!=null){
                                isBoolean = isBoolean(propertyValue);
                            }

                            String name = "propertyName_" + i;
                            String value = "propertyValue_" + i;
                            mandatory.add(value);
                    %>
                    <tr>
                        <%
                            if (propertyName != null && propertyName.trim().length() > 0) {

                        %>
                        <td class="leftCol-med" width="50%" style="text-align:left;"><%=propertyName%><span
                                class="required">*</span></td>
                        <input type="hidden" name=<%=name%> id=<%=name%> value="<%=propertyName%>" />

                        <%
                        } else {
                        %>

                        <%
                            }
                        %>

                        <td style="width:30%" style="text-align:left;">
                            <%
                                if (propertyValue!=null) {

                                    if (isBoolean == 1) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox" checked/>
                            <%
                            } else if (isBoolean == 0) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox"/>
                            <%
                            } else if (propertyName.endsWith("password")||propertyName.endsWith("Password")) { %>
                            <input type="password" name=<%=value%> id=<%=value%> style="width:95%"
                                   value="<%=propertyValue%>"/>
                            <%
                            } else {
                            %>
                            <input type="text" name=<%=value%> id=<%=value%>  style="width:95%"
                                   value="<%=propertyValue%>"/>
                            <%
                                }
                            %>


                            <%
                                } else {

                                }
                            %>
                        </td>
                        <td class="sectionHelp" width="50%" style="text-align:left; !important">
                            <%=description%>
                        </td>

                    </tr>
                    <%
                            i++;

                        }
                    %>

                    </tbody>
                </table>
            </td>
        </tr>
    </table>
</div>


<%--Define optional properties--%>

<div class="sectionSeperator"><fmt:message key='optional'/></div>
<div class="sectionSub">
        <%--Optional properties--%>
    <table id="propertiesTable">
        <tr data-value="0">
            <td>
                <table class="oneline-listing" style="width: 100%;margin-top:10px;">
                    <thead>
                    <tr>

                        <th style="width:20%" style="text-align:left;"><fmt:message
                                key='property.name'/></th>
                        <th style="width:30%" style="text-align:left;"><fmt:message
                                key='property.value'/></th>
                        <th style="width:50%" style="text-align:left;"><fmt:message
                                key='description'/></th>
                    </tr>
                    </thead>
                    <%

                        isBoolean = -123;

                        for (int x = 0; x < length; x++) {
                            propertyName = properties.get(x).getName();
                            propertyValue = properties.get(x).getValue();
                            Boolean isMandatory = properties.get(x).getMandatory();


                            if (isMandatory) {
                                continue;
                            }

                            if(properties.get(x).getDescription()!=null){
                            description = properties.get(x).getDescription();
                            }

                            if(propertyValue!=null){
                            isBoolean = isBoolean(propertyValue);
                            }
//                            System.out.println("Name"+propertyName+propertyValue+isMandatory);
                            String name = "propertyName_" + i;
                            String value = "propertyValue_" + i;
                    %>
                    <tr>

                        <%
                            if (propertyName != null && propertyName.trim().length() > 0) {

                        %>
                        <td class="leftCol-med" width="50%" style="text-align:left;" id="<%=name%>"><%=propertyName%></td>
                        <input type="hidden" name=<%=name%> id=<%=name%> value="<%=propertyName%>" />
                        <%
                        } else {
                        %>

                        <%
                            }
                        %>

                        </td>
                        <td style="width:30%" style="text-align:left;">
                            <%
                                if (propertyValue!=null) {
                                    if (isBoolean == 1) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox" checked/>

                            <%
                            } else if (isBoolean == 0) { %>
                            <input type="checkbox" name=<%=value%> id=<%=value%>
                                   class="checkbox"/>
                            <%

                            } else {
                            %>
                            <input type="text" name=<%=value%> id=<%=value%> style="width:95%"
                                   value="<%=propertyValue%>"/>
                            <%
                                }
                            %>


                            <%
                                } else {

                                }
                            %>
                        </td>
                        <td class="sectionHelp" width="50%" style="text-align:left; !important">
                            <%=description%>
                        </td>

                    </tr>
                    <%
                            i++;
                        }
                    %>
                    </tr>

                    <tbody>

                    <%--<tr></tr>--%>
                    </tbody>
                </table>
                <input type="hidden" name="defaultProperties"  id="defaultProperties" value=<%=i%> />
            </td>
        </tr>
    </table>


</div>


</div>

</div>


<%--********************--%>
<%--********************--%>
<%--END properties--%>
<%--********************--%>
<%--********************--%>
<%--********************--%>


<div class="buttonRow">
    <input type="button" onclick="doSubmit();" value="<fmt:message key="finish"/>"
           class="button"/>
    <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />"
           class="button"/>
</div>

</form>
</div>
</div>
</fmt:bundle>
