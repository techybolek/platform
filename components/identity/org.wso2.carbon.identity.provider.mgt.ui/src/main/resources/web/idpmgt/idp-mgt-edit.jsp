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
<%@ page import="org.wso2.carbon.identity.provider.mgt.ui.bean.CertData" %>
<%@ page import="org.wso2.carbon.identity.provider.mgt.ui.bean.TrustedIdPBean" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<carbon:breadcrumb label="trusted.idp" resourceBundle="org.wso2.carbon.identity.provider.mgt.ui.i18n.Resources"
                    topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String issuer = request.getParameter("issuer");
    String url = null;
    CertData certData = null;
    List<String> roles = null;
    Map<String,String> roleMappings = null;
    boolean primary = false;
    List<String> audience = null;
    TrustedIdPBean bean = (TrustedIdPBean)session.getAttribute("trustedIdPBean");
    String[] tenantIdPList = (String[])session.getAttribute("tenantIdPList");
    if(tenantIdPList == null){
    %>
        <script type="text/javascript">
            location.href = "idp-mgt-list-load.jsp";
        </script>
    <%
        return;
    }
    if(issuer != null && !issuer.equals("") && bean != null){
        issuer = bean.getIdPIssuerId();
        url = bean.getIdPUrl();
        certData = bean.getCertData();
        roles = bean.getRoles();
        roleMappings = bean.getRoleMappings();
        primary = bean.isPrimary();
        audience = bean.getAudience();
    }
    if(issuer == null){
        issuer = "";
    }
    if(url == null){
        url = "";
    }
    String disabled = "", checked = "";
    if(bean != null){
        if(primary){
            checked = "checked";
            disabled = "disabled=\'disabled\'";
        }
    } else {
        if(tenantIdPList.length > 0){
            if(primary){
                disabled = "disabled=\'disabled\'";
                checked = "checked=\'checked\'";
            }
        } else {
            disabled = "disabled=\'disabled\'";
            checked = "checked=\'checked\'";
        }
    }

%>

