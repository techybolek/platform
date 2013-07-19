<table style="width:100%">
    <tr>
        <td style="width:50%" id="basicAuthTable">
            <form action="../../commonauth" method="post" id="loginForm">
                <div id="loginbox" class="identity-box">
                    <table id="loginTable1">
                        <tr height="22">
                            <td colspan="2"></td>
                        </tr>
                        <% if (loginFailed) { %>
                        <tr>
                            <td colspan="2" style="color: #dc143c;"><fmt:message
                                    key='<%=errorMessage%>'/></td>
                        </tr>
                        <% } %>
                        <tr>
                        <td><fmt:message key='username'/></td>
                            <td>
                                <input type="text" id='username' name="username"
                                       size='30'/>
                                <input type="hidden" name="<%=SAMLSSOConstants.SESSION_DATA_KEY%>" 
                                value="<%=request.getParameter(SAMLSSOConstants.SESSION_DATA_KEY)%>"/>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key='password'/></td>
                            <td>
                                <input type="password" id='password' name="password"
                                       size='30'/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><input type="checkbox" id="chkRemember" name="chkRemember"><fmt:message key='remember.me'/></td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <input type="submit" value="<fmt:message key='login'/>"
                                       class="button">
                            </td>
                        </tr>
                    </table>
                </div>
            </form>
        </td>
    </tr>
</table>