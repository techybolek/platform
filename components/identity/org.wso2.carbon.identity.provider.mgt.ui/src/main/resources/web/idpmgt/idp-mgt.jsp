<%@ page import="org.wso2.carbon.identity.provider.mgt.ui.bean.CertData" %>
<%@ page import="org.wso2.carbon.identity.provider.mgt.ui.bean.TrustedIdPBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!--
~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<carbon:breadcrumb label="trusted.idp" resourceBundle="org.wso2.carbon.identity.provider.mgt.ui.i18n.Resources"
                    topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String issuer = null;
    String url = null;
    CertData certData = null;
    List<String> roles = null;
    Map<String,String> roleMappings = null;
    TrustedIdPBean bean = (TrustedIdPBean)session.getAttribute("trustedIdPBean");
    if(bean != null){
        issuer = bean.getIdPIssuerId();
        url = bean.getIdPUrl();
        certData = bean.getCertData();
        roles = bean.getRoles();
        roleMappings = bean.getRoleMappings();
    }
    if(url == null){
        url = "";
    }
%>

<script>
    <% if(roles != null){ %>
        var rowId = <%=roles.size()-1%>;
    <% } else { %>
        var rowId = -1;
    <% } %>
    function deleteRoleRow(obj){
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#roleAddTable tr')).length == 1){
            $(jQuery('#roleAddTable')).toggle();
        }
    }
    jQuery(document).ready(function(){
        jQuery('#roleAddLink').click(function(){
            rowId++;
            jQuery('#roleAddTable').append(jQuery('<tr><td><input type="text" id="rowid_'+rowId+'" name="rowname_'+rowId+'"/></td>' +
                    '<td><a onclick="deleteRoleRow(this)" class="icon-link" '+
                    'style="background-image: url(images/delete.gif)">'+
                    'Delete'+
                    '</a></td></tr>'));
            if($(jQuery('#roleAddTable tr')).length == 2){
                $(jQuery('#roleAddTable')).toggle();
            }
        })
    })
    function idpMgtUpdate(){
        if(doValidation()){
            jQuery('#finalRowId').val(rowId);
            jQuery('#idp-mgt-form').submit();
        }
    }
    function idpMgtDelete(){
        var input = document.createElement('input');
        input.type = "hidden";
        input.name = "delete";
        input.value = "delete";
        document.forms['idp-mgt-form'].appendChild(input);
        jQuery('#idp-mgt-form').submit();
    }

    function doValidation() {
        var reason = "";
        reason = validateEmpty("issuer");
        if (reason != "") {
            CARBON.showWarningDialog("<fmt:message key="idp.issuer.warn"/>");
            return false;
        }
        for(var i=0; i <= rowId; i++){
            if(document.getElementsByName('rowname_'+i)[0] != null){
                reason = validateEmpty('rowname_'+i);
                if(reason != ""){
                    CARBON.showWarningDialog("Role name strings cannot be zero length");
                    return false;
                }
            }
        }
        return true;
    }
</script>

<fmt:bundle basename="org.wso2.carbon.identity.provider.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='identity.provider'/>
        </h2>
        <div id="workArea">
            <form id="idp-mgt-form" name="idp-mgtform" method="post" action="idp-mgt-finish.jsp" enctype="multipart/form-data" >
            <div class="sectionSeperator togglebleTitle"><fmt:message key='identity.provider.info'/></div>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <tr>
                        <td colspan="2">
                            <div class="sectionHelp">
                                <fmt:message key='identity.provider.info.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='idp.issuer'/>:<span class="required">*</span></td>
                        <td>
                            <input id="issuer" name="issuer" type="text" value="<%=issuer%>" autofocus/>

                            <div class="sectionHelp">
                                <fmt:message key='idp.issuer.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='idp.url'/>:</td>
                        <td>
                            <input id="url" name="url" type="text" value="<%=url%>"/>
                            <div class="sectionHelp">
                                <fmt:message key='idp.url.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='idp.certificate'/>:</td>
                        <td>
                            <input id="certFile" name="certFile" type="file" />
                            <div class="sectionHelp">
                                <fmt:message key='idp.certificate.help'/>
                            </div>
                            <% if(certData != null) { %>
                            <table class="styledLeft">
                                <thead><tr><th class="leftCol-big"><fmt:message key='issuerdn'/></th><th><fmt:message key='subjectdn'/></th></tr></thead>
                                <tbody>
                                    <tr><td><%=certData.getIssuerDN()%></td><td><%=certData.getSubjectDN()%></td></tr>
                                </tbody>
                            </table>
                            <% } %>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='idp.roles'/>:</td>
                        <td>
                            <a id="roleAddLink" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key='add.role'/></a>
                            <br/><br>
                            <div class="sectionHelp">
                                <fmt:message key='idp.roles.help'/>
                            </div>
                            <table class="styledLeft" id="roleAddTable" style="display:none">
                                <thead><tr><th class="leftCol-big"><fmt:message key='idp.role'/></th><th></th></tr></thead>
                                <tbody>
                                <% if(roles != null && roles.size() > 0){ %>
                                    <script>
                                        $(jQuery('#roleAddTable')).toggle();
                                    </script>
                                    <% for(int i = 0; i < roles.size(); i++){ %>
                                        <tr>
                                            <td><input type="text" value="<%=roles.get(i)%>" id="rowid_<%=i%>" name="rowname_<%=i%>"/></td>
                                            <td><a onclick="deleteRoleRow(this)" class="icon-link"
                                                   style="background-image: url(images/delete.gif)">
                                                <fmt:message key='idp.role.delete'/>
                                            </a></td>
                                        </tr>
                                    <% } %>
                                <% } %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='idp.role.mappings'/>:</td>
                        <td>
                            <input id="roleMappingFile" name="roleMappingFile" type="file" />
                            <div class="sectionHelp">
                                <fmt:message key='idp.role.mappings.help'/>
                            </div>
                            <% if(roleMappings != null && roleMappings.size()>0){ %>
                                <table class="styledLeft">
                                    <thead><tr><th class="leftCol-big"><fmt:message key='idp.role'/></th><th><fmt:message key='tenant.role'/></th></tr></thead>
                                    <tbody>
                                        <% for(Map.Entry<String,String> entry:roleMappings.entrySet()){ %>
                                            <tr><td><%=entry.getKey()%></td><td><%=entry.getValue()%></td></tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            <% } %>
                        </td>
                    </tr>
                </table>
            </div>
            <% if(roles != null && roles.size() > 0) { %>
                <input type="hidden" name="finalRowId" id="finalRowId" value="<%=roles.size()-1%>"/>
            <% } else { %>
                <input type="hidden" name="finalRowId" id="finalRowId" value="-1"/>
            <% } %>
            <!-- sectionSub Div -->
            <div class="buttonRow">
                <input type="button" value="<fmt:message key='idp.update'/>" onclick="idpMgtUpdate();"/>
                <input type="button" value="<fmt:message key='idp.delete'/>" onclick="idpMgtDelete();"/>
            </div>
            </form>
        </div>
    </div>

</fmt:bundle>