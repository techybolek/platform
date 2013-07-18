package org.wso2.carbon.connectors.twilio.call;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Transcription;
import com.twilio.sdk.resource.list.TranscriptionList;
/*
 * Class mediator for getting a list of Call transcription instances.
 * For more information, see http://www.twilio.com/docs/api/rest/transcription
 */
public class GetTranscriptionList extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		try {
			getTranscriptionList(accountSid, authToken, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getTranscriptionList(String accountSid, String authToken, SynapseLog log)
			throws IllegalArgumentException, TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get the Transcription List
		TranscriptionList transcriptions = twilioRestClient.getAccount()
				.getTranscriptions();

		// Loop over transcriptions and print out a property
		for (Transcription transcription : transcriptions) {
			log.auditLog("Transcription Text" + transcription.getTranscriptionText());
		}

	}
}
