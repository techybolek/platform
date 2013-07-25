/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.com.core.fieldcontext;

import org.wso2.carbon.com.core.fieldcontext.FieldContextPath.PathComponent;

/**
 * This class contains utility methods required for field contexts.
 */
public class FieldContextUtils {

	public static FieldContextPath parseFieldContextPath(String path) throws FieldContextException {
		if (path == null || path.length() == 0) {
			throw new FieldContextException("The field context path should not be empty");
		}
		String[] strComps = path.split("\\.");
		PathComponent[] pathComps = new FieldContextPath.PathComponent[strComps.length];
		for (int i = 0; i < pathComps.length; i++) {
			if (strComps[i].startsWith("[")) {
				if (!strComps[i].endsWith("]")) {
					throw new FieldContextException("The index based path component starting " +
							"with \"[\" must end with \"]: " + strComps[i]);
				}
				int indexVal;
				try {
					indexVal = Integer.parseInt(strComps[i].substring(0, strComps[i].length() - 1));
				} catch (NumberFormatException e) {
					throw new FieldContextException("Invalid index value: " + strComps[i]);
				}
				pathComps[i] = new PathComponent(true, indexVal);
			} else {
				pathComps[i] = new PathComponent(false, strComps[i]);
			}
		}
		return new FieldContextPath(pathComps);
	}
	
}
