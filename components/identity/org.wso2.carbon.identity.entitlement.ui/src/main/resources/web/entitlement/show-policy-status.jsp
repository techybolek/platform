<!--
 ~ Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.StatusHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PaginatedStatusHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>


<%

    int numberOfPages = 0;
    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
    }

    String statusSearchString = request.getParameter("statusSearchString");
    if (statusSearchString == null) {
        statusSearchString = "";
    } else {
        statusSearchString = statusSearchString.trim();
    }

    String paginationValue = "statusSearchString=" + statusSearchString;

    String policyId = request.getParameter("policyid");

    StatusHolder[] statusHolders = new StatusHolder[0];


    try {
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.                                                                           CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);
        PaginatedStatusHolder holder = client.getStatusData(EntitlementConstants.Status.ABOUT_POLICY,
                            policyId,  "*", statusSearchString, pageNumberInt);
        statusHolders = holder.getStatusHolders();
        numberOfPages = holder.getNumberOfPages();
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

<script type="text/javascript">
    function searchService() {
        document.searchForm.submit();
    }

    function doCancel(){
        location.href = 'index.jsp';
    }
</script>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<div id="middle">
    <h2><fmt:message key="policy.status"/></h2>
<div id="workArea">
    <form action="show-policy-status.jsp" name="searchForm" method="post">
        <table style="border:0;
                                                !important margin-top:10px;margin-bottom:10px;">
            <tr>
                <td>
                    <table style="border:0; !important">
                        <tbody>
                        <tr style="border:0; !important">
                            <td style="border:0; !important">
                                <nobr>
                                <fmt:message key="search.status"/>
                                <input type="text" name="statusSearchString"
                                       value="<%= statusSearchString != null? statusSearchString :""%>"/>&nbsp;
                                </nobr>
                            </td>
                            <td style="border:0; !important">
                                <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                                   onclick="searchService(); return false;"
                                   alt="<fmt:message key="search"/>"></a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
        </table>
    </form>
    <table  class="styledLeft"  style="width: 100%;margin-top:10px;">
        <thead>
        <tr>
            <th><fmt:message key="time.stamp"/></th>
            <th><fmt:message key="action"/></th>
            <th><fmt:message key="policy.user"/></th>
            <th><fmt:message key="target"/></th>
            <th><fmt:message key="target.action"/></th>
            <th><fmt:message key="status"/></th>
            <th><fmt:message key="details"/></th>
        </tr>
        </thead>
        <%
            if(statusHolders != null){
            for(StatusHolder dto : statusHolders){
                if(dto != null && dto.getTimeInstance() != null){
        %>
        <tr>
            <td><%=dto.getTimeInstance()%></td>
            <td><% if(dto.getType() != null){%> <%=dto.getType()%><%}%></td>
            <td><% if(dto.getUser() != null){%> <%=dto.getUser()%><%}%></td>
            <td><% if(dto.getTarget() != null){%> <%=dto.getTarget()%><%}%></td>
            <td><% if(dto.getTargetAction() != null){%> <%=dto.getTargetAction()%><%}%></td>
            <td><% if(dto.getSuccess()){%> <fmt:message key="status.success"/> <%}
                                            else {%> <fmt:message key="status.fail"/> <%} %></td>
            <td><% if(dto.getMessage() != null){%> <%=dto.getMessage()%><%}%></td>
        </tr>
        <%
                }
            }
        }

        %>
        <tr>
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              page="show-policy-status.jsp"
                              pageNumberParameterName="pageNumber"
                              parameters="<%=paginationValue%>"
                              resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                              prevKey="prev" nextKey="next"/>
        </tr>

    </table>
</div>
<div class="buttonRow">
    <a onclick="doCancel()" class="icon-link" style="background-image:none;">
        <fmt:message key="back.to.subscribers"/></a><div style="clear:both"></div>
</div>
</div>
</fmt:bundle>