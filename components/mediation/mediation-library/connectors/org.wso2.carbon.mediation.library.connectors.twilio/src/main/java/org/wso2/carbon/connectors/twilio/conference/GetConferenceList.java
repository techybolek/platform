package org.wso2.carbon.connectors.twilio.conference;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Conference;
import com.twilio.sdk.resource.list.ConferenceList;

/*
 * Class mediator for getting Conference instances.
 * For more information, see http://www.twilio.com/docs/api/rest/conference
 */
public class GetConferenceList extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// optional parameters for filtering resultant conferences.
		// See http://www.twilio.com/docs/api/rest/conference#list-get-filters
		String status = (String) messageContext.getProperty("TwilioStatus");
		String friendlyName = (String) messageContext.getProperty("TwilioFriendlyName");
		String dateCreated = (String) messageContext.getProperty("TwilioDateCreated");
		String dateUpdated = (String) messageContext.getProperty("TwilioDateUpdated");

		// Build a filter for the ConferenceList
		Map<String, String> filter = new HashMap<String, String>();
		if (status != null) {
			filter.put("Status", status);
		}
		if (friendlyName != null) {
			filter.put("FriendlyName", friendlyName);
		}
		if (dateCreated != null) {
			filter.put("DateCreated", dateUpdated);
		}
		if (dateUpdated != null) {
			filter.put("DateUpdated", dateUpdated);
		}

		try {
			getConferenceList(accountSid, authToken, log, filter);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getConferenceList(String accountSid, String authToken, SynapseLog log,
			Map<String, String> filter) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		ConferenceList conferences = twilioRestClient.getAccount().getConferences(filter);

		// TODO: change response
		// Loop over conferences and print out a property
		for (Conference conference : conferences) {
			log.auditLog("Conference Name: " + conference.getFriendlyName()
					+ "     Status: " + conference.getStatus());
		}
	}
}