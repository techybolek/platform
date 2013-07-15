<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ page import="org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean" %>
<%@ page import="org.wso2.carbon.registry.properties.stub.utils.xsd.Property" %>
<%@ page import="org.wso2.carbon.registry.properties.ui.clients.PropertiesServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.wso2.carbon.registry.core.utils.PaginationContext" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>


<%
    PropertiesServiceClient client_ = new PropertiesServiceClient(config, session);
    try {
    if (request.getParameter("name") != null) {
        if (request.getParameter("oldName") != null) {
            client_.updateProperty(request);
        } else if (request.getParameter("remove") != null) {
            client_.removeProperty(request);
        } else {
            client_.setProperty(request);
        }
    }
    } catch (Exception e) {
        response.setStatus(500);
%>
<%=e.getMessage()%>
<%
        return;
    }
    String requestedPage = request.getParameter("page");
    int start;
    int count = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);
    if (requestedPage != null) {
        start = (int) ((Integer.parseInt(requestedPage) - 1) * (RegistryConstants.ITEMS_PER_PAGE * 1.5));
    } else {
        start = 1;
    }
    PaginationContext.init(start, count, "", "", 1500);
    PropertiesBean propertiesBean_ = client_.getProperties(request);
    if (propertiesBean_ == null) {
        return;
    }
%>
<%--<script type="text/javascript">
   function retentionError(){
       CARBON.showWarningDialog('<fmt:message key="retention.warn"/>' );
       return;
   }
