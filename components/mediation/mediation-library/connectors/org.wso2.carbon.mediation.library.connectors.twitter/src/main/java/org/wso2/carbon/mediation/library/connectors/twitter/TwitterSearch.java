/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediation.library.connectors.twitter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class TwitterSearch extends AbstractTwitterConnector {

	public static final String SEARCH_STRING = "search";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wso2.carbon.mediation.library.connectors.core.AbstractConnector#connect
	 * ()
	 */
	@Override
	public void connect() throws ConnectException {
		// TODO Auto-generated method stub
		MessageContext messageContext = getMessageContext();
		try {
			Query query = new Query(TwitterMediatorUtils.lookupFunctionParam(messageContext, SEARCH_STRING));
			Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
			OMElement element = this.performSearch(twitter, query);
			super.preparePayload(messageContext, element);
		} catch (TwitterException te) {
			log.error("Failed to search twitter : " + te.getMessage(), te);
			TwitterMediatorUtils.storeErrorResponseStatus(messageContext, te);
		} catch (Exception te) {
			log.error("Failed to search generic: " + te.getMessage(), te);
			TwitterMediatorUtils.storeErrorResponseStatus(messageContext, te);
		}
	}

	/**
	 * Performing the searching operation for the given search criteria.
	 * 
	 * @param twitter
	 * @param query
	 * @return
	 * @throws XMLStreamException
	 * @throws TwitterException
	 * @throws JSONException
	 * @throws IOException
	 */
	private OMElement performSearch(Twitter twitter, Query query) throws XMLStreamException, TwitterException, JSONException, IOException {
		OMElement resultElement = AXIOMUtil.stringToOM("<XMLPayload/>");
		QueryResult result;
		result = twitter.search(query);
		List<Status> results = result.getTweets();
		for (Status tweet : results) {
			String json = DataObjectFactory.getRawJSON(tweet);
			OMElement element = super.parseJsonToXml(json);
			resultElement.addChild(element);
		}
		return resultElement;

	}
}
