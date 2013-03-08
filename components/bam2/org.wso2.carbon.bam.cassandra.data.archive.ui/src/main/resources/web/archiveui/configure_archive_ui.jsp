<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.cassandra.data.archive.ui.CassandraDataArchiveAdminClient" %>
<%@ page import="org.wso2.carbon.bam.cassandra.data.archive.stub.util.ArchiveConfiguration" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%! public static final String DATE_PATTERN = "dd/MM/yyyy";

%><fmt:bundle basename="org.wso2.carbon.bam.cassandra.data.archive.ui.i18n.Resources">
    <script type="text/javascript">
        /*jQuery(document).init(function () {*/
        function handleOK() {

        }

        function showHideTr(showTrId, hideTrId) {
            var showTrElement = document.getElementById(showTrId);
            showTrElement.style.display = "";
            var hideTrElement = document.getElementById(hideTrId);
            hideTrElement.style.display = "none";
        }

        function showHideOneTr(showHideTrId) {
            var theTr = document.getElementById(showHideTrId);
            if (theTr.style.display == "none") {
                theTr.style.display = "";
            } else {
                theTr.style.display = "none";
            }
        }

    </script>

<carbon:breadcrumb
            label="cassandra.data.archive"
            resourceBundle="org.wso2.carbon.bam.cassandra.data.archive.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

<%
        String streamName = "";
        String version = "";
        String from = "";
        String to ="";
        String noOfDays ="";
        String cron = "";
        String username = "";
        String password = "";
        String scheduling = "";
        boolean isCassandraExternal = false;
        String connectionURL = "";

        String setConfig = request.getParameter("setConfig"); // hidden parameter to check if the form is being submitted
        if(request.getParameter("stream_name")!=null){
            streamName = request.getParameter("stream_name");
        }
        if(request.getParameter("version")!=null){
            version = request.getParameter("version");
        }
        if(request.getParameter("from")!=null){
            from = request.getParameter("from");
        }
        if(request.getParameter("to")!=null){
            to = request.getParameter("to");
        }
        if(request.getParameter("specified_date")!=null){
            noOfDays = request.getParameter("specified_date");
        }
        if(request.getParameter("cron")!=null){
            cron = request.getParameter("cron");
        }
        if(request.getParameter("username")!=null){
            username = request.getParameter("username");
        }
        if(request.getParameter("password")!=null){
            password = request.getParameter("password");
        }
        if(request.getParameter("date")!=null){
            scheduling = request.getParameter("date");
        }
        if(request.getParameter("cassandra_cluster")!=null){
            String cassandraExternal = request.getParameter("cassandra_cluster");
            if(cassandraExternal.equals("on")){
                isCassandraExternal = true;
            }
        }
        if(request.getParameter("connection_url")!=null && isCassandraExternal){
            connectionURL = request.getParameter("connection_url");
        }


        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        CassandraDataArchiveAdminClient client = new CassandraDataArchiveAdminClient(
                cookie, backendServerURL, configContext, request.getLocale());

        ArchiveConfiguration archiveConfiguration = null;

        if (setConfig != null) {    // form submitted request to set eventing config
            archiveConfiguration = new ArchiveConfiguration();
            if (streamName != null && !streamName.equals("")) {
                archiveConfiguration.setStreamName(streamName.trim());
            }
            if (version != null && !version.equals("")) {
                archiveConfiguration.setVersion(version.trim());
            }
            if (from != null && !from.equals("")) {
                Date fromDate = new SimpleDateFormat(DATE_PATTERN).parse(from.trim());
                archiveConfiguration.setStartDate(fromDate);
            }
            if(to!=null && !to.equals("")){
                Date toDate =  new SimpleDateFormat(
                        DATE_PATTERN).parse(to.trim());
                archiveConfiguration.setEndDate(toDate);
            }
            if(noOfDays!=null && !noOfDays.equals("")){
                archiveConfiguration.setNoOfDays(Integer.parseInt(noOfDays.trim()));
            }
            if(cron!=null && !cron.equals("")){
                archiveConfiguration.setCronExpression(cron.trim());
            }
            if(username !=null && !username.equals("")){
                archiveConfiguration.setUserName(username.trim());
            }
            if(password!=null && !password.equals("")){
                archiveConfiguration.setPassword(password.trim());
            }
            if(scheduling!=null && !scheduling.equals("")){
                boolean isScheduled = false;
                if(scheduling.equals("date_range")){
                    isScheduled = false;
                }else {
                    isScheduled = true;
                }
                archiveConfiguration.setSchedulingOn(isScheduled);
            }
            if(connectionURL!=null && isCassandraExternal){
                archiveConfiguration.setConnectionURL(connectionURL);
            }

        }

            try {
                if (archiveConfiguration != null && archiveConfiguration.getStreamName()!=null) {
                    client.archiveCassandraData(archiveConfiguration);
%>
    <script language="JavaScript" type="text/JavaScript">

        CARBON.showInfoDialog("Configuration submitted successfully!", handleOK);
        /*});*/
    </script>
    <%
        }
    } catch (Exception e) {
            CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
    %>
    <jsp:include page="../admin/error.jsp"/>
    <%
    }
    %>
    <link rel="stylesheet" href="js_libs/css/ui-lightness/jquery-ui-1.10.0.custom.min.css" />
    <link rel="stylesheet" href="js_libs/" />
    <style type="text/css">
        .ui-datepicker{
            font: 62.5% "Trebuchet MS", sans-serif;
            margin: 50px;
        }
    </style>