</script>--%>
<fmt:bundle basename="org.wso2.carbon.registry.properties.ui.i18n.Resources">


    <div id="propertiesSum" class="summeryStyle">
        <%
            if (propertiesBean_.getSysProperties() == null ||
                    propertiesBean_.getSysProperties().length == 0) {
        %>
        <fmt:message key="no.properties.defined.yet"/>
        <%
            } else if (propertiesBean_.getSysProperties().length == 1) {
        %>
        <fmt:message key="one.property"/>
        <%
        } else {
        %>
        <%=Integer.parseInt(session.getAttribute("row_count").toString())%> <fmt:message key="properties"/>
        <%
            }
        %>
    </div>

    <div id="propertiesList">
        <%
            HashMap properties = new HashMap();
            if (!(propertiesBean_.getSysProperties() == null ||
                    propertiesBean_.getSysProperties().length == 0)) {
                Property[] propArray = propertiesBean_.getProperties();
                for (Property aPropArray : propArray) {
                    properties.put(aPropArray.getKey(), aPropArray.getValue());
                }
        %>
        <table cellpadding="0" cellspacing="0" border="0" class="styledLeft">
            <thead>
            <tr>
                <th style="width:40%" align="left"><fmt:message key="name"/></th>
                <th align="left"><fmt:message key="value"/></th>
                <th align="left"><fmt:message key="action"/></th>
            </tr>
            </thead>
            <%
                    String[] sysProperties = propertiesBean_.getSysProperties();
                    int itemsPerPage = (int) (RegistryConstants.ITEMS_PER_PAGE * 1.5);

                    int pageNumber;
                    if (requestedPage != null && requestedPage.length() > 0) {
                        pageNumber = new Integer(requestedPage);
                    } else {
                        pageNumber = 1;
                    }

                    int rowCount = Integer.parseInt(session.getAttribute("row_count").toString());
                    int numberOfPages;
                    if (rowCount % itemsPerPage == 0) {
                        numberOfPages = rowCount / itemsPerPage;
                    } else {
                        numberOfPages = rowCount / itemsPerPage + 1;
                    }

                for (int i = 0; i < sysProperties.length; i++) {
                    String name = sysProperties[i];
                    String value = "";
                    if (properties.get(name) != null) {
                        value = (String) properties.get(name);
                    }

            %>

            <tr id="propEditPanel_<%=i%>" style="display:none;">
                <td><input id="propRPath_<%=i%>" type="hidden"
                           value="<%=propertiesBean_.getPathWithVersion()%>"/><input
                        id="oldPropName_<%=i%>"
                        type="hidden"
                        value="<%=name%>"/><input
                        value="<%=name%>" type="text" id="propName_<%=i%>" class="propEditNameSelector"/></td>
                <td><input value="<%=value%>" id="propValue_<%=i%>" type="text" /></td>
                <td>
                    <a class="icon-link" style="background-image:url(../properties/images/save-button.gif);"
                       id="propSaveButton_<%=i%>"
                       onclick="showHideCommon('propViewPanel_<%=i%>');showHideCommon('propEditPanel_<%=i%>'); editProperty('<%=i%>')">
                        <fmt:message key="save"/>
                    </a>

                    <a class="icon-link" style="background-image:url(../admin/images/cancel.gif);"
                       onclick="showHideCommon('propViewPanel_<%=i%>');showHideCommon('propEditPanel_<%=i%>');">
                        <fmt:message key="cancel"/>
                    </a>
                </td>
            </tr>


            <tr id="propViewPanel_<%=i%>">
            	<%
            	String tmpName = name.replaceAll("<","&lt;");
            	tmpName = tmpName.replaceAll(">","&gt;");
            	
            	String tmpValue = value.replaceAll("<","&lt;");
            	tmpValue = tmpValue.replaceAll(">","&gt;");
            	%>
                <td><span class="__propName"><%=tmpName%></span><span class="__propNameRef propViewNameSelector"
                                                                   style="display:none;"><%=name%></span>
                </td>
                <td><span class="__propValue"><%=tmpValue%></span><span class="__propValueRef"
                                                                     style="display:none;"><%=value%></span>
                </td>


                <% if (propertiesBean_.getPutAllowed() && !propertiesBean_.getVersionView()) { %>
                <td style="width:150px">
                    <%
                        if(Boolean.parseBoolean(propertiesBean_.getWriteLocked())){
                    %>
                    <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/edit.gif);"
                       onclick="retentionError();">
                        <fmt:message key="edit"/>
                    </a>
                    <%}else {%>
                     <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/edit.gif);"
                       onclick="showHideCommon('propViewPanel_<%=i%>');showHideCommon('propEditPanel_<%=i%>');$('propName_<%=i%>').focus();">
                        <fmt:message key="edit"/>
                    </a>
                    <%}%>
                    <%if(Boolean.parseBoolean(propertiesBean_.getDeleteLocked())){%>

                    <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"
                       onclick="retentionError();"
                       style="margin-left:5px;cursor:pointer;"><fmt:message key="delete"/></a>
                    <%}else {%>
                     <a class="icon-link registryWriteOperation" style="background-image:url(../admin/images/delete.gif);"
                       onclick="removeProperty('<%=name.replace("\\", "\\\\")%>');"
                       style="margin-left:5px;cursor:pointer;"><fmt:message key="delete"/></a>
                    <%}%>
                </td>
                <% } else {%>
                <td>&nbsp;</td>
                <% } %>

            </tr>

            <%
                }
            %>
        </table>
            <carbon:resourcePaginator pageNumber="<%=pageNumber%>" numberOfPages="<%=numberOfPages%>"
                                      resourceBundle="org.wso2.carbon.registry.properties.ui.i18n.Resources"
                                      nextKey="next" prevKey="prev"
                                      paginationFunction="<%="loadPropertiesDiv('" + request.getParameter("path").replaceAll("&","%26") + "', {0})"%>" />
        <%
            }
        %>
        <%
                if (properties.get("registry.absent") != null &&
                    properties.get("registry.absent").equals("true")){
        %>
        <script type="text/javascript">CARBON.showWarningDialog("<fmt:message key="could.not.create.symlink"/>")</script>
        <%
            }
        %>
    </div>

</fmt:bundle>
