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

/*
 * Class mediator for getting a participant of a given conference.
 * For more information, see http://www.twilio.com/docs/api/rest/conference#instance-subresources-participants
 */
public class GetParticipant extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// Get the conference Sid
		String conferenceSid = (String) messageContext.getProperty("TwilioConferenceSid");
		String callSidOfParticipant = (String) messageContext
				.getProperty("TwilioCallSidOfParticipant");

		try {
			getParticipant(accountSid, authToken, conferenceSid, callSidOfParticipant,
					log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getParticipant(String accountSid, String authToken,
			String conferenceSid, String callSidOfParticipant, SynapseLog log)
			throws IllegalArgumentException, TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get the conference
		Conference conference = twilioRestClient.getAccount()
				.getConference(conferenceSid);

		// TODO: change response
		// Get the participant by his call sid.
		// Refer https://www.twilio.com/docs/api/rest/participant
		Participant participant = conference.getParticipant(callSidOfParticipant);

		log.auditLog("Is Participant Muted: " + participant.isMuted());
	}
}
