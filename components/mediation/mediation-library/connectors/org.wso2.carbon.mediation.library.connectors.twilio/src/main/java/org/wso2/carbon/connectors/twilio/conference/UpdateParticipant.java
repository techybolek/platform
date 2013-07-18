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
import com.twilio.sdk.resource.instance.Participant;

/*
 * Class mediator for updating a particulars of a participant in a given conference.
 * For more information, seehttp://www.twilio.com/docs/api/rest/participant
 */
public class UpdateParticipant extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// Get the conference Sid
		String conferenceSid = (String) messageContext.getProperty("TwilioConferenceSid");

		// Get the call sid of the participant
		String callSidOfParticipant = (String) messageContext
				.getProperty("TwilioCallSidOfParticipant");

		// Optional parameter
		// Also see
		// http://www.twilio.com/docs/api/rest/participant#instance-post
		String muted = (String) messageContext.getProperty("TwilioParticipantMuted");

		try {
			updateParticipant(accountSid, authToken, conferenceSid, callSidOfParticipant,
					muted, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void updateParticipant(String accountSid, String authToken,
			String conferenceSid, String callSidOfParticipant, String muted,
			SynapseLog log) throws TwilioRestException, IllegalArgumentException {
		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get the conference
		Conference conference = twilioRestClient.getAccount()
				.getConference(conferenceSid);

		// Get the participant by his call sid.
		// Refer https://www.twilio.com/docs/api/rest/participant
		Participant participant = conference.getParticipant(callSidOfParticipant);

		Map<String, String> params = new HashMap<String, String>();
		params.put("Muted", muted);

		// update the matching participant instance
		participant.update(params);
		// TODO: change response
		log.auditLog("Is Participant Muted: " + participant.isMuted());

	}
}