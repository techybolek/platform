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

import twitter4j.GeoLocation;
import twitter4j.GeoQuery;
import twitter4j.Place;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

public class TwitterSearchPlaces extends AbstractTwitterConnector{

	public static final String SEARCH_BY_LATITUDE = "latitude";
	
	public static final String SEARCH_LONGITUDE = "longitude";
	
	public static final String SEARCH_IP = "ip";
	
	
	@Override
	public void connect() throws ConnectException {
		// TODO Auto-generated method stub
		MessageContext messageContext = getMessageContext();
		try {
			String latitude = TwitterMediatorUtils.lookupFunctionParam(messageContext, SEARCH_BY_LATITUDE);
			String longitude = TwitterMediatorUtils.lookupFunctionParam(messageContext, SEARCH_LONGITUDE);
			String ip = TwitterMediatorUtils.lookupFunctionParam(messageContext, SEARCH_IP);
			GeoQuery query = new GeoQuery(new GeoLocation(Double.parseDouble(latitude), Double.parseDouble(longitude)));
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
	private OMElement performSearch(Twitter twitter, GeoQuery query) throws XMLStreamException, TwitterException, JSONException, IOException {
		OMElement resultElement = AXIOMUtil.stringToOM("<XMLPayload/>");
		List<Place> results = twitter.searchPlaces(query);
		
		for (Place place : results) {
		    StringBuilder stringBuilder = new StringBuilder();
		    stringBuilder.append("{ \"Place\" : ");
			String json = DataObjectFactory.getRawJSON(place);
			stringBuilder.append(json);
			stringBuilder.append("} ");
			//System.out.println(stringBuilder.toString());
			OMElement element =super.parseJsonToXml(stringBuilder.toString());
			resultElement.addChild(element);
		}
		return resultElement;

	}
	
	
	public static void main(String ar[]) {
		TwitterSearchPlaces search = new TwitterSearchPlaces();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthAccessToken("1114764380-JNGKRkrUFUDCHC0WdmjDurZ3wwi9BV6ysbDRYca");
		cb.setOAuthAccessTokenSecret("vkpELc3OWK0TM0BjYcPLCn22Wm3HRliNUyx1QSxg4JI");
		cb.setOAuthConsumerKey("6U5CNaHKh7hVSGpk1CXo6A");
		cb.setOAuthConsumerSecret("EvTEzc3jj9Z1Kx58ylNfkpnuXYuCeGgKhkVkziYNMs");
		cb.setJSONStoreEnabled(true);
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		try {
	        twitter.verifyCredentials();
        } catch (TwitterException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
        }
		
        GeoQuery query = new GeoQuery(new GeoLocation(40.71435, -74.00597));
		
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
