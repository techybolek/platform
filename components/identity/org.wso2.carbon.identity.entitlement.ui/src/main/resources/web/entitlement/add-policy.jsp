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
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorException" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>


<%
    String forwardTo = null;
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    System.out.println(request.getParameter("editorConfig"));
    if(request.getParameter("editorConfig") != null){
        try {
            PolicyEditorEngine.getInstance().persistConfig(request.getParameter("editorConfig"));
        } catch (PolicyEditorException e) {
            String message = "Config can not be updated. " + e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
            forwardTo = "policy-editor-config-view.jsp";
            %>
            <script type="text/javascript">
                function forward() {
                    location.href = "<%=forwardTo%>";
                }
            </script>
            <script type="text/javascript">
                forward();
            </script>
            <%
        }
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">

<div id="middle">
    <h2><fmt:message key="add.new.policy"/></h2>
<div id="workArea">
    <%--<p> <fmt:message key="add.new.policy.description"/> </p>--%>
    <table class="styledLeft" style="width:100%">
        <thead>
        <tr>
            <th><fmt:message key="add.new.policy.method"/></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td class="formRow">
                <table class="normal" style="width:100%">
                    <tbody>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="rbac-policy-editor.jsp"><fmt:message key="add.new.policy.rbac"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.rbac.description"/></td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td width="20%">
                                <a href="basic-policy-editor.jsp"><fmt:message key="add.new.policy.basic"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.basic.description"/>
                                                    <a href="policy-editor-config-view.jsp"> here</a></td>
                        </tr>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="policy-editor.jsp"><fmt:message key="add.new.policy.editor"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.editor.description"/>
                                            <a href="policy-editor-config-view.jsp"> here</a></td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td width="20%">
                                <a href="import-policy.jsp"><fmt:message key="add.new.policy.import"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.import.description"/></td>
                        </tr>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="policy-view.jsp"><fmt:message key="add.new.policy.write"/></a>
                            </td>
                            <td><fmt:message key="add.new.policy.write.description"/></td>
                        </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>
/<div>
</fmt:bundle>