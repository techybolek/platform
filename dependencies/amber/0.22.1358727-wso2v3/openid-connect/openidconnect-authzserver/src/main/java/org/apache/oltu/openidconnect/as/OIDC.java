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

}