<div id="middle">
<h2>
    <fmt:message key="bam.cassandra.data.archive.config"/>
</h2>

<div id="workArea">
<div id="result"></div>
<p>&nbsp;</p>

<form action="configure_archive_ui.jsp" method="post">
<input type="hidden" name="setConfig" value="on"/>
<table width="100%" class="styledLeft" style="margin-left: 0px;">
<thead>
<tr>
    <th colspan="4">
        <fmt:message key="archive.configuration"/>
    </th>
</tr>
</thead>

<tr>
    <td style="border-color:#FFFFFF;"><fmt:message key="stream.name"/></td>
    <td style="border-color:#FFFFFF;"><input id="stream_name" class="serviceConfigurationInput" type="text" name="stream_name" value="<%=streamName%>"/></td>
    <td style="border-color:#FFFFFF;"><fmt:message key="version"/></td>
    <td style="border-color:#FFFFFF;"><input id="version" type="text" class="serviceConfigurationInput" name="version" value="<%=version%>"/></td>
</tr>
    <tr>
        <td style="border-color:#FFFFFF;"><input id="date_range_radio_btn" type="radio" onchange="showHideTr('date_range_tr','specified_date_tr')" value="date_range" name="date" checked/>Date range (DD/MM/YYYY)</td>
        <td style="border-color:#FFFFFF;"></td>
        <td style="border-color:#FFFFFF;"></td>
        <td style="border-color:#FFFFFF;"></td>
    </tr>
    <tr>
        <td style="border-color:#FFFFFF;"><input id="specified_date_radio_btn" type="radio" onchange="showHideTr('specified_date_tr','date_range_tr')" value="below_date" name="date"/>Below this no of days</td>
        <td style="border-color:#FFFFFF;"></td>
        <td style="border-color:#FFFFFF;"></td>
        <td style="border-color:#FFFFFF;"></td>
    </tr>
    <tr id="date_range_tr">
    <td style="border-color:#FFFFFF;"><fmt:message key="from"/></td>
    <td style="border-color:#FFFFFF;"><input id="datePicker_from" type="text" class="serviceConfigurationInput" name="from" value="<%=from%>"/></td>
    <td style="border-color:#FFFFFF;"><fmt:message key="to"/></td>
    <td style="border-color:#FFFFFF;"><input id="datePicker_to" type="text" class="serviceConfigurationInput" name="to" value="<%=to%>"/></td>
</tr>
<tr id="specified_date_tr" style="display:none">
    <td style="border-color:#FFFFFF;">No of days:</td>
    <td style="border-color:#FFFFFF;"><input id="specified_date" type="text" name="specified_date" value="<%=noOfDays%>"/></td>
    <td style="border-color:#FFFFFF;">Cron expression:</td>
    <td style="border-color:#FFFFFF;"><input id="cron" type="text" name="cron" value="<%=cron%>"/></td>
</tr>

    <tr>
        <td style="border-color:#FFFFFF;"><input type="checkbox" name="cassandra_cluster" onchange="showHideOneTr('external_cassandra_cluster')"/>External cassandra cluster</td>
        <td style="border-color:#FFFFFF;"></td>
        <td style="border-color:#FFFFFF;"></td>
        <td style="border-color:#FFFFFF;"></td>
    </tr>
    <tr id="external_cassandra_cluster" style="display:none">
        <td style="border-color:#FFFFFF;">Connection URL:</td>
        <td style="border-color:#FFFFFF;"><input id="connection_url" type="text" name="connection_url" value="<%=connectionURL%>"/><font color="#C0C0C0">ex:- 10.100.60.150:9160,10.100.60.151:9160</font></td>
        <td style="border-color:#FFFFFF;"></td>
        <td style="border-color:#FFFFFF;"></td>
    </tr>
    <tr id="authentication">
        <td style="border-color:#FFFFFF;">Username:</td>
        <td style="border-color:#FFFFFF;"><input id="username" type="text" name="username" value="<%=username%>"/></td>
        <td style="border-color:#FFFFFF;">Password:</td>
        <td style="border-color:#FFFFFF;"><input id="password" type="password" name="password" value="<%=password%>"/>
        </td>
    </tr>
 <tr>
     <td colspan="4" class="buttonRow" style="border-color:#FFFFFF;">
            <input type="submit" class="button" value="<fmt:message key="submit"/>"
                   id="updateStats"/>
      </td>
 </tr>
</table>
</form>

    <script src="js_libs/js/jquery-1.9.0.js"></script>
    <script src="js_libs/js/jquery-ui-1.10.0.custom.min.js"></script>

    <script>
        $(function() {
            $("#datePicker_from").datepicker();
            $("#datePicker_from").datepicker( "option", "dateFormat", "dd/mm/yy" );
            $("#datePicker_to").datepicker();
            $("#datePicker_to").datepicker( "option", "dateFormat", "dd/mm/yy" );
        });
    </script>
</div>

</fmt:bundle>