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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<%--<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.config.UserStoreDTO" %>--%>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.stub.config.UserStoreDTO" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.client.UserStoreConfigAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.user.store.configuration.ui.utils.UserStoreMgtDataKeeper" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.user.core.UserStoreConfigConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.List" %>

<fmt:bundle basename="org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources">
<carbon:breadcrumb
        label="new.userstore"
        resourceBundle="org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>
<%--<%!private Boolean isDisabled = false;%>--%>
<%
    String forwardTo = "index.jsp";
    String BUNDLE = "org.wso2.carbon.identity.user.store.configuration.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    ArrayList orderList = null;


    UserStoreDTO[] userStoreDTOs;
    if (session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE) != null) {
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        UserStoreConfigAdminServiceClient userStoreConfigAdminServiceClient = new UserStoreConfigAdminServiceClient(cookie, backendServerURL, configContext);
        if (!UserStoreMgtDataKeeper.getEdited()) {
            userStoreDTOs = userStoreConfigAdminServiceClient.getActiveDomains();
            int order;
            String description;
            String className;
            String isDisabled;
            Map<String, String> userStoreProperties;
            for (UserStoreDTO userStoreDTO : userStoreDTOs) {
                order = userStoreDTO.getOrder();
                description = userStoreDTO.getDescription();
                className = userStoreDTO.getClassName();
                isDisabled = String.valueOf(userStoreDTO.getDisabled());
                userStoreProperties = userStoreConfigAdminServiceClient.getActiveUserStoreProperties(order);
                userStoreProperties.put("Description", description);
                userStoreProperties.put("Class", className);
                userStoreProperties.put("Disabled", isDisabled);
                UserStoreMgtDataKeeper.addUserStoreManagers(userStoreProperties, order);
            }

        } else {
            userStoreDTOs = UserStoreMgtDataKeeper.getUserStoreManagersBasics();
        }


        UserStoreMgtDataKeeper.setConfigProperties(userStoreConfigAdminServiceClient.getConfigProperties());
        UserStoreMgtDataKeeper.setAuthzProperties(userStoreConfigAdminServiceClient.getAuthzProperties());

    } else {
        userStoreDTOs = new UserStoreDTO[0];                                         //to avoid initialization issue
        CarbonUIMessage.sendCarbonUIMessage("Your session has timed out. Please try again.", CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/index.jsp";
    }
%>

<%--<%! private Boolean isEdited=UserStoreMgtDataKeeper.getEdited();--%>


<%--%>--%>
<%--<%! private void setEdited(Boolean edited) { isEdited=edited;} %>--%>


<script type="text/javascript">


        var allUserStoresSelected = false;

        function updownthis(thislink, updown) {
            var sampleTable = document.getElementById('dataTable');
            var clickedRow = thislink.parentNode.parentNode;
            var addition = -1;
            if (updown == "down") {
                addition = 1;
            }
            var otherRow = sampleTable.rows[clickedRow.rowIndex + addition];
            var numrows = jQuery("#dataTable tbody tr").length;
            if (numrows <= 1) {
                return;
            }
            if (clickedRow.rowIndex == 1 && updown == "up") {
                return;
            } else if (clickedRow.rowIndex == numrows + 1 && updown == "down") {
                return;
            }
            var rowdata_clicked = new Array();
            for (var i = 0; i < clickedRow.cells.length; i++) {
                rowdata_clicked.push(clickedRow.cells[i].innerHTML);
                clickedRow.cells[i].innerHTML = otherRow.cells[i].innerHTML;
            }
            for (i = 0; i < otherRow.cells.length; i++) {
                otherRow.cells[i].innerHTML = rowdata_clicked[i];
            }
        }


        function selectAllInThisPage(isSelected) {
            allUserStoresSelected = false;
            if (document.userStoreForm.userStores != null &&
                    document.userStoreForm.userStores[0] != null) { // there is more than 1 service
                if (isSelected) {
                    for (var j = 0; j < document.userStoreForm.userStores.length; j++) {
                        document.userStoreForm.userStores[j].checked = true;
                    }
                } else {
                    for (j = 0; j < document.userStoreForm.userStores.length; j++) {
                        document.userStoreForm.userStores[j].checked = false;
                    }
                }
            } else if (document.userStoreForm.userStores != null) { // only 1 service
                document.userStoreForm.userStores.checked = isSelected;
            }
            return false;
        }

        function resetVars() {
            allUserStoresSelected = false;

            var isSelected = false;
            if (document.userStoreForm.userStores != null) { // there is more than 1 service
                for (var j = 0; j < document.userStoreForm.userStores.length; j++) {
                    if (document.userStoreForm.userStores[j].checked) {
                        isSelected = true;
                    }
                }
            } else if (document.userStoreForm.userStores != null) { // only 1 service
                if (document.userStoreForm.userStores.checked) {
                    isSelected = true;
                }
            }
            return false;
        }


        function deleteUserStores() {
            var selected = false;
            if (document.userStoreForm.userStores[0] != null) { // there is more than 1 user store
                for (var j = 0; j < document.userStoreForm.userStores.length; j++) {
                    selected = document.userStoreForm.userStores[j].checked;
                    if (selected) break;
                }
            } else if (document.userStoreForm.userStores != null) { // only 1 user store
                selected = document.userStoreForm.userStores.checked;
            }
            if (!selected) {
                CARBON.showInfoDialog('<fmt:message key="select.user.stores.to.be.deleted"/>');
                return;
            }
            if (allUserStoresSelected) {
                CARBON.showConfirmationDialog("<fmt:message key="delete.all.user.stores.prompt"/>", function () {

                });
            } else {
                CARBON.showConfirmationDialog("<fmt:message key="delete.user.stores.on.page.prompt"/>", function () {
                    var checkedList = new Array();
                    jQuery("input:checked").each(function () {
                        checkedList.push($(this).val());
                    });

                    var orderList = new Array();
                    jQuery('.valueCell a').each(function () {
                        orderList.push($(this).html().trim());
                    });
                    document.userStoreForm.action = "remove-userstore.jsp?checkedList=" + checkedList + "&orderList=" + orderList;
                    document.userStoreForm.submit();
                });
            }
        }

        function doUpdate() {
            var orderList = new Array();
            jQuery('.valueCell a').each(function () {
                orderList.push($(this).html().trim());
            });
            document.userStoreForm.action = "index-update.jsp?orderList=" + orderList;
            document.userStoreForm.submit();

        }

        function doCancel() {
            document.userStoreForm.action = "cancel.jsp";
            document.userStoreForm.submit();
        }

        function edit(order, className) {
//        UserStoreMgtDataKeeper.setEdited(true);
            <%UserStoreMgtDataKeeper.setEdited(true);%>
            document.userStoreForm.action = "userstore-config.jsp?order= " + order + "&className= " + className;
            document.userStoreForm.submit();

        }

        function enable(order) {
            location.href = "enable-disable-userstores.jsp?order=" + order + "&action=enable";

        }

        function disable(order) {
            location.href = "enable-disable-userstores.jsp?order=" + order + "&action=disable";

        }

    //    function getOrder() {
    //        var orderList = new Array();
    //        jQuery('.valueCell a').each(function () {
    //            orderList.push($(this).html());
    //        });
    //        return orderList;
    //    }
</script>


<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>


<div id="middle">
    <h2><fmt:message key='userstore.config'/></h2>

    <div id="workArea">
        <table style="margin-top:10px;margin-bottom:10px" width="40%">
            <tbody>
            <tr>
                <td>
                    <a style="cursor: pointer;" onclick="selectAllInThisPage(true);return false;" href="#"><fmt:message
                            key="selectAllInPage"/></a>
                    | <a style="cursor: pointer;" onclick="selectAllInThisPage(false);return false;"
                         href="#"><fmt:message
                        key="selectNone"/></a>
                </td>
                <td>
                    <a onclick="deleteUserStores();return false;" href="#" class="icon-link"
                       style="background-image: url(images/delete.gif);"><fmt:message key="delete"/></a>
                </td>
                <td width="10%" style="text-align:center; !important">
                    <a title="<fmt:message key='add'/>"
                       onclick="edit('0','0');"
                       href="#" style="background-image: url(images/add.gif);" class="icon-link">
                        <fmt:message key='add.user.store'/></a>
                </td>
            </tr>
            </tbody>
        </table>

        <form id="userStoreForm" action="" name="userStoreForm" method="post">
            <table style="width: 100%" id="dataTable" class="styledLeft">
                <thead>
                <tr>

                    <th style="width:6%" style="text-align:left;"><fmt:message key='organize'/></th>
                    <th style="width:6%" style="text-align:left;"><fmt:message key='select'/></th>
                    <th style="width:15%" style="text-align:left;"><fmt:message key='domain.name'/></th>
                    <th style="width:25%" style="text-align:left;"><fmt:message key='class.description'/></th>
                    <th style="width:25%" style="text-align:left;"><fmt:message key='class.name'/></th>
                    <th style="width:6%" hidden="true" style="text-align:left;"><fmt:message key='order'/></th>
                    <th style="width:17%" style="text-align:left;"><fmt:message key='action'/></th>

                </tr>
                </thead>
                <tbody>
                <% for (UserStoreDTO userstoreDTO : userStoreDTOs) {
                    String className = userstoreDTO.getClassName();
                    int order = userstoreDTO.getOrder();
                    String description = userstoreDTO.getDescription();
                    String domainId = userstoreDTO.getDomainId();
                    Boolean isDisabled = userstoreDTO.getDisabled();

                    if (className == null) {
                        className = " ";
                    }

                    if (description == null) {
                        description = " ";
                    }

                    if (domainId == null) {
                        domainId = UserStoreConfigConstants.PRIMARY;
                    }
                %>
                <tr id=<%=domainId%>>
                    <%if (UserStoreMgtDataKeeper.getChangedUserStores().contains(domainId)) {

                    %>
                    <script>
                        $this.css("background-color", "green")
                    </script>
                    <%
                            }   %>
                    <td style="width:5%;">
                        <a class="icon-link" onclick="updownthis(this,'up')"
                           style="background-image:url(images/up.gif)"></a>
                        <a class="icon-link" onclick="updownthis(this,'down')"
                           style="background-image:url(images/down.gif)"></a>
                        <input type="hidden" value="<%=CharacterEncoder.getSafeText(className)%>"/>
                    </td>
                    <td width="5%" style="text-align:center; !important">
                        <input type="checkbox" name="userStores"
                               value="<%=userstoreDTO.getOrder()%>"
                               onclick="resetVars()"
                               class="chkBox"/> <%--jQuery("input:checked").each(function(){console.info($(this).val());});--%>
                    </td>
                    <td style="width:6%" style="text-align:left;">
                        <a><%=domainId%>
                        </a>
                    </td>
                    <td style="width:35%" style="text-align:left;">
                        <a><%=description%>
                        </a>
                    </td>
                    <td style="width:25%" style="text-align:left;">
                        <a><%=className%>
                        </a>
                    </td>
                    <td hidden="true" width="5%" style="text-align:center; !important" class="valueCell">
                        <a><%=order%>
                        </a>
                    </td>
                    <td width="29%" style="text-align:center; !important">
                        <a title="<fmt:message key='edit.userstore'/>"
                           onclick="edit('<%=order%>','<%=className%>');"
                           href="#" style="background-image: url(images/edit.gif);" class="icon-link">
                            <fmt:message key='edit.userstore'/></a>
                        <% if (!isDisabled) { %>
                        <a title="<fmt:message key='disable.userstore'/>"
                           onclick="disable('<%=order%>');return false;"
                           href="#" style="background-image: url(images/disable.gif);" class="icon-link">
                            <fmt:message key='disable.userstore'/></a>
                        <% } else { %>
                        <a title="<fmt:message key='enable.userstore'/>"
                           onclick="enable('<%=order%>');return false;"
                           href="#" style="background-image: url(images/enable.gif);" class="icon-link">
                            <fmt:message key='enable.userstore'/></a>
                        <%
                            }

                    %>
                    </td>
                </tr>
                <% }%>
                </tbody>
            </table>
            <div id="buttonRow">
                <tr>
                    <td style="border:0; !important">
                        <input type="button" onclick="doUpdate();" value="<fmt:message key="update"/>"
                               class="button"/>
                        <input type="button" onclick="doCancel();" value="<fmt:message key="cancel" />"
                               class="button"/>
                    </td>
                </tr>
            </div>
        </form>
    </div>
</div>
</fmt:bundle>

