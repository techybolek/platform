package org.wso2.carbon.mediation.library.connectors.twitter;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import twitter4j.Location;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

public class TwiterGetAvailableTrends extends AbstractTwitterConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {
		// TODO Auto-generated method stub
		try {
			Twitter twitter = new TwitterClientLoader(messageContext).loadApiClient();
			List<Location> locations = twitter.getAvailableTrends();
			OMElement resultElement = AXIOMUtil.stringToOM("<XMLPayload/>");
			for (Location location : locations) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("{ \"trend\" : ");
				String json = DataObjectFactory.getRawJSON(location);
				stringBuilder.append(json);
				stringBuilder.append("} ");
				OMElement element = super.parseJsonToXml(stringBuilder.toString());
				resultElement.addChild(element);
			}
			super.preparePayload(messageContext, resultElement);

		} catch (TwitterException te) {
			log.error("Failed to load  available trends: " + te.getMessage(), te);
			TwitterMediatorUtils.storeErrorResponseStatus(messageContext, te);
		} catch (Exception te) {
			// TODO Auto-generated catch block
			log.error("Failed to load  available trends: " + te.getMessage(), te);
			TwitterMediatorUtils.storeErrorResponseStatus(messageContext, te);
		}

	}

}
