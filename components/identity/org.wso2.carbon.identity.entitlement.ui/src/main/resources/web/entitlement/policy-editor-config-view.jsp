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
<jsp:include page="../dialog/display_messages.jsp"/>
<jsp:include page="../highlighter/header.jsp"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%@ page import="org.wso2.carbon.identity.entitlement.common.PolicyEditorEngine" %>

<%
    String editorConfig = PolicyEditorEngine.getInstance().getConfig();
%>

<div id="workArea">
    <form method="post" name="configForm" id="configForm" action="add-policy.jsp">
        <table class="styledLeft" style="width:100%">
            <thead>
            <tr>
                <th>
                    Policy Editor Configuration
                </th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td class="formRow">
                    <table class="normal" style="width:100%">
                        <tbody><tr>
                            <td>
                                <textarea name="editorConfigText"  id="editorConfigText"
                                          style="border: 1px solid rgb(204, 204, 204); width: 99%;
                                          height: 400px; margin-top: 5px; display: none;"><%=editorConfig%>
                                </textarea>
                            </td>
                            <td><input type="hidden" name="editorConfig" id="editorConfig"></td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            <tr>
                <td class="buttonRow">
                    <button class="button" onclick="submitForm(); return false;">Update</button>
                    <button class="button" onclick="resetConfiguration(); return false;">Reset</button>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>

<script src="../editarea/edit_area_full.js" type="text/javascript"></script>

    <script type="text/javascript">

    function submitForm() {
        document.getElementById("editorConfig").value = document.getElementById("editorConfigText").value;
        document.configForm.submit();
    }

    editAreaLoader.init({
        id : "editorConfigText"		// text area id
        ,syntax: "xml"			// syntax to be uses for highlighting
        ,start_highlight: true  // to display with highlight mode on start-up
    });

</script>