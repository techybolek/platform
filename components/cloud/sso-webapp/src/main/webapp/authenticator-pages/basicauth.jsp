<section class="start_content" >
    <% if (loginFailed) { %>
    <div class="message error">
        <ul>
            <li><fmt:message key='<%=errorMessage%>'/></li>
        </ul>
    </div>

    <% } %>

    <form action="../../commonauth" method="post" id="loginForm" class="well form-horizontal">
        <div class="input_row">
            <label for="username"><fmt:message key='username'/>:</label>
            <input class="input-large" type="text" id='username' name="username"
                   size='30'/>
            <input type="hidden" name="<%=SAMLSSOConstants.SESSION_DATA_KEY%>"
                   value="<%=request.getParameter(SAMLSSOConstants.SESSION_DATA_KEY)%>"/>
        </div>
        <div class="input_row">
            <label for="password"><fmt:message key='password'/>:</label>
            <input type="password" id='password' name="password" class="input-large"
                   size='30'/>
        </div>
        <div class="input_row btn_row">
            <input class="btn" type="submit" value="<fmt:message key='login'/>">

        </div>

    </form>
    <div class="input_row btn_row" style="margin-bottom:20px">
    <a href="https://cloudmgt.cloudpreview.staging.wso2.com/cloudmgt/site/pages/register.jag" class="link" style="margin-top:10px;display:block;float:left">Sign Up</a>
 <% String queryString ="../../authenticationendpoint/samlsso/samlsso_ot_login.jsp?SAMLRequest="+request.getParameter("SAMLRequest")+"&issuer="+request.getParameter("issuer")+"&sessionDataKey="+request.getParameter("sessionDataKey")+"&commonAuthCallerPath="+request.getParameter("commonAuthCallerPath")+"&forceAuthenticate="+request.getParameter("forceAuthenticate"); %>

 <form  action="<%=queryString%>" method="post" id="mockForm" style="float:right;margin-top:10px;">

                         <input type="hidden" name="<%=SAMLSSOConstants.SESSION_DATA_KEY%>"
                   value="<%=request.getParameter(SAMLSSOConstants.SESSION_DATA_KEY)%>"/>


                      <a href="javascript:{}" onclick="document.getElementById('mockForm').submit(); return false;">Sign in with wso2.com credentials</a>

                   </form>
     </div>
</section>




