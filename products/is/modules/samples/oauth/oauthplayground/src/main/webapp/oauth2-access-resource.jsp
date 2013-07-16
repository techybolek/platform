<%@page import="com.wso2.identity.oauth.sample.OAuth2ServiceClient"%>
<%@page import="org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.io.DataOutputStream" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>

<%
    OAuth2ServiceClient client = new OAuth2ServiceClient();
    String accessToken = request.getParameter("accessToken");
    String resource_url = request.getParameter("resource_url");

    if (accessToken==null || accessToken.trim().length()==0) {
%>
<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true";
</script>
<%
    }

    if(config.getInitParameter("setup").equals("IS")){
        OAuth2TokenValidationRequestDTO  oauthReq = new OAuth2TokenValidationRequestDTO();
        oauthReq.setAccessToken(accessToken);
        oauthReq.setTokenType("bearer");
        try {
            // Validate the OAuth access token.
            if (!client.validateAuthenticationRequest(oauthReq)) {
%>
<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true&error='Invalid Access Attempt'";
</script>
<%
            }
            RequestDispatcher view = request.getRequestDispatcher("my-photos.jsp");
            view.forward(request, response);
        } catch(Exception e) {
%>
<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true&error=<%=e.getMessage()%>";
</script>
<%
        }
    }else if(getServletConfig().getInitParameter("setup").equals("AM")){
        String result = executePost(resource_url,"",accessToken);
        out.print(result);
    }else{
%>
<script type="text/javascript">
    window.location = "oauth2.jsp?reset=true&error='Invalid Setup value'";
</script>
<%
    }
%>

<%!
    public static String executePost(String targetURL, String urlParameters,String accessToken)
    {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization","Bearer "+accessToken);

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }
%>