/**
 * 
 */
package org.apache.oltu.openidconnect.as.util;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sga
 * 
 */
public class OIDCAuthzServerUtil {

	private static Logger log = LoggerFactory.getLogger(OIDCAuthzServerUtil.class);
	private static boolean DEBUG = log.isDebugEnabled();

	public static boolean isOIDCAuthzRequest(Set<String> scope) {
		if (DEBUG) {
			log.debug("is OIDC Authorization request " + scope.contains("openid"));
		}
		return scope.contains("openid");
	}

	public static boolean isOIDCAuthzRequest(String[] scope) {
		for(String openidscope : scope) {
			if (openidscope.equals("openid")) {
				if (DEBUG) {
					log.debug("openid scope found");
				}
				return true;
			}
		}
		return false;
	}
}
