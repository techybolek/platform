package org.wso2.carbon.connectors.twilio.conference;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Conference;
import com.twilio.sdk.resource.instance.Participant;
import com.twilio.sdk.resource.list.ParticipantList;

/*
 * Class mediator for getting a list of participants from a given conference.
 * For more information, see http://www.twilio.com/docs/api/rest/conference#instance-subresources-participants
 */
public class GetParticipantList extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// Get the conference Sid
		String conferenceSid = (String) messageContext.getProperty("TwilioConferenceSid");

		try {
			getParticipantList(accountSid, authToken, conferenceSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getParticipantList(String accountSid, String authToken,
			String conferenceSid, SynapseLog log) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get the conference
		Conference conference = twilioRestClient.getAccount()
				.getConference(conferenceSid);

		// Get the participants list by the defined filter
		ParticipantList participantList = conference.getParticipants();

		// TODO: change response
		for (Participant participant : participantList) {
			log.auditLog("Participant Call Sid: " + participant.getCallSid());
		}
	}
}
