<script type="text/javascript">

function activateSubmit() {
    	var mockusername = document.getElementById("mockusername");
        var username =document.getElementById("username");
        username.value =  mockusername.value.replace("@","..")+"@wso2.org";
        
        return true;
    }



</script>
<section class="start_content">
    <% if (loginFailed) { %>
    <div class="message error">
        <ul>
            <li><fmt:message key='<%=errorMessage%>'/></li>
        </ul>
    </div>

    <% } %>
 <% String queryString ="../../commonauth?SAMLRequest="+request.getParameter("SAMLRequest")+"&issuer="+request.getParameter("issuer")+"&sessionDataKey="+request.getParameter("sessionDataKey")+"&commonAuthCallerPath="+request.getParameter("commonAuthCallerPath")+"&forceAuthenticate="+request.getParameter("forceAuthenticate");%>

    <form action="<%=queryString%>"  onsubmit="return activateSubmit();" method="post" id="loginForm" class="well form-horizontal">
        <div class="input_row">
            <label for="username"><fmt:message key='username'/>:</label>
              <input class="input-large" type="text" id='mockusername' name="mockusername"

                   size='30'/>

             <input type="hidden" class="input-large" type="text" id='username' name="username"

                   size='30'/>
            <input type="hidden" name="<%=SAMLSSOConstants.SESSION_DATA_KEY%>"
                   value="<%=request.getParameter(SAMLSSOConstants.SESSION_DATA_KEY)%>"/>

        </div>
        <div class="input_row">
            <label for="password"><fmt:message key='password'/>:</label>
            <input type="password" id='password' name="password" class="input-large"
                   size='30'/>
        </div>
        <div class="input_row btn_row" style="margin-bottom:20px">
            <input class="btn" type="submit" value="<fmt:message key='login'/>">
            <a href="https://wso2.com/user/register" class="link" style="margin-top:10px;display:block">Sign up with wso2.com</a>
        </div>

    </form>
</section>




