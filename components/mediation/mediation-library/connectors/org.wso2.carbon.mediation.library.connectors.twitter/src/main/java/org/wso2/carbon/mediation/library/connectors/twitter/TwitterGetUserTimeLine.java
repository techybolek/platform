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
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

public class TwitterGetUserTimeLine extends AbstractTwitterConnector {

	public static final String USER_ID = "userID";

	public static final String PAGE = "page";

	public static final String SCREEN_NAME = "screenName";

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		if (log.isDebugEnabled()) {
			log.info("executing twitter get user time line");
		}

		try {
			String userID = TwitterMediatorUtils.lookupFunctionParam(messageContext,
					USER_ID);
			String page = TwitterMediatorUtils.lookupFunctionParam(messageContext, PAGE);

			Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();

			List<Status> results = null;
			if (userID != null && page != null && !userID.isEmpty() && !page.isEmpty()) {
				results = twitter.getUserTimeline(Long.parseLong(userID),
						new Paging(Long.parseLong(page)));
			} else if (userID != null && !userID.isEmpty()) {
				results = twitter.getUserTimeline(Long.parseLong(userID));
			} else if (page != null && !page.isEmpty()) {
				results = twitter.getUserTimeline(new Paging(Long.parseLong(page)));
			} else {
				results = twitter.getUserTimeline();
			}
			OMElement element = this.performSearch(results);

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
	 * Performing the searching operation for the given Geo Query criteria.
	 * 
	 * @param twitter
	 * @param query
	 * @return
	 * @throws XMLStreamException
	 * @throws TwitterException
	 * @throws JSONException
	 * @throws IOException
	 */
	private OMElement performSearch(List<Status> results) throws XMLStreamException,
			TwitterException, JSONException, IOException {
		OMElement resultElement = AXIOMUtil.stringToOM("<XMLPayload/>");

		for (Status place : results) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("{ \"status\" : ");
			String json = DataObjectFactory.getRawJSON(place);
			stringBuilder.append(json);
			stringBuilder.append("} ");
			// System.out.println(stringBuilder.toString());
			OMElement element = super.parseJsonToXml(stringBuilder.toString());
			resultElement.addChild(element);
		}
		return resultElement;

	}

	public static void main(String ar[]) {
		TwitterGetUserTimeLine getUserTimeLine = new TwitterGetUserTimeLine();
		TwitterSearchPlaces search = new TwitterSearchPlaces();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthAccessToken("1114764380-JNGKRkrUFUDCHC0WdmjDurZ3wwi9BV6ysbDRYca");
		cb.setOAuthAccessTokenSecret("vkpELc3OWK0TM0BjYcPLCn22Wm3HRliNUyx1QSxg4JI");
		cb.setOAuthConsumerKey("6U5CNaHKh7hVSGpk1CXo6A");
		cb.setOAuthConsumerSecret("EvTEzc3jj9Z1Kx58ylNfkpnuXYuCeGgKhkVkziYNMs");
		cb.setJSONStoreEnabled(true);
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();

		String userID = "15479536";
		String page = "";

		List<Status> results = null;
		try {
			if (!userID.isEmpty() && !page.isEmpty()) {
				results = twitter.getUserTimeline(Long.parseLong(userID),
						new Paging(Long.parseLong(page)));
			} else if (!userID.isEmpty()) {
				results = twitter.getUserTimeline(Long.parseLong(userID));
			} else if (!page.isEmpty()) {
				results = twitter.getUserTimeline(new Paging(Long.parseLong(page)));
			} else {
				results = twitter.getUserTimeline();
			}
			OMElement element = getUserTimeLine.performSearch(results);
			System.out.println(element);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
