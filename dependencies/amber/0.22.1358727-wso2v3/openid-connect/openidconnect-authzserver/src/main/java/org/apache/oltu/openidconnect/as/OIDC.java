/**
 * 
 */
package org.apache.oltu.openidconnect.as;

/**
 * @author sga
 *
 */
public class OIDC {
	
	public static class AuthZRequest {
		public static final String NONCE = "nonce";
		public static final String DISPLAY = "display";
		public static final String PROMPT = "prompt";
		public static final String REQUEST = "request";
		public static final String REQUEST_URI = "request_uri";
		public static final String ID_TOKEN_HINT = "id_token_hint";
		public static final String LOGIN_HINT = "login_hint";
	}
	
	public static class Response {
		public static final String ID_TOKEN = "id_token";
	}
	
	public static class Error {
		public static final String LOGIN_REQUIRED = "login_required";
		public static final String CONSENT_REQUIRED = "consent_required";
	}
	
	public static class Prompt {
		public static final String NONE = "none";
		public static final String LOGIN = "login";
		public static final String CONSENT = "consent";
		public static final String SELECT_PROFILE = "select_profile";
	}

}
