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
<%@ page
        import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.ClientUtil" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>

<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>

<%
    String[] subscriberIds = null;
    String publishAll = request.getParameter("publishAllPolicies");
    String policyId = request.getParameter("policyId");
    String toPDP = request.getParameter("toPDP");
    String[] selectedPolicies = request.getParameterValues("policies");

    String publishAction = request.getParameter("publishAction");
    String policyVersion = request.getParameter("policyVersion");
    String policyOrder = request.getParameter("policyOrder");
    String versionSelector = request.getParameter("versionSelector");

    if(publishAction == null || publishAction.trim().length() == 0) {
        publishAction = (String)session.getAttribute("publishAction");        
    } else {
        session.setAttribute("publishAction", publishAction);    
    }
    
    // setting default action
    if(publishAction == null){
        publishAction = EntitlementConstants.PolicyPublish.ACTION_CREATE;
    }

    if(versionSelector != null){
        if(policyVersion == null || policyVersion.trim().length() == 0) {
            policyVersion = (String)session.getAttribute("policyVersion");
        } else {
            session.setAttribute("policyVersion", policyVersion);
        }
    }

    if(policyOrder == null || policyOrder.trim().length() == 0) {
        policyOrder = (String)session.getAttribute("policyOrder");
    } else {
        session.setAttribute("policyOrder", policyOrder);
    }

    int numberOfPages = 0;
    String subscriberSearchString = request.getParameter("subscriberSearchString");
    if (subscriberSearchString == null) {
        subscriberSearchString = "";
    } else {
        subscriberSearchString = subscriberSearchString.trim();
    }
    String paginationValue = "subscriberSearchString=" + subscriberSearchString;

    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
        // ignore
    }

    if (publishAll != null && "true".equals(publishAll.trim())) {
        session.setAttribute("publishAllPolicies", true);
    } else {
        session.setAttribute("publishAllPolicies", false);
    }

    if (policyId != null && policyId.trim().length() > 0) {
        selectedPolicies = new String[]{policyId};
    }

    if(selectedPolicies != null ){
        session.setAttribute("selectedPolicies", selectedPolicies);
    } else {
        selectedPolicies = (String[]) session.getAttribute("selectedPolicies");
    }

    String tmp = "";
    if(selectedPolicies != null && selectedPolicies.length == 1){
        policyId = selectedPolicies[0];
    }
    try{
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.                                                                           CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);       
        if (policyId != null && policyId.trim().length() > 0) {
            String[] versions = client.getPolicyVersions(policyId);
            if(versions != null && versions.length > 0){
                for(String version : versions){
                    if(policyVersion == null || policyVersion.trim().length() == 0) {
                        tmp += "<option value=\"" + version + "\" >" + version + "</option>";
                    } else {
                        tmp += "<option value=\"" + version + "\"    selected=\"selected\" >" + version + "</option>";
                    }
                }
            }
        }
        // as these are just strings, get all values in to UI and the do the pagination
        String[] allSubscriberIds = (String[])session.getAttribute("subscriberIds");
        if(allSubscriberIds == null){
            allSubscriberIds = client.getSubscriberIds(subscriberSearchString);
            session.setAttribute("subscriberIds", allSubscriberIds);
        }
        if (allSubscriberIds != null) {
            numberOfPages = (int) Math.ceil((double) allSubscriberIds.
                    length / 5);
            subscriberIds = ClientUtil.doPagingForStrings(pageNumberInt, 5,
                    client.getSubscriberIds(subscriberSearchString));
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
        label="publish.policy"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    var allSubscribersSelected = false;

    function doCancel() {
        location.href = 'index.jsp';
    }

    function doNext() {
        document.publishForm.action = "policy-publish.jsp";
        document.publishForm.submit();
    }

    function doPaginate(page, pageNumberParameterName, pageNumber){

        document.publishForm.action =  page + "?" + pageNumberParameterName + "=" + pageNumber + "&";
        document.publishForm.submit();
    }

    function showVersion(){

        var selectorElement = document.getElementById('policyVersionSelect');
        selectorElement.innerHTML = '<label for="policyVersion"></label>' +
            '<select id="policyVersion" name="policyVersion" class="leftCol-small">' +'<%=tmp%>' + '</select>';
    }

    function disableVersion(){
        var selectorElement = document.getElementById('policyVersionSelect');
        selectorElement.innerHTML = '';
    }

    function searchService() {
        document.publishForm.submit();
    }

    function resetVars() {
        allSubscribersSelected = false;

        var isSelected = false;
        if (document.publishForm.subscribers[0] != null) { // there is more than 1 service
            for (var j = 0; j < document.publishForm.subscribers.length; j++) {
                if (document.publishForm.subscribers[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.publishForm.subscribers != null) { // only 1 service
            if (document.publishForm.subscribers.checked) {
                isSelected = true;
            }
        }
        return false;
    }

    function viewSubscriber(subscriber) {
        location.href = "add-subscriber.jsp?view=true&subscriberId=" + subscriber;
    }

    function publishToSubscriber(toPDP) {
        var selected = false;

        if(!toPDP){
            if (document.publishForm.subscribers == null) {
                CARBON.showWarningDialog('<fmt:message key="no.subscriber.to.be.published"/>');
                return;
            }

            if (document.publishForm.subscribers[0] != null) { // there is more than 1 policy
                for (var j = 0; j < document.publishForm.subscribers.length; j++) {
                    selected = document.publishForm.subscribers[j].checked;
                    if (selected) break;
                }
            } else if (document.publishForm.subscribers != null) { // only 1 policy
                selected = document.publishForm.subscribers.checked;
            }



            if (!selected) {
                CARBON.showInfoDialog('<fmt:message key="select.subscriber.to.be.published"/>');
                return;
            }
        }
        if (allSubscribersSelected) {
            CARBON.showConfirmationDialog("<fmt:message key="publish.to.all.subscribers.prompt"/>", function () {
                document.publishForm.action = "publish-finish.jsp";
                document.publishForm.submit();
            });
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="publish.selected.subscriber.prompt"/>", function () {
                document.publishForm.action = "publish-finish.jsp";
                document.publishForm.submit();
            });
        }
    }

    function publishToAll() {
        if (document.publishForm.subscribers == null) {
            CARBON.showWarningDialog('<fmt:message key="no.subscriber.to.be.published"/>');
            return;
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="publish.to.all.subscribers.prompt"/>", function () {
                location.href = "publish-finish.jsp?publishToAllSubscribers=true";
            });
        }
    }

