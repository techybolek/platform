package org.wso2.carbon.connectors.twilio.call;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Transcription;
/*
 * Class mediator for getting a call transcription.
 * For more information, see http://www.twilio.com/docs/api/rest/transcription
 */
public class GetTranscription extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");
		String transcriptionSid = (String) messageContext
				.getProperty("TwilioTranscriptionSid");

		try {
			getTranscription(accountSid, authToken, transcriptionSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getTranscription(String accountSid, String authToken,
			String transcriptionSid, SynapseLog log) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get the transcription by it's Sid
		Transcription transcription = twilioRestClient.getAccount().getTranscription(
				transcriptionSid);

		// TODO: Change response
		log.auditLog("Transcription Text" + transcription.getTranscriptionText());

	}
}
