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

<style>
    .identity-box{
    	height:200px;
    }
</style>

<%
    String openidrealm = request.getParameter("openid.realm");
    String openidreturnto = request.getParameter("openid.return_to");
    String openidclaimedid = request.getParameter("openid.claimed_id");
    String openididentity = request.getParameter("openid.identity");
    String userName = request.getParameter("username");
    String errorMsg = request.getParameter("errorMsg");
    
    String openidrp  = openidreturnto;
    if (openidrp!=null && openidreturnto.indexOf("?")>0){
    	openidrp = openidreturnto.substring(0,openidreturnto.indexOf("?"));
    }
 %>

<fmt:bundle  basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.openid.Resources">
      
<script type="text/javascript">
    function doLogin(){
        var loginForm = document.getElementById('loginForm');

        var loadingImg = document.getElementById('loadingImg');
        var loginTable = document.getElementById('loginTable');
        
        loginTable.style.display = "none";
        loadingImg.style.display = "";
        loginForm.submit();
    }

    function setRememberMe() {
        var val = document.getElementById("chkRemember").checked;
        var remMe = document.getElementById("remember");

        if (val) {
            remMe.value = "true";
        } else {
            remMe.value = "false";
        }
    }
</script>

    <div id="middle">
        <h2><fmt:message key='signin.with.openid'/></h2>

        <div id="workArea">
            <fmt:message key='signin.to.authenticate1'/><strong>"<%=openidrp%>" </strong><fmt:message key='signin.to.authenticate2'/><%if(!openididentity.endsWith("/openid/")){%><strong> "<%=openididentity%>"</strong><% } else { %><strong> "<%=openididentity%>&lt;username&gt;"</strong><% } %>.
            <br/><br/>

            <table style="width:100%">
                <tr>
                    <td style="width:50%" id="loginTable">
                        <form action="../../openidserver" method="post" id="loginForm" onsubmit="doLogin()">
                            <input type="hidden" id='openid' name='openid' value="<%=openididentity%>"/>
                            <input type="hidden" id="remember" name="remember" value="false" />
                            
                            <div id="loginbox" class="identity-box">
                                <% if(userName == null || "".equals(userName.trim())) { %>
                                    <strong id="loginDisplayText"><fmt:message key='enter.username.password.to.signin'/></strong>
                                <% } else { %>
                                    <strong id="loginDisplayText"><fmt:message key='enter.password.to.signin'/></strong>
                                <% } %>
                                 <table id="loginTable">
                                    <tr height="20">
                                        <td colspan="2"></td>
                                    </tr>
	                                    <% if (errorMsg != null) { %>
	                                    <tr>
	                                        <td colspan="2" style="color: #dc143c;"><fmt:message
	                                                key='<%=errorMsg%>'/></td>
	                                    </tr>
	                                    <% } %>
                                        <%

                                         if(userName == null || "".equals(userName.trim())){

                                        %>
                                        <tr>
                                            <td><fmt:message key='username'/></td>
                                        <td>
                                            <input id='userName' name="userName" size='30'/>
                                        </td>
                                        </tr>

                                        <tr>
                                             <td><fmt:message key='password'/></td>
                                        <td>
                                            <input type="password" id='password' name="password" size='30'/>
                                        </td>
                                        </tr>

                                        <tr>
                                         <td>
                                            <input type="button" value="<fmt:message key='login'/>" class="button" onclick="doLogin()">
                                        </td>
                                        </tr>
                                        <%
                                            }  else {
                                        %>
                                        <tr>
                                            <td>
                                                <input type="password" id='password' name="password" size='30'/>
                                            </td>
                                             <td>
                                                <input type="button" value="<fmt:message key='login'/>" class="button" onclick="doLogin()">
                                            </td>
                                        </tr>

                                        <%
                                            }
                                        %>

                                        <tr>
                                        <td colspan="2"><input type="checkbox" id="chkRemember" onclick="setRememberMe();"><fmt:message key='remember.me'/></td>
                                       </tr>                                   
                                </table>
                                
                            </div>
                        </form>
                    </td>
                    <td style="width:50%;display:none" id="loadingImg">
                        <form action="openid_auth_submit.jsp" method="post" id="loginForm" onsubmit="doLogin()">
                            <input type="hidden" id='openid' name='openid' value="<%=openididentity%>"/>
                            
                            <div id="loginbox" class="identity-box">
                                <strong id="loginDisplayText"><fmt:message key='authenticating.please.wait'/></strong>

                                <h2></h2>
				                <div style="padding-left:30px; padding-top:25px;">
                                	<img src="images/ajax-loader.gif" vspace="20" />
                            	</div>
                                
                            </div>
                        </form>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</fmt:bundle>