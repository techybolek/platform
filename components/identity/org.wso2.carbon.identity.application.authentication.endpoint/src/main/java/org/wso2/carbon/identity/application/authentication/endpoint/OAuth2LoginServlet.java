package org.wso2.carbon.identity.application.authentication.endpoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuth2LoginServlet extends HttpServlet{

    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.getRequestDispatcher("oauth2/oauth2_login.jsp").forward(request,response);

    }

}
