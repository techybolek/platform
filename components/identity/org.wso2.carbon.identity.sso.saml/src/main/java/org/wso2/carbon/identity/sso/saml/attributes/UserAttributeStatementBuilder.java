/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.sso.saml.attributes;

import java.util.Iterator;
import java.util.Map;

import org.opensaml.Configuration;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;

public class UserAttributeStatementBuilder implements SAMLAttributeStatementBuilder {

	/**
	 * Reads the claims from the userstore 
	 */
	public AttributeStatement buildAttributeStatement(SAMLSSOAuthnReqDTO authReqDTO) throws IdentityException {
		Map<String, String> claims = SAMLSSOUtil.getAttributes(authReqDTO);
		AttributeStatement attStmt = null;
		if (claims != null) {
			attStmt = new AttributeStatementBuilder().buildObject();
			Iterator<String> ite = claims.keySet().iterator();

			for (int i = 0; i < claims.size(); i++) {
				Attribute attrib = new AttributeBuilder().buildObject();
				String claimUri = ite.next();
				attrib.setName(claimUri);
				// look
				// https://wiki.shibboleth.net/confluence/display/OpenSAML/OSTwoUsrManJavaAnyTypes
				XSStringBuilder stringBuilder =
				                                (XSStringBuilder) Configuration.getBuilderFactory()
				                                                               .getBuilder(XSString.TYPE_NAME);
				XSString stringValue =
				                       stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
				                                                 XSString.TYPE_NAME);
				stringValue.setValue(claims.get(claimUri));
				attrib.getAttributeValues().add(stringValue);
				attStmt.getAttributes().add(attrib);
			}
		}
		return attStmt;
	}

}
