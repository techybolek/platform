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

package org.wso2.carbon.mediation.library.connectors.twitter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import twitter4j.Twitter;

public class TwitterSendDirectMesage extends AbstractConnector {

	public static final String USER_ID = "userID";
	public static final String MESSAGE = "message";

	private static Log log = LogFactory.getLog(TwitterSendDirectMesage.class);

	@Override
	public void connect() throws ConnectException {
		MessageContext messageContext = getMessageContext();
		try {
			String userID = TwitterMediatorUtils.lookupFunctionParam(messageContext, USER_ID);
			String message = TwitterMediatorUtils.lookupFunctionParam(messageContext, MESSAGE);
			Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
			twitter.sendDirectMessage(Long.parseLong(userID), message);

			if (log.isDebugEnabled()) {
				log.info("sending direct message to user completed!");
			}
		} catch (Exception e) {
			log.error("Failed to login user: " + e.getMessage(), e);
			TwitterMediatorUtils.storeErrorResponseStatus(messageContext, e);
		}
	}
}