<script>
    <% if(roles != null){ %>
        var rowId = <%=roles.size()-1%>;
    <% } else { %>
        var rowId = -1;
    <% } %>

    jQuery(document).ready(function(){
        jQuery('#publicCertDeleteLink').click(function(){
            $(jQuery('#publicCertDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deletePublicCert";
            input.id = "deletePublicCert";
            input.value = "true";
            document.forms['idp-mgt-edit-form'].appendChild(input);
        })
    })
    var deletedRoleRows = [];
    function deleteRoleRow(obj){
       if(jQuery(obj).parent().prev().children()[0].value != ''){
            deletedRoleRows.push(jQuery(obj).parent().prev().children()[0].value);
        }
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#roleAddTable tr')).length == 1){
            $(jQuery('#roleAddTable')).toggle();
        }
    }
    var deletedAudienceRows = [];
    function deleteAudienceRow(obj){
        if(jQuery(obj).parent().prev().children()[0].value != ''){
            deletedAudienceRows.push(jQuery(obj).parent().prev().children()[0].value);
        }
        jQuery(obj).parent().parent().remove();
        if($(jQuery('#audienceAddTable tr')).length == 1){
            $(jQuery('#audienceAddTable')).toggle();
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
    jQuery(document).ready(function(){
        jQuery('#audienceAddLink').click(function(){
            jQuery('#audienceAddTable').append(jQuery('<tr><td><input type="text" id="audience" name="audience"/></td>' +
                    '<td><a onclick="deleteAudienceRow(this)" class="icon-link" '+
                    'style="background-image: url(images/delete.gif)">'+
                    'Delete'+
                    '</a></td></tr>'));
            if($(jQuery('#audienceAddTable tr')).length == 2){
                $(jQuery('#audienceAddTable')).toggle();
            }
        })
    })
    jQuery(document).ready(function(){
        jQuery('#roleMappingDeleteLink').click(function(){
            $(jQuery('#roleMappingDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deleteRoleMappings";
            input.id = "deleteRoleMappings";
            input.value = "true";
            document.forms['idp-mgt-edit-form'].appendChild(input);
        })
    })
    function idpMgtUpdate(){
        if(doValidation()){
            var allDeletedRoleStr = "";
            for(var i = 0;i<deletedRoleRows.length;i++){
                if(i < deletedRoleRows.length-1){
                    allDeletedRoleStr += deletedRoleRows[i] + ", ";
                } else {
                    allDeletedRoleStr += deletedRoleRows[i] + "?";
                }
            }
            var allDeletedAudienceStr = "";
            for(var i = 0;i<deletedAudienceRows.length;i++){
                if(i < deletedAudienceRows.length-1){
                    allDeletedAudienceStr += deletedAudienceRows[i] + ", ";
                } else {
                    allDeletedAudienceStr += deletedAudienceRows[i] + "?";
                }
            }
            if(allDeletedRoleStr != "") {
                CARBON.showConfirmationDialog('Are you sure you want to delete the role(s) ' + allDeletedRoleStr,
                        function(){
                            if(jQuery('#deleteRoleMappings').val() == 'true'){
                                CARBON.showConfirmationDialog('Are you sure you want to delete all the role mappings?',
                                        function (){
                                            if(jQuery('#deletePublicCert').val() == 'true'){
                                                CARBON.showConfirmationDialog('Are you sure you want to delete the public certificate of ' +
                                                        jQuery('#issuer').val() + '?',
                                                        function (){
                                                            if(allDeletedAudienceStr != ""){
                                                                CARBON.showConfirmationDialog('Are you sure you want to delete the audience ' + allDeletedAudienceStr,
                                                                        function(){
                                                                            doEditFinish();
                                                                        },
                                                                        function(){
                                                                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                                                                        });
                                                            } else {
                                                                doEditFinish();
                                                            }
                                                        },
                                                        function(){
                                                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                                                        });
                                            } else {
                                                doEditFinish();
                                            }
                                        },
                                        function(){
                                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                                        });
                            } else {
                                doEditFinish();
                            }
                        },
                        function(){
                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                        });
            } else if(jQuery('#deleteRoleMappings').val() == 'true'){
                CARBON.showConfirmationDialog('Are you sure you want to delete all the role mappings?',
                        function (){
                            if(jQuery('#deletePublicCert').val() == 'true'){
                                CARBON.showConfirmationDialog('Are you sure you want to delete the public certificate of ' +
                                        jQuery('#issuer').val() + '?',
                                        function (){
                                            if(allDeletedAudienceStr != ""){
                                                CARBON.showConfirmationDialog('Are you sure you want to delete the audience ' + allDeletedAudienceStr,
                                                        function(){
                                                            doEditFinish();
                                                        },
                                                        function(){
                                                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                                                        });
                                            } else {
                                                doEditFinish();
                                            }
                                        },
                                        function(){
                                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                                        });
                            } else {
                                doEditFinish();
                            }
                        },
                        function(){
                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                        });
            } else if(jQuery('#deletePublicCert').val() == 'true'){
                CARBON.showConfirmationDialog('Are you sure you want to delete the public certificate of ' +
                        jQuery('#issuer').val() + '?',
                        function (){
                            if(allDeletedAudienceStr != ""){
                                CARBON.showConfirmationDialog('Are you sure you want to delete the audience ' + allDeletedAudienceStr,
                                        function(){
                                            doEditFinish();
                                        },
                                        function(){
                                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                                        });
                            } else {
                                doEditFinish();
                            }
                        },
                        function(){
                            location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                        });
            } else if (allDeletedAudienceStr != "") {
                CARBON.showConfirmationDialog('Are you sure you want to delete the audience ' + allDeletedAudienceStr,
                    function(){
                        doEditFinish();
                    },
                    function(){
                        location.href = "idp-mgt-edit.jsp?issuer=<%=issuer%>"
                    });
            } else {
                doEditFinish();
            }
        }
    }
    function doEditFinish(){
        jQuery('#primary').removeAttr('disabled');
        <% if(issuer == null || issuer.equals("")){ %>
            jQuery('#idp-mgt-edit-form').attr('action','idp-mgt-add-finish.jsp');
        <% } %>
        jQuery('#idp-mgt-edit-form').submit();
    }
    function idpMgtCancel(){
        location.href = "idp-mgt-list.jsp"
    }
    function doValidation() {
        var reason = "";
        reason = validateEmpty("issuer");
        if (reason != "") {
            CARBON.showWarningDialog("IssuerId of IdP cannot be empty");
            return false;
        }
        for(var i=0; i <= rowId; i++){
            if(document.getElementsByName('rowname_'+i)[0] != null){
                reason = validateEmpty('rowname_'+i);
                if(reason != ""){
                    CARBON.showWarningDialog("Role name strings cannot be of zero length");
                    return false;
                }
            }
        }
        if(document.getElementsByName('audience') != null && document.getElementsByName('audience').length > 0){
            var audienceLength = document.getElementsByName('audience').length;
            for(var i = 0; i < audienceLength; i++){
                if(document.getElementsByName('audience')[i].value == ''){
                    CARBON.showWarningDialog("Audience strings cannot be of zero length");
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
            <fmt:message key='identity.providers'/>
        </h2>
        <div id="workArea">
            <form id="idp-mgt-edit-form" name="idp-mgt-edit-form" method="post" action="idp-mgt-edit-finish.jsp" enctype="multipart/form-data" >
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
                        <td class="leftCol-med labelField">
                            <label for="primary"><fmt:message key='idp.primary'/></label>
                        </td>
                        <td>
                            <div class="sectionCheckbox">
                                <input id="primary" name="primary" type="checkbox" <%=disabled%> <%=checked%>/>
                                <div class="sectionHelp">
                                    <fmt:message key='idp.primary.help'/>
                                </div>
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
                            <div id="publicCertDiv">
                                <% if(certData != null) { %>
                                <a id="publicCertDeleteLink" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key='idp.public.cert.delete'/></a>
                                    <table class="styledLeft">
                                        <thead><tr><th><fmt:message key='issuerdn'/></th>
                                            <th><fmt:message key='subjectdn'/></th>
                                            <th><fmt:message key='notafter'/></th>
                                            <th><fmt:message key='notbefore'/></th>
                                            <th><fmt:message key='serialno'/></th>
                                            <th><fmt:message key='version'/></th>
                                        </tr></thead>
                                        <tbody>
                                            <tr><td><%=certData.getIssuerDN()%></td>
                                                <td><%=certData.getSubjectDN()%></td>
                                                <td><%=certData.getNotAfter()%></td>
                                                <td><%=certData.getNotBefore()%></td>
                                                <td><%=certData.getSerialNumber()%></td>
                                                <td><%=certData.getVersion()%></td>
                                            </tr>
                                        </tbody>
                                    </table>
                                <% } %>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='idp.roles'/>:</td>
                        <td>
                            <a id="roleAddLink" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key='add.role'/></a>
                            <div style="clear:both"/>
                            <div class="sectionHelp">
                                <fmt:message key='idp.roles.help'/>
                            </div>
                            <table class="styledLeft" id="roleAddTable" style="display:none">
                                <thead><tr><th class="leftCol-big"><fmt:message key='idp.role'/></th><th><fmt:message key='idp.actions'/></th></tr></thead>
                                <tbody>
                                <% if(roles != null && !roles.isEmpty()){ %>
                                    <script>
                                        $(jQuery('#roleAddTable')).toggle();
                                    </script>
                                    <% for(int i = 0; i < roles.size(); i++){ %>
                                        <tr>
                                            <td><input type="text" value="<%=roles.get(i)%>" id="rowid_<%=i%>" name="rowname_<%=i%>"/></td>
                                            <td>
                                                <a title="<fmt:message key='delete.role'/>"
                                                   onclick="deleteRoleRow(this);return false;"
                                                   href="#"
                                                   class="icon-link"
                                                   style="background-image: url(images/delete.gif)">
                                                    <fmt:message key='delete.icon'/>
                                                </a>
                                            </td>
                                        </tr>
                                    <% } %>
                                <% } %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='role.mappings'/>:</td>
                        <td>
                            <input id="roleMappingFile" name="roleMappingFile" type="file" />
                            <div class="sectionHelp">
                                <fmt:message key='role.mappings.help'/>
                            </div>
                            <% if(roleMappings != null && !roleMappings.isEmpty()){ %>
                                <div id="roleMappingDiv">
                                    <a id="roleMappingDeleteLink" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key='role.mapping.delete'/></a>
                                    <table class="styledLeft">
                                        <thead><tr><th class="leftCol-big"><fmt:message key='idp.role'/></th><th><fmt:message key='tenant.role'/></th></tr></thead>
                                        <tbody>
                                            <% for(Map.Entry<String,String> entry:roleMappings.entrySet()){ %>
                                                <tr><td><%=entry.getKey()%></td><td><%=entry.getValue()%></td></tr>
                                            <% } %>
                                        </tbody>
                                    </table>
                                </div>
                            <% } %>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField"><fmt:message key='idp.audience'/>:</td>
                        <td>
                            <a id="audienceAddLink" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key='add.audience'/></a>
                            <div style="clear:both"/>
                            <div class="sectionHelp">
                                <fmt:message key='idp.audience.help'/>
                            </div>
                            <table class="styledLeft" id="audienceAddTable" style="display:none">
                                <thead><tr><th class="leftCol-big"><fmt:message key='idp.allowed.audience'/></th><th><fmt:message key='idp.actions'/></th></tr></thead>
                                <tbody>
                                <% if(audience != null && !audience.isEmpty()){ %>
                                <script>
                                    $(jQuery('#audienceAddTable')).toggle();
                                </script>
                                <% for(int i = 0; i < audience.size(); i++){ %>
                                <tr>
                                    <td><input type="text" value="<%=audience.get(i)%>" id="audience" name="audience"/></td>
                                    <td>
                                        <a title="<fmt:message key='delete.audience'/>"
                                           onclick="deleteAudienceRow(this);return false;"
                                           href="#"
                                           class="icon-link"
                                           style="background-image: url(images/delete.gif)">
                                            <fmt:message key='delete.icon'/>
                                        </a>
                                    </td>
                                </tr>
                                <% } %>
                                <% } %>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </table>
            </div>
            <!-- sectionSub Div -->
            <div class="buttonRow">
                <% if(bean != null){ %>
                    <input type="button" value="<fmt:message key='idp.update'/>" onclick="idpMgtUpdate();"/>
                <% } else { %>
                    <input type="button" value="<fmt:message key='idp.register'/>" onclick="idpMgtUpdate();"/>
                <% } %>
                <input type="button" value="<fmt:message key='idp.cancel'/>" onclick="idpMgtCancel();"/>
            </div>
            </form>
        </div>
    </div>

</fmt:bundle>