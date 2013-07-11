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
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

public class TwitterSearch extends AbstractConnector {

	public static final String SEARCH_STRING = "search";
	
	/* (non-Javadoc)
	 * @see org.wso2.carbon.mediation.library.connectors.core.AbstractConnector#connect()
	 */
	@Override
	public void connect() throws ConnectException {
		// TODO Auto-generated method stub
		MessageContext messageContext = getMessageContext();
		try {
			Query query = new Query(TwitterMediatorUtils.lookupFunctionParam(messageContext, SEARCH_STRING));
			Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
			OMElement element = this.performSearch(twitter, query);
			SOAPBody soapBody = messageContext.getEnvelope().getBody();
			for (Iterator itr = soapBody.getChildElements(); itr.hasNext();) {
				OMElement child = (OMElement) itr.next();
				child.detach();
			}
			for (Iterator itr = element.getChildElements(); itr.hasNext();) {
				OMElement child = (OMElement) itr.next();
				soapBody.addChild(child);
			}
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
			OMElement element = JSONUtil.parseJsonToXml(json);
			resultElement.addChild(element);
		}
		return resultElement;

	}

	public static void main(String ar[]) {
		TwitterSearch search = new TwitterSearch();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthAccessToken("1114764380-LA2T7swZe6T5DsNlz0PJD63quXhJc3s1Vi5VB5s");
		cb.setOAuthAccessTokenSecret("hYOONb9FvSwDfRgmH8CbaY6klXPSwUEmORrEbqNAIe8");
		cb.setOAuthConsumerKey("7l0Kgm2qfFbh59BkM2zfmw");
		cb.setOAuthConsumerSecret("31AWkrAwn5mzolVqdKKxGlPeH8nSNxyqJ2bVtuRfU");
		cb.setJSONStoreEnabled(true);
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		try {
	        twitter.verifyCredentials();
        } catch (TwitterException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
        }
		
		Query query = new Query("wso2");
		try {
			OMElement element =search.performSearch(twitter, query);
			System.out.println("e"+element.toString());
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TwitterException e) {
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
