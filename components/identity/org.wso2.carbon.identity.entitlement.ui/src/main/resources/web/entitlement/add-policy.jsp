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
            //session.setAttribute("entitlementpolicy", dto.getPolicy());
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

<div id="workArea">
    <p>
        Create new policy using following ways
    </p>
    <table class="styledLeft" style="width:100%">
        <thead>
        <tr>
            <th>
                Select policy creation method
            </th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td class="formRow">
                <table class="normal" style="width:100%">
                    <tbody>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="rbac-policy-editor.jsp">RBAC Policy</a>
                            </td>
                            <td>This is role base access control policy</td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td width="20%">
                                <a href="basic-policy-editor.jsp">Basic XACML Policy</a>
                            </td>
                            <td>You can configure Basic policy editor from
                                                    <a href="policy-editor-config-view.jsp"> here</a></td>
                        </tr>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="policy-editor.jsp">Advance XACML Policy</a>
                            </td>
                            <td>You can configure Basic policy editor from
                                                    <a href="policy-editor-config-view.jsp"> here</a></td>
                        </tr>
                        <tr class="tableEvenRow">
                            <td width="20%">
                                <a href="import-policy.jsp">Import Existing XACML Policy</a>
                            </td>
                            <td>This is role base access control policy</td>
                        </tr>
                        <tr class="tableOddRow">
                            <td width="20%">
                                <a href="policy-view.jsp">Write XACML Policy</a>
                            </td>
                            <td>This is role base access control policy</td>
                        </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        </tbody>
    </table>
</div>