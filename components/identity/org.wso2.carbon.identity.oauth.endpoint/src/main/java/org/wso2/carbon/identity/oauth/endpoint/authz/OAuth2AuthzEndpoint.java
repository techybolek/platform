package org.wso2.carbon.identity.oauth.endpoint.authz;

import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.OIDC;
import org.apache.oltu.openidconnect.as.util.OIDCAuthzServerUtil;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.ui.OAuth2Parameters;
import org.wso2.carbon.identity.oauth.ui.OAuthConstants;
import org.wso2.carbon.identity.oauth.ui.internal.OAuthUIServiceComponentHolder;
import org.wso2.carbon.identity.oauth.util.EndpointUtil;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientValidationResponseDTO;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.util.CharacterEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/authorize")
public class OAuth2AuthzEndpoint {

    private static Log log = LogFactory.getLog(OAuth2AuthzEndpoint.class);

    @GET
    @Path("/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request, MultivaluedMap<String, String> paramMap) throws URISyntaxException {


            String clientId = CharacterEncoder.getSafeText(request.getParameter("client_id"));
            OAuth2Parameters oauth2Params = (OAuth2Parameters) request.getSession().getAttribute(OAuthConstants.OAUTH2_PARAMS);
            try{
                if(clientId != null){
                    String redirectURL = handleOAuthAuthorizationRequest(request);
                    OAuthResponse response = OAuthASResponse
                            .authorizationResponse(request, HttpServletResponse.SC_FOUND)
                            .location(redirectURL)
                            .buildQueryMessage();
                    return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();

            } else if(oauth2Params != null){
                String redirectURL = handleAuthorizationRequest(request);
                    OAuthResponse response = OAuthASResponse
                            .authorizationResponse(request, HttpServletResponse.SC_FOUND)
                            .location(redirectURL)
                            .buildQueryMessage();
                    return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();
            } else {
                log.error("Invalid Authorization Request");
                return Response.status(HttpServletResponse.SC_FOUND).location(new URI(EndpointUtil.getErrorPageURL(request, null, OAuth2ErrorCodes.INVALID_REQUEST, "Invalid Authorization Request"))).build();
            }
        } catch (OAuthSystemException e) {
            log.error(e.getMessage(), e);
            return Response.status(HttpServletResponse.SC_FOUND).location(new URI(EndpointUtil.getErrorPageURL(request, null, OAuth2ErrorCodes.INVALID_REQUEST, e.getMessage()))).build();
        }

    }

