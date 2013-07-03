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
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
           
<%
String selectedProfile = request.getParameter("selectedProfile");
String[] profiles = request.getParameterValues("profile");
String[] claimTags = request.getParameterValues("claimTag");
String[] claimValues = request.getParameterValues("claimValue");
%>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.openid.Resources">
    <link media="all" type="text/css" rel="stylesheet" href="css/openid-provider.css">

    <script type="text/javascript">
        function submitProfileSelection() {
            document.profileSelection.submit();
        }
    </script>

    <div id="middle">
        <h2>OpenID User Profile</h2>

        <div id="workArea">
            <script type="text/javascript">
                function approved() {
                 	document.getElementById("hasApprovedAlways").value = "false";
                    document.profile.submit();
                }
                function approvedAlways() {
                    document.getElementById("hasApprovedAlways").value = "true";
                    document.profile.submit();
                }                
            </script>

            <form name="profileSelection" action="openid_profile_view.jsp" method="POST">
                <table style="margin-bottom:10px;">
                    <tr>
                        <td style="height:25px; vertical-align:middle;"><fmt:message key='profile'/></td>
                        <td style="height:25px; vertical-align:middle;"><select name="selectedProfile" onchange="submitProfileSelection();">
                            <% 
                               if(profiles != null) {
                                for (int i = 0; i < profiles.length; i++) {
	                                String profile = profiles[i];
	                                if(profile.equals(request.getParameter("selectedProfile"))){
		                            %>
		                            <option value="<%=profile%>" selected="selected"><%=profile%>
		                            </option>
		                            <%}
		                            else{
		                              %><option value="<%=profile%>"><%=profile%></option><%
		                            }
	                            }
	                         }
		                    %>
                            </select>
                        </td>
                    </tr>
                </table>
            </form>

            <form action="../../openidserver" id="profile" name="profile">

                <table cellpadding="0" cellspacing="0" class="card-box">
                    <tr>
                        <td><img src="images/card-box01.jpg"/></td>
                        <td class="card-box-top"></td>
                        <td><img src="images/card-box03.jpg"/></td>
                    </tr>
                    <tr>
                        <td class="card-box-left"></td>
                        <td class="card-box-mid">
                            <div class="user-pic"><img src="images/profile-picture.gif"
                                                       align="bottom"/></div>                       
                            <%
                            if(claimTags != null) {
	                            for (int i = 0; i < claimTags.length; i++) {
	                                String claimTag = claimTags[i];
	                            %>
		                            <div><strong><%=claimTag%></strong></div>
		                            <div><%=claimValues[i]%></div>
		                            <br/>
		                    <%
	                            }
                            }
                            %>
                        </td>
                        <td class="card-box-right"></td>
                    </tr>
                    <tr>
                        <td><img src="images/card-box07.jpg"/></td>
                        <td class="card-box-bottom"></td>
                        <td><img src="images/card-box05.jpg"/></td>
                    </tr>
                </table>
                <br/>
                <table width="100%" class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="buttonRow" colspan="2">
                        	<input type="button" class="button" id="approve" name="approve"
                                             onclick="javascript: approved(); return false;"
                                             value="<fmt:message key='approve'/>"/>
                       		<input type="button" class="button" id="chkApprovedAlways" onclick="javascript: approvedAlways();" value="<fmt:message key='approve.always'/>"/>
                       		<input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways" value="false" />
                       		<input class="button" type="reset" value="<fmt:message key='cancel'/>"
                               onclick="javascript:document.location.href='../../carbon/admin/login.jsp'"/>
                       </td>
                    </tr>
                    </tbody>
                </table>
            </form>

        </div>
    </div>
</fmt:bundle>