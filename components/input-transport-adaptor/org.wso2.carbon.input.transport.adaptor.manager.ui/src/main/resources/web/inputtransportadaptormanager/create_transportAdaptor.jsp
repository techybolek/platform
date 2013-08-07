
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.input.transport.adaptor.manager.ui.i18n.Resources">

<carbon:breadcrumb
        label="transportmanager.add"
        resourceBundle="org.wso2.carbon.input.transport.adaptor.manager.ui.i18n.Resources"
        topPage="false"
        request="<%=request%>"/>

<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../inputtransportadaptormanager/js/create_transport_adaptor_helper.js"></script>


<div id="middle">
<div id="workArea">
<h3><fmt:message key="transport.adaptor.create"/></h3>

<form name="inputForm" action="index.jsp" method="get" id="addTransport">
<table style="width:100%" id="transportAdd" class="styledLeft">
    <thead>
    <tr>
        <th><fmt:message key="transport.adaptor.details"/></th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td class="formRaw">
            <%@include file="inner_transport_adaptor_ui.jsp" %>
        </td>
    </tr>
    <tr>
        <td class="buttonRow">
            <input type="button" value="<fmt:message key="add.transport.adaptor"/>"
                   onclick="addTransport(document.getElementById('addTransport'))"/>
        </td>
    </tr>
    </tbody>
</table>


</form>
</div>
</div>
</fmt:bundle>
