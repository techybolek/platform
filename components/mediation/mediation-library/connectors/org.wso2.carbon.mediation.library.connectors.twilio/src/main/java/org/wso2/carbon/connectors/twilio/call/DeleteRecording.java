package org.wso2.carbon.connectors.twilio.call;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Recording;

/*
 * Class mediator for deleting a recording instance based on its Sid.
 * For more information, see http://www.twilio.com/docs/api/rest/recording#instance-delete
 */
public class DeleteRecording extends AbstractTwilioConnector {

	// //Authorization details
	// private String accountSid;
	// private String authToken;
	// private String recordingSid;

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// The Sid of the recording, must be provided
		String recordingSid = (String) messageContext.getProperty("TwilioRecordingSid");

		try {
			deleteRecording(accountSid, authToken, recordingSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}
	}

	private void deleteRecording(String accountSid, String authToken,
			String recordingSid, SynapseLog log) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get an recording object from its sid.
		Recording recording = twilioRestClient.getAccount().getRecording(recordingSid);
		boolean deleted = recording.delete();

		// TODO: change response
		if (deleted) {
			log.auditLog("Recording deleted successfully");
		}

	}
}