    private String handleOAuthAuthorizationRequest(HttpServletRequest req) throws OAuthSystemException {
        OAuth2ClientValidationResponseDTO clientDTO = null;
        try {
            // Extract the client_id and callback url from the request, because constructing an Amber
            // Authz request can cause an OAuthProblemException exception. In that case, that error
            // needs to be passed back to client. Before that we need to validate the client_id and callback URL
            String clientId = CharacterEncoder.getSafeText(req.getParameter("client_id"));
            String callbackURL = CharacterEncoder.getSafeText(req.getParameter("redirect_uri"));

            if (clientId != null) {
                clientDTO = validateClient(req, clientId, callbackURL);
            } else { // Client Id is not present in the request.
                log.warn("Client Id is not present in the authorization request.");
                return EndpointUtil.getErrorPageURL(req, clientDTO, OAuth2ErrorCodes.INVALID_REQUEST,
                        "Invalid Request. Client Id is not present in the request");
            }
            // Client is not valid. Do not send this error back to client, send
            // to an error page instead.
            if (!clientDTO.isValidClient()) {
                return EndpointUtil.getErrorPageURL(req, clientDTO, clientDTO.getErrorCode(),
                        clientDTO.getErrorMsg());
            }

            // Now the client is valid, redirect him to the authorization page.
            OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(req);
            OAuth2Parameters params = new OAuth2Parameters();
            params.setApplicationName(clientDTO.getApplicationName());
            params.setRedirectURI(clientDTO.getCallbackURL());
            params.setResponseType(oauthRequest.getResponseType());
            params.setScopes(oauthRequest.getScopes());
            params.setState(oauthRequest.getState());
            params.setClientId(clientId);

            // OpenID Connect request parameters
            if (OIDCAuthzServerUtil.isOIDCAuthzRequest(oauthRequest.getScopes())) {
                // these parameters must be processed in the complete implementation
                params.setNonce(oauthRequest.getParam(OIDC.AuthZRequest.NONCE));
                params.setDisplay(oauthRequest.getParam(OIDC.AuthZRequest.DISPLAY));
                params.setRequest(oauthRequest.getParam(OIDC.AuthZRequest.REQUEST));
                params.setRequestURI(oauthRequest.getParam(OIDC.AuthZRequest.REQUEST_URI));
                params.setIDTokenHint(oauthRequest.getParam(OIDC.AuthZRequest.ID_TOKEN_HINT));
                params.setLoginHint(oauthRequest.getParam(OIDC.AuthZRequest.LOGIN_HINT));
                String prompt = oauthRequest.getParam(OIDC.AuthZRequest.PROMPT);
                params.setPrompt(prompt);
                req.getSession().setAttribute(OAuthConstants.OIDCSessionConstant.OIDC_REQUEST, "true");
                req.getSession().setAttribute(OAuthConstants.OIDCSessionConstant.OIDC_RP, params.getApplicationName());
                if(prompt != null) { // processing prompt
                    // prompt can be four values {none, login, consent, select_profile}
                    String[] prompts = prompt.trim().split(" ");
                    boolean contains_none = prompt.contains("none");
                    if (prompts.length > 1 && contains_none) { // invalid combination
                        log.error("Invalid prompt variable combination. " + prompt);
                        EndpointUtil.getErrorPageURL(req, clientDTO, OAuth2ErrorCodes.INVALID_REQUEST,
                                "Invalid prompt combination. The valune none cannot be used with others");
                    }
                    Object logedInUser = req.getSession().getAttribute(OAuthConstants.OIDCSessionConstant.OIDC_LOGGED_IN_USER);
                    if (contains_none && logedInUser == null) {
                        log.error("User not authenticated. " + prompt);
                        EndpointUtil.getErrorPageURL(req, clientDTO, OAuthConstants.OAUTH_ERROR_CODE,
                                "Received prompt none but no authenticated user found");
                    }
                    if(!prompt.contains("login")) { // we should not log the user
                        req.getSession().setAttribute(OAuthConstants.OAUTH2_PARAMS, params);
                        String loginFinishPage = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth2-authn-finish.jsp";
                        loginFinishPage = loginFinishPage.replace("/oauth2/authorize", "");
                        return loginFinishPage;
                    }
                }
            }
            req.getSession().setAttribute(OAuthConstants.OAUTH2_PARAMS, params);
            return EndpointUtil.getLoginPageURL(req, clientDTO, params);

        } catch (OAuthProblemException e) {
            log.error(e.getError(), e.getCause());
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
                    .location(clientDTO.getCallbackURL()).buildQueryMessage().getLocationUri();
        }
    }

    private OAuth2ClientValidationResponseDTO validateClient(HttpServletRequest req, String clientId, String callbackURL) {
        return EndpointUtil.getOAuth2Service().validateClientInfo(clientId,callbackURL);
    }

    public String handleAuthorizationRequest(HttpServletRequest request) throws OAuthSystemException {

        OAuth2Parameters oauth2Params =
                (OAuth2Parameters) request.getSession()
                        .getAttribute(OAuthConstants.OAUTH2_PARAMS);

        // user has denied the authorization. Send back the error code.
        if ("true".equals(request.getParameter("deny"))) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .setError(OAuth2ErrorCodes.ACCESS_DENIED)
                    .location(oauth2Params.getRedirectURI()).setState(oauth2Params.getState())
                    .buildQueryMessage().getLocationUri();
        }

