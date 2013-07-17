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
    if(issuer == null){
        issuer = "";
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

    jQuery(document).ready(function(){
        jQuery('#publicCertDeleteLink').click(function(){
            $(jQuery('#publicCertDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deletePublicCert";
            input.id = "deletePublicCert";
            input.value = "true";
            document.forms['idp-mgt-form'].appendChild(input);
        })
    })
    var deletedRows = [];
    function deleteRoleRow(obj){
       if(jQuery(obj).parent().prev().children()[0].value != ''){
            deletedRows.push(jQuery(obj).parent().prev().children()[0].value);
        }
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
    jQuery(document).ready(function(){
        jQuery('#roleMappingDeleteLink').click(function(){
            $(jQuery('#roleMappingDiv')).toggle();
            var input = document.createElement('input');
            input.type = "hidden";
            input.name = "deleteRoleMappings";
            input.id = "deleteRoleMappings";
            input.value = "true";
            document.forms['idp-mgt-form'].appendChild(input);
        })
    })
    function idpMgtUpdate(){
        if(doValidation()){
            var allDeletedStr = "";
            for(var i = 0;i<deletedRows.length;i++){
                if(i < deletedRows.length-1){
                    allDeletedStr += deletedRows[i] + ", ";
                } else {
                    allDeletedStr += deletedRows[i] + "?";
                }
            }
            if(allDeletedStr != "") {
                CARBON.showConfirmationDialog('Are you sure you want to delete the role ' + allDeletedStr,
                        function(){
                            if(jQuery('#deleteRoleMappings').val() == 'true'){
                                CARBON.showConfirmationDialog('Are you sure you want to delete all the role mappings?',
                                        function (){
                                            if(jQuery('#deletePublicCert').val() == 'true'){
                                                CARBON.showConfirmationDialog('Are you sure you want to delete the public certificate of ' +
                                                        jQuery('#issuer').val() + '?',
                                                        function (){
                                                            jQuery('#idp-mgt-form').submit();
                                                        },
                                                        function(){
                                                            location.href = "idp-mgt.jsp"
                                                        });
                                            } else {
                                                jQuery('#idp-mgt-form').submit();
                                            }
                                        },
                                        function(){
                                            location.href = "idp-mgt.jsp"
                                        });
                            } else {
                                jQuery('#idp-mgt-form').submit();
                            }
                        },
                        function(){
                            location.href = "idp-mgt.jsp"
                        });
            } else if(jQuery('#deleteRoleMappings').val() == 'true'){
                CARBON.showConfirmationDialog('Are you sure you want to delete all the role mappings?',
                        function (){
                            if(jQuery('#deletePublicCert').val() == 'true'){
                                CARBON.showConfirmationDialog('Are you sure you want to delete the public certificate of ' +
                                        jQuery('#issuer').val() + '?',
                                        function (){
                                            jQuery('#idp-mgt-form').submit();
                                        },
                                        function(){
                                            location.href = "idp-mgt.jsp"
                                        });
                            } else {
                                jQuery('#idp-mgt-form').submit();
                            }
                        },
                        function(){
                            location.href = "idp-mgt.jsp"
                        });
            } else if(jQuery('#deletePublicCert').val() == 'true'){
                CARBON.showConfirmationDialog('Are you sure you want to delete the public certificate of ' +
                        jQuery('#issuer').val() + '?',
                        function (){
                            jQuery('#idp-mgt-form').submit();
                        },
                        function(){
                            location.href = "idp-mgt.jsp"
                        });
            } else {
                jQuery('#idp-mgt-form').submit();
            }
        }
    }
    function idpMgtDelete(){
        CARBON.showConfirmationDialog('Are you sure you want to delete "'  + jQuery('#issuer').val() + '" IdP information?',
                function (){
                    var input = document.createElement('input');
                    input.type = "hidden";
                    input.name = "delete";
                    input.value = "true";
                    document.forms['idp-mgt-form'].appendChild(input);
                    jQuery('#idp-mgt-form').submit();
                },
                null);
    }
    function idpMgtCancel(){
        location.href = "idp-mgt.jsp"
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
                                <% if(roles != null && roles.size() > 0){ %>
                                    <script>
                                        $(jQuery('#roleAddTable')).toggle();
                                    </script>
                                    <% for(int i = 0; i < roles.size(); i++){ %>
                                        <tr>
                                            <td><input type="text" value="<%=roles.get(i)%>" id="rowid_<%=i%>" name="rowname_<%=i%>"/></td>
                                            <td>
                                                <a title="<fmt:message key='idp.role.delete'/>"
                                                   onclick="deleteRoleRow(this);return false;"
                                                   href="#"
                                                   class="icon-link"
                                                   style="background-image: url(images/delete.gif)">
                                                    <fmt:message key='idp.role.delete'/>
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
                        <td class="leftCol-med labelField"><fmt:message key='idp.role.mappings'/>:</td>
                        <td>
                            <input id="roleMappingFile" name="roleMappingFile" type="file" />
                            <div class="sectionHelp">
                                <fmt:message key='idp.role.mappings.help'/>
                            </div>
                            <% if(roleMappings != null && roleMappings.size()>0){ %>
                                <div id="roleMappingDiv">
                                    <a id="roleMappingDeleteLink" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key='idp.role.mapping.delete'/></a>
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
                </table>
            </div>
            <!-- sectionSub Div -->
            <div class="buttonRow">
                <% if(bean != null){ %>
                    <input type="button" value="<fmt:message key='idp.update'/>" onclick="idpMgtUpdate();"/>
                <% } else { %>
                    <input type="button" value="<fmt:message key='idp.register'/>" onclick="idpMgtUpdate();"/>
                <% } %>
                <input type="button" value="<fmt:message key='idp.delete'/>" onclick="idpMgtDelete();"/>
                <input type="button" value="<fmt:message key='idp.cancel'/>" onclick="idpMgtCancel();"/>
            </div>
            </form>
        </div>
    </div>

</fmt:bundle>