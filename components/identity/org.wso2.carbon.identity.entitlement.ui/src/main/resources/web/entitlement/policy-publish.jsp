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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="java.lang.Exception" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.ClientUtil" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO" %>
<%

    String[] subscriberIds = null;
    boolean fromIndexPage = false;
    session.removeAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_MODULE);

    EntitlementPolicyAdminServiceClient client = null;

    int numberOfPages = 0;
    String isPaginatedString = request.getParameter("isPaginated");
    if (isPaginatedString != null && isPaginatedString.equals("true")) {
        client = (EntitlementPolicyAdminServiceClient) session.getAttribute(EntitlementPolicyConstants.ENTITLEMENT_ADMIN_CLIENT);
    }
    String paginationValue = "isPaginated=true";

    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    String publishAll = request.getParameter("publishAllPolicies");
    String policyId = request.getParameter("policyid");
    String fromIndex = request.getParameter("fromIndexPage");
    String[] selectedPolicies = request.getParameterValues("policies");
    String selectedModule = request.getParameter("selectedModule");
    boolean update = Boolean.parseBoolean(request.getParameter("update"));
    PublisherPropertyDTO[] propertyDTOs = (PublisherPropertyDTO[]) session.
            getAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_PROPERTY);

    session.removeAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_PROPERTY);
    if (fromIndex != null) {
        fromIndexPage = Boolean.parseBoolean(fromIndex);
    }

    if (propertyDTOs != null) {
        for (PublisherPropertyDTO dto : propertyDTOs) {
            String value = request.getParameter(dto.getId());
            if (value != null && value.trim().length() > 0) {
                dto.setValue(value);
            }
        }
    }

    if (policyId != null && policyId.trim().length() > 0) {
        selectedPolicies = new String[]{policyId};
    }

    if (selectedPolicies != null && selectedPolicies.length > 0) {
        session.setAttribute("selectedPolicies", selectedPolicies);
        fromIndexPage = true;
    }

    if (publishAll != null && "true".equals(publishAll.trim())) {
        session.setAttribute("publishAllPolicies", true);
        fromIndexPage = true;
    }
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                                                                                   CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    try {

        if (client == null) {

            client = new EntitlementPolicyAdminServiceClient(cookie,
                                                             serverURL, configContext);
            session.setAttribute(EntitlementPolicyConstants.ENTITLEMENT_ADMIN_CLIENT, client);
        }


        if (selectedModule != null && selectedModule.trim().length() > 0 && propertyDTOs != null
            && propertyDTOs.length > 0) {
            PublisherDataHolder holder = new PublisherDataHolder();
            holder.setModuleName(selectedModule);
            holder.setPropertyDTOs(propertyDTOs);
            client.updateSubscriber(holder, update);

        }
        int itemsPerPageInt = EntitlementPolicyConstants.DEFAULT_ITEMS_PER_PAGE;
        if (client != null && client.getSubscriberIds() != null) {
            numberOfPages = (int) Math.ceil((double) client.getSubscriberIds().length / itemsPerPageInt);
            subscriberIds = ClientUtil.doPaging(pageNumberInt, client.getSubscriberIds());
        }
    } catch (Exception e) {
%>

<script type="text/javascript">
    CARBON.showErrorDialog('<%=e.getMessage()%>', function () {
        location.href = "policy-publish.jsp";
    });
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
        label="policy.publisher"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    var allSubscribersSelected = false;

    function doCancel() {
        location.href = 'index.jsp?';
    }

    function editSubscriber(subscriber) {
        location.href = "add-subscriber.jsp?view=false&subscriberId=" + subscriber;
    }

    function viewSubscriber(subscriber) {
        location.href = "add-subscriber.jsp?view=true&subscriberId=" + subscriber +
                        "&fromIndexPage=" + '<%=fromIndexPage%>';
    }

    function deleteSubscriber(subscriber) {
        location.href = "policy-publish.jsp?delete=" + subscriber;
    }

    function publishToSubscriber() {
        var selected = false;

        if (document.policyForm.subscribers == null) {
            CARBON.showWarningDialog('<fmt:message key="no.subscriber.to.be.published"/>');
            return;
        }

        if (document.policyForm.subscribers[0] != null) { // there is more than 1 policy
            for (var j = 0; j < document.policyForm.subscribers.length; j++) {
                selected = document.policyForm.subscribers[j].checked;
                if (selected) break;
            }
        } else if (document.policyForm.subscribers != null) { // only 1 policy
            selected = document.policyForm.subscribers.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.subscriber.to.be.published"/>');
            return;
        }
        if (allSubscribersSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="publish.to.all.subscribers.prompt"/>", function () {
                document.policyForm.action = "publish-finish.jsp";
                document.policyForm.submit();
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="publish.selected.subscriber.prompt"/>", function () {
                document.policyForm.action = "publish-finish.jsp";
                document.policyForm.submit();
            });
        }

    }

    function publishToAll() {
        if (document.policyForm.subscribers == null) {
            CARBON.showWarningDialog('<fmt:message key="no.subscriber.to.be.published"/>');
            return;
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="publish.to.all.subscribers.prompt"/>", function () {
                location.href = "publish-finish.jsp?publishToAllSubscribers=true";
            });
        }
    }

    function resetVars() {
        allSubscribersSelected = false;

        var isSelected = false;
        if (document.policyForm.subscribers[0] != null) { // there is more than 1 service
            for (var j = 0; j < document.policyForm.subscribers.length; j++) {
                if (document.policyForm.subscribers[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.policyForm.subscribers != null) { // only 1 service
            if (document.policyForm.subscribers.checked) {
                isSelected = true;
            }
        }
        return false;
    }

    function deleteServices() {
        var selected = false;
        if (document.policyForm.subscribers[0] != null) { // there is more than 1 policy
            for (var j = 0; j < document.policyForm.subscribers.length; j++) {
                selected = document.policyForm.subscribers[j].checked;
                if (selected) break;
            }
        } else if (document.policyForm.subscribers != null) { // only 1 policy
            selected = document.policyForm.subscribers.checked;
        }
        if (!selected) {
            CARBON.showInfoDialog('<fmt:message key="select.policies.to.be.deleted"/>');
            return;
        }
        if (allSubscribersSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="delete.all.policies.prompt"/>", function () {
                document.policyForm.action = "remove-subscriber.jsp";
                document.policyForm.submit();
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="delete.services.on.page.prompt"/>", function () {
                document.policyForm.action = "remove-subscriber.jsp";
                document.policyForm.submit();
            });
        }
    }

    function selectAllInThisPage(isSelected) {

        allSubscribersSelected = false;
        if (document.policyForm.subscribers != null &&
            document.policyForm.subscribers[0] != null) { // there is more than 1 service
            if (isSelected) {
                for (var j = 0; j < document.policyForm.subscribers.length; j++) {
                    document.policyForm.subscribers[j].checked = true;
                }
            } else {
                for (j = 0; j < document.policyForm.subscribers.length; j++) {
                    document.policyForm.subscribers[j].checked = false;
                }
            }
        } else if (document.policyForm.subscribers != null) { // only 1 service
            document.policyForm.subscribers.checked = isSelected;
        }
        return false;
    }

</script>

<div id="middle">
    <h2><fmt:message key="policy.publisher"/></h2>

    <div id="workArea">
        <%
            if (fromIndexPage) {

        %>
        <table style="border:none; margin-bottom:10px">
            <tr>
                <td>
                    <a style="cursor: pointer;" onclick="selectAllInThisPage(true);return false;"
                       href="#"><fmt:message key="selectAllInPage"/></a>
                    &nbsp;<b>|</b>&nbsp;</td>
                <td><a style="cursor: pointer;" onclick="selectAllInThisPage(false);return false;"
                       href="#"><fmt:message key="selectNone"/></a>
                </td>
                <td width="20%">&nbsp;</td>
                <td>
                    <div style="height:30px;">
                        <a onclick="publishToSubscriber(); return false;" href="#" class="icon-link"
                           style="background-image:url(images/publish.gif);"><fmt:message
                                key='publish.selected'/></a>
                    </div>
                </td>
                <td>
                    <div style="height:30px;">
                        <a onclick="publishToAll(); return false;" href="#" class="icon-link"
                           style="background-image:url(images/publish-all.gif);"><fmt:message
                                key='publish.to.all'/></a>
                    </div>
                </td>
            </tr>
        </table>
        <%
        } else {
        %>
        <table style="border:none; margin-bottom:10px">
            <tr>
                <td>
                    <div style="height:30px;">
                        <a href="javascript:document.location.href='add-subscriber.jsp'"
                           class="icon-link"
                           style="background-image:url(images/add.gif);"><fmt:message
                                key='add.subscriber'/></a>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <a style="cursor: pointer;" onclick="selectAllInThisPage(true);return false;"
                       href="#"><fmt:message key="selectAllInPage"/></a>
                    &nbsp;<b>|</b>&nbsp;</td>
                <td><a style="cursor: pointer;" onclick="selectAllInThisPage(false);return false;"
                       href="#"><fmt:message key="selectNone"/></a>
                </td>
                <td width="20%">&nbsp;</td>
                <td>
                    <a onclick="deleteServices();return false;" class="icon-link"
                       style="background-image:url(images/delete.gif);" href="#"><fmt:message
                            key="delete"/></a>
                </td>
            </tr>

        </table>
        <%
            }
        %>
        <form action="" name="policyForm" method="post">
            <table class="styledLeft" style="width: 100%;margin-top:10px;">
                <thead>
                <tr>
                    <th colspan='2'><fmt:message key='subscriber.name'/></th>
                    <th><fmt:message key='action'/></th>
                </tr>
                </thead>
                <tbody>
                <%
                    if (subscriberIds != null && subscriberIds.length > 0) {
                        for (String subscriber : subscriberIds) {
                            if (subscriber != null && !subscriber.equals("")) {
                %>
                <tr>
                    <td width="10px" style="text-align:center; !important">
                        <input type="checkbox" name="subscribers"
                               value="<%=subscriber%>"
                               onclick="resetVars()" class="chkBox"/>
                    </td>
                    <td><%=subscriber%>
                    </td>
                    <td>
                        <a onclick="viewSubscriber('<%=subscriber%>');return false;"
                           href="#" style="background-image: url(images/edit.gif);"
                           class="icon-link">
                            <fmt:message key='view'/></a>
                        <%
                            if (!fromIndexPage) {
                        %>
                        <a onclick="editSubscriber('<%=subscriber%>');return false;"
                           href="#" style="background-image: url(images/edit.gif);"
                           class="icon-link">
                            <fmt:message key='edit'/></a>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <%
                            }
                        }
                    }
                %>
                </tbody>
                <tr>
                    <carbon:paginator pageNumber="<%=pageNumberInt%>"
                                      numberOfPages="<%=numberOfPages%>"
                                      page="policy-publish.jsp"
                                      pageNumberParameterName="pageNumber"
                                      parameters="<%=paginationValue%>"
                                      resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                                      prevKey="prev" nextKey="next"/>
                </tr>
            </table>
        </form>
    </div>
</div>
</fmt:bundle>
