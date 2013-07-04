/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.rest.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * this class validates whether the resource path is null or not. If null return
 * a BAD Request error.
 * If not null, check whether user specifies the page start and size (no of
 * records to be fetched out)
 * less than 0, returns a HTTP BAD request error code.
 */
public class RestPathPaginationValidation {

	private static Log log = LogFactory.getLog(RestPathPaginationValidation.class);

	protected static int validate(String resourcePath, int start, int size) {
		// null check for resource path and invalid argument check for
		// pagination params.
		if (resourcePath.length() == 0 || start < 0 || size < 0) {
			if (log.isDebugEnabled()) {
				log.debug("invalid parameters have been passed with the request");
			}
			return -1;
		}
		return 0;
	}

	protected static int validate(String resourcePath) {
		// null check for resource path.
		if (resourcePath.length() == 0) {
			if (log.isDebugEnabled()) {
				log.debug("resource path has not been specified in the request");
			}
			return -1;
		}
		return 0;
	}

	protected static int validate(int start, int size) {
		// invalid value check for the pagination parameters
		if (start < 0 || size < 0) {
			if (log.isDebugEnabled()) {
				log.debug("negative values were passed to start and size variables");
			}
			return -1;
		}
		return 0;
	}
}