        OAuth2AuthorizeRespDTO authzRespDTO = authorize(request, oauth2Params);
        // Authentication Failure, send back to the login page
        if (!authzRespDTO.isAuthenticated()) {
            String loginPageInSession = (String) request.getSession().getAttribute("loginPage");
            return loginPageInSession+"&auth_status=failed";
        }
        OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                OAuthASResponse.authorizationResponse(request,
                        HttpServletResponse.SC_FOUND);
        OAuthResponse oauthResponse;
        // user is authorized.
        if (authzRespDTO.isAuthorized()) {

            if (ResponseType.CODE.toString().equals(oauth2Params.getResponseType())) {
                builder.setCode(authzRespDTO.getAuthorizationCode());
            } else if (ResponseType.TOKEN.toString().equals(oauth2Params.getResponseType())) {
                builder.setAccessToken(authzRespDTO.getAccessToken());
                builder.setExpiresIn(String.valueOf(60 * 60));
            }

            builder.setParam("state", oauth2Params.getState());
            String redirectURL = authzRespDTO.getCallbackURI();
            oauthResponse = builder.location(redirectURL).buildQueryMessage();

        } else {
            OAuthProblemException oauthException =
                    OAuthProblemException.error(authzRespDTO.getErrorCode(),
                            authzRespDTO.getErrorMsg());
            oauthResponse =
                    OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                            .error(oauthException).location(authzRespDTO.getCallbackURI())
                            .setState(oauth2Params.getState()).buildQueryMessage();
        }
        //response.setStatus(HttpServletResponse.SC_FOUND);
        request.getSession().removeAttribute(OAuthConstants.OAUTH2_PARAMS);
        return handleOpenIDConnectParams(request, oauthResponse.getLocationUri(), oauth2Params);
    }

    private String handleOpenIDConnectParams(HttpServletRequest request, String redirectUrl,
                                             OAuth2Parameters oauth2Params) {

        if ("true".equals(request.getSession()
                .getAttribute(OAuthConstants.OIDCSessionConstant.OIDC_REQUEST))) {
            // store the logged in user in session to support prompt = none
            String loggedInUser = request.getParameter(OAuthConstants.REQ_PARAM_OAUTH_USER_NAME);
            if (loggedInUser != null) {
                request.getSession()
                        .setAttribute(OAuthConstants.OIDCSessionConstant.OIDC_LOGGED_IN_USER,
                                loggedInUser);
            } else {
                loggedInUser =
                        (String) request.getSession()
                                .getAttribute(OAuthConstants.OIDCSessionConstant.OIDC_LOGGED_IN_USER);
            }
            // load the users approved applications
            String appName = oauth2Params.getApplicationName();
            boolean isRpInStore =
                    OAuthUIServiceComponentHolder.getInstance()
                            .getOauth2UserAppsStore()
                            .isUserRPInStore(loggedInUser,
                                    appName);

            if(isRpInStore && oauth2Params.getPrompt() != null && oauth2Params.getPrompt().contains("none")) {
                return redirectUrl; // should not prompt for consent
            }
            redirectUrl = EndpointUtil.getUserConsentURL(request, null, oauth2Params, loggedInUser, redirectUrl);
            // store the response and forward for user consent
            request.getSession().setAttribute(OAuthConstants.OIDCSessionConstant.OIDC_RESPONSE, redirectUrl);
        }
        return redirectUrl;
    }

    private OAuth2AuthorizeRespDTO authorize(HttpServletRequest req, OAuth2Parameters oauth2Params) {

        // authenticate and issue the authorization code
        OAuth2AuthorizeReqDTO authzReqDTO = new OAuth2AuthorizeReqDTO();
        authzReqDTO.setCallbackUrl(oauth2Params.getRedirectURI());
        authzReqDTO.setConsumerKey(oauth2Params.getClientId());
        authzReqDTO.setResponseType(oauth2Params.getResponseType());
        authzReqDTO.setScopes(oauth2Params.getScopes().toArray(new String[oauth2Params.getScopes().size()]));
        String username = req.getParameter(OAuthConstants.REQ_PARAM_OAUTH_USER_NAME);
        if (username == null) {
            username = (String) req.getSession().getAttribute(OAuthConstants.OIDCSessionConstant.OIDC_LOGGED_IN_USER);
        }
        authzReqDTO.setUsername(username);
        authzReqDTO.setPassword(req.getParameter(OAuthConstants.REQ_PARAM_OAUTH_USER_PASSWORD));
        return EndpointUtil.getOAuth2Service().authorize(authzReqDTO);
    }
}
