/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth.ui;

import org.wso2.carbon.ui.util.CharacterEncoder;

import java.io.Serializable;
import java.util.Set;

/**
 * A Bean class which is used to store the OAuth parameters available in a OAuth request in the http
 * Session.
 */
public class OAuth2Parameters implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -8719088680725780804L;
	private String applicationName;
    private String redirectURI;
    private Set<String> scopes;
    private String state;
    private String responseType;
    private String clientId;
    private String nonce;
    private String display;
    private String prompt;
    private String request;
    private String request_uri;
    private String id_token_hint;
    private String login_hint;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = CharacterEncoder.getSafeText(applicationName);
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = CharacterEncoder.getSafeText(redirectURI);
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = CharacterEncoder.getSafeText(state);
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = CharacterEncoder.getSafeText(responseType);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = CharacterEncoder.getSafeText(clientId);
    }

	/**
	 * @return the nonce
	 */
    public String getNonce() {
	    return nonce;
    }

	/**
	 * @param nonce the nonce to set
	 */
    public void setNonce(String nonce) {
	    this.nonce = CharacterEncoder.getSafeText(nonce);
    }

	/**
	 * @return the display
	 */
    public String getDisplay() {
	    return display;
    }

	/**
	 * @param display the display to set
	 */
    public void setDisplay(String display) {
	    this.display = CharacterEncoder.getSafeText(display);
    }

	/**
	 * @return the prompt
	 */
    public String getPrompt() {
	    return prompt;
    }

	/**
	 * @param prompt the prompt to set
	 */
    public void setPrompt(String prompt) {
	    this.prompt = CharacterEncoder.getSafeText(prompt);
    }

	/**
	 * @return the request
	 */
    public String getRequest() {
	    return request;
    }

	/**
	 * @param request the request to set
	 */
    public void setRequest(String request) {
	    this.request = CharacterEncoder.getSafeText(request);
    }

	/**
	 * @return the request_uri
	 */
    public String getRequestURI() {
	    return request_uri;
    }

	/**
	 * @param request_uri the request_uri to set
	 */
    public void setRequestURI(String request_uri) {
	    this.request_uri = CharacterEncoder.getSafeText(request_uri);
    }

	/**
	 * @return the id_token_hint
	 */
    public String getIDTtokenHint() {
	    return id_token_hint;
    }

	/**
	 * @param id_token_hint the id_token_hint to set
	 */
    public void setIDTokenHint(String id_token_hint) {
	    this.id_token_hint = CharacterEncoder.getSafeText(id_token_hint);
    }

	/**
	 * @return the login_hint
	 */
    public String getLoginHint() {
	    return login_hint;
    }

	/**
	 * @param login_hint the login_hint to set
	 */
    public void setLoginHint(String login_hint) {
	    this.login_hint = CharacterEncoder.getSafeText(login_hint);
    }
}