</script>

<div id="middle">
    <h2><fmt:message key="publish.policy"/></h2>
    <div id="workArea">
        <form action="policy-publish.jsp" name="publishForm" method="post">
            <table class="styledLeft" style="width: 100%;margin-top:10px;">
            <thead>
            <tr>
                <th  colspan="3"> <fmt:message key="select.publish.actions"/></th>
            </tr>
            </thead>
            <tr>
                <td style="width: 33%;margin-top:10px;">
                    <label>
                        <input name="publishAction" type="radio"
                        <% if(EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_CREATE%>">
                        <fmt:message key="select.publish.actions.add"/>
                    </label>
                </td>
                <td style="width: 33%;margin-top:10px;">
                    <label>
                        <input name="publishAction" type="radio"
                        <% if(EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_UPDATE%>">
                        <fmt:message key="select.publish.actions.update"/>
                    </label>
                </td>
                <td style="width: 33%;margin-top:10px;">
                    <label>
                        <input name="publishAction" type="radio"
                        <% if(EntitlementConstants.PolicyPublish.ACTION_DELETE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_DELETE%>">
                        <fmt:message key="select.publish.actions.delete"/>
                    </label>
                </td>
            </tr>
        </table>

        <%
            if(policyId != null && tmp.trim().length() > 0){
        %>
            <table class="styledLeft" style="width: 100%;margin-top:10px;">
            <thead>
                <tr>
                    <th colspan="3"><fmt:message key="select.publish.version"/></th>
                </tr>
            </thead>
            <tr>
                <td style="width: 33%;margin-top:10px;">
                    <label>
                        <input name="versionSelector" type="radio" value="versionSelector"
                        <%if(policyVersion == null || policyVersion.trim().length() == 0) { %>
                               checked="checked"
                        <% } %>
                               onclick="disableVersion();">
                        <fmt:message key="select.publish.version.current"/>
                    </label>
                </td>
                <td style="width: 33%;margin-top:10px;">
                    <label>
                        <input name="versionSelector" type="radio"
                        <%if(policyVersion != null && policyVersion.trim().length() > 0) { %>
                               checked="checked"
                        <% } %>
                               onclick="showVersion();">
                        <fmt:message key="select.publish.version.older"/>
                    </label>
                </td>
                <td style="width: 33%;margin-top:10px;" id="policyVersionSelect" >
                </td>
            </tr>
        </table>
        <%
            }
        %>

    <%
        if(!"true".equals(toPDP)){
    %>

            <table class="styledLeft noBorders" style="width: 100%;margin-top:10px;">
                <thead>
                <tr>
                    <th><fmt:message key='select.subscriber'/></th>
                </tr>
                </thead>
            </table>

            <table class="styledLeft noBorders">
                <tbody>
                <tr style="border:0; !important">
                    <td style="border:0; !important">
                        <nobr>
                            <fmt:message key="search"/>
                            <input type="text" name="subscriberSearchString"
                                   value="<%= subscriberSearchString != null? subscriberSearchString :""%>"/>&nbsp;
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

            <table class="styledLeft" style="width: 100%;margin-top:10px;">
            <%
                if (subscriberIds != null && subscriberIds.length > 0) {
                    for (String subscriber : subscriberIds) {
                        if (subscriber != null && subscriber.trim().length() > 0 ) {
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
                </td>
            </tr>
            <%
                    }
                }
            %>


<%if(policyVersion != null && policyVersion.trim().length() > 0) { %>
<script type="text/javascript">
showVersion()
</script>
<%}%>
            <carbon:paginator pageNumber="<%=pageNumberInt%>"
                              numberOfPages="<%=numberOfPages%>"
                              action="post"
                              page="start-publish.jsp"
                              pageNumberParameterName="pageNumber"
                              parameters="<%=paginationValue%>"
                              resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                              prevKey="prev" nextKey="next"/>
            <%
                } else {
            %>
            <tr class="noRuleBox">
                <td colspan="3"><fmt:message key="no.subscribers.defined"/><br/></td>
            </tr>
            <%
                }
            %>

        </table>

    <%
        } else {
    %>
            <tr>
                <td>
                    <input name="subscribers" type="hidden" value="<%=EntitlementConstants.PDP_SUBSCRIBER_ID%>" />
                </td>
            </tr>
    <%
        }
    %>

        <div class="buttonRow">
        <table class="styledLeft noBorders">
        <tr>
            <td>
                <%
                    if("true".equals(toPDP)){
                %>
                <input type="button" class="button" value="Publish" onclick="publishToSubscriber(true);">
                <%
                    }  else {
                %>
                <input type="button" class="button" value="Publish" onclick="publishToSubscriber(false);">
                <input type="button" class="button" value="PublishToAll" onclick="publishToAll();">
                <%
                    }
                %>
                <input type="button" class="button" value="Cancel" onclick="doCancel();">
            </td>
        </tr>
        </table>
        </div>
        </form>
    </div>
</div>
</fmt:bundle>