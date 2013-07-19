package org.wso2.carbon.registry.rest.api.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class RestAPISecurityUtils {
	private static Log log = LogFactory.getLog(RestAPISecurityUtils.class);
	
	public static RestAPIAuthContext isAuthorized(PrivilegedCarbonContext context, String JWTToken) {
		RestAPIAuthContext authContext = new RestAPIAuthContext();
		if (context.getUsername() != null && 
				context.getTenantId() != org.wso2.carbon.base.MultitenantConstants.INVALID_TENANT_ID) {
			authContext.setUserName(context.getUsername());
			authContext.setTenantId(context.getTenantId());
			authContext.setAuthorized(true);
			
		} else if (JWTToken != null){
			String jWTTokenString = getTokenStringJWTToken(JWTToken);
			try {
				authContext.setUserName(getUserNameFromJWTTokenString(jWTTokenString));
				authContext.setTenantId(getTenantIdFromJWTTokenString(jWTTokenString));
				authContext.setAuthorized(true);
			} catch (Exception e) {
				log.error("Error retreiving UserName and TenantID" , e);
				authContext.setAuthorized(false);
			}
		} else {
			authContext.setAuthorized(false);
		}
		return authContext;
	}
	
	private static String getTokenStringJWTToken(String JWTToken) {
		String token = JWTToken.substring(JWTToken.indexOf(".") + 1, JWTToken.lastIndexOf("."));
		
		//decode the jwt token and convert it to string
		byte[] jwtBytes = token.getBytes();
		byte[] decodedBytes = Base64.decodeBase64(jwtBytes);
		String jwtStr = new String(decodedBytes);
		
		return jwtStr;
		
	}
	
	private static String getUserNameFromJWTTokenString(String JWTTokenString) throws Exception{
		//extract the enduser's username and returns
		String endUserClaimUri = "http://wso2.org/claims/enduser";
		int endUserIndex = JWTTokenString.indexOf(endUserClaimUri);
		JWTTokenString = JWTTokenString.substring(endUserIndex + endUserClaimUri.length() + 1);	
		String endUsername = JWTTokenString.substring(JWTTokenString.indexOf('"')+1); 
		endUsername = endUsername.substring(0, endUsername.indexOf('"'));
		
		return endUsername;
	}
	
	private static int getTenantIdFromJWTTokenString(String JWTTokenString) throws Exception{
		//extract the enduser's tenant id and returns
		String enduserTenantIdClaimUri = "http://wso2.org/claims/enduserTenantId";
		int endUserIndex = JWTTokenString.indexOf(enduserTenantIdClaimUri);
		JWTTokenString = JWTTokenString.substring(endUserIndex + enduserTenantIdClaimUri.length() + 1);	
		String enduserTenantId = JWTTokenString.substring(JWTTokenString.indexOf('"')+1); 
		enduserTenantId = enduserTenantId.substring(0, enduserTenantId.indexOf('"'));
		
		return Integer.parseInt(enduserTenantId);
	}
	
}
