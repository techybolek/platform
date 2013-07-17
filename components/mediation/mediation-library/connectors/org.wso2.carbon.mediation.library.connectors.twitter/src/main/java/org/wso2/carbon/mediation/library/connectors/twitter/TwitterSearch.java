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

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class TwitterSearch extends AbstractTwitterConnector {

	private static Log log = LogFactory.getLog(TwitterSearch.class);
	
	public static final String SEARCH_STRING = "search";
	public static final String LANG = "lang";
	public static final String LOCALE = "locale";
	public static final String MAX_ID = "maxId";
	public static final String SINCE = "since";
	public static final String SINCE_ID = "sinceId";
	public static final String GEO_CODE = "geocode";
	public static final String RADIUS = "radius";
	public static final String UNIT = "unit";
	public static final String UNITL = "until";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wso2.carbon.mediation.library.connectors.core.AbstractConnector#connect
	 * ()
	 */
	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		try {

			Query query = new Query(TwitterMediatorUtils.lookupFunctionParam(
					messageContext, SEARCH_STRING));
			polulateOptionalParamters(messageContext, query);
			Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
			OMElement element = this.performSearch(twitter, query);
			if(log.isDebugEnabled()){
				log.error("seach twitter result"+ element.toString());
			}
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
	 * Populating additional parameters in for the search query
	 * 
	 * @param messageContext
	 * @param query
	 */
	private void polulateOptionalParamters(MessageContext messageContext, Query query) {

		String lang = TwitterMediatorUtils.lookupFunctionParam(messageContext, LANG);
		String locale = TwitterMediatorUtils.lookupFunctionParam(messageContext, LOCALE);
		String maxID = TwitterMediatorUtils.lookupFunctionParam(messageContext, MAX_ID);
		String since = TwitterMediatorUtils.lookupFunctionParam(messageContext, SINCE);
		String sinceID = TwitterMediatorUtils.lookupFunctionParam(messageContext,
				SINCE_ID);
		String geocode = TwitterMediatorUtils.lookupFunctionParam(messageContext,
				GEO_CODE);
		String radius = TwitterMediatorUtils.lookupFunctionParam(messageContext, RADIUS);
		String unit = TwitterMediatorUtils.lookupFunctionParam(messageContext, UNIT);
		String until = TwitterMediatorUtils.lookupFunctionParam(messageContext, UNITL);

		if (lang != null && !lang.isEmpty()) {
			query.setLang(lang);
		}
		if (locale != null && !locale.isEmpty()) {
			query.setLocale(locale);
		}
		if (maxID != null && !maxID.isEmpty()) {
			query.setMaxId(Long.parseLong(maxID));
		}
		if (since != null && !since.isEmpty()) {
			query.setSince(since);
		}
		if (sinceID != null && !sinceID.isEmpty()) {
			query.setSinceId(Long.parseLong(sinceID));
		}
		if (geocode != null && !geocode.isEmpty() && radius != null && !radius.isEmpty()
				&& unit != null && !unit.isEmpty()) {
			String[] codes = geocode.split(",");
			query.setGeoCode(
					new GeoLocation(Double.parseDouble(codes[0]), Double
							.parseDouble(codes[1])), Double.parseDouble(radius), unit);
		}

		if (until != null && !until.isEmpty()) {
			query.setUntil(until);
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
	private OMElement performSearch(Twitter twitter, Query query)
			throws XMLStreamException, TwitterException, JSONException, IOException {
		OMElement resultElement = AXIOMUtil.stringToOM("<XMLPayload/>");
		QueryResult result;
		result = twitter.search(query);
		List<Status> results = result.getTweets();
		for (Status tweet : results) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("{ \"status\" : ");
			String json = DataObjectFactory.getRawJSON(tweet);
			stringBuilder.append(json);
			stringBuilder.append("}");
			OMElement element = super.parseJsonToXml(stringBuilder.toString());
			resultElement.addChild(element);
		}
		return resultElement;

	}

}
