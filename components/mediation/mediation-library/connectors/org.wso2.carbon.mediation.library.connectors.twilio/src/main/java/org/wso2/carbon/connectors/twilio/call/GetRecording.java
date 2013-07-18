package org.wso2.carbon.connectors.twilio.call;

import java.io.InputStream;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Recording;

/*
 * Class mediator for getting a recording instance based on its Sid.
 * For more information, see http://www.twilio.com/docs/api/rest/recording
 */
public class GetRecording extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// The Sid of the recording, must be provided
		String recordingSid = (String) messageContext.getProperty("TwilioRecordingSid");
		String recordingReturnType = (String) messageContext
				.getProperty("TwilioRecordingReturnType");

		try {
			if (recordingReturnType != null) {
				getRecording(accountSid, authToken, recordingSid, log,
						recordingReturnType);
			} else {
				getRecording(accountSid, authToken, recordingSid, log);
			}
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getRecording(String accountSid, String authToken, String recordingSid,
			SynapseLog log, String recordingReturnType) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get an recording object from its sid.
		Recording recording = twilioRestClient.getAccount().getRecording(recordingSid);

		InputStream inputStream = recording.getMedia(recordingReturnType);

		// TODO: change response
		log.auditLog("Call Sid: " + recording.getCallSid() + "  Duration: "
				+ recording.getDuration());

	}

	private void getRecording(String accountSid, String authToken, String recordingSid,
			SynapseLog log) throws IllegalArgumentException, TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get an recording object from its sid.
		Recording recording = twilioRestClient.getAccount().getRecording(recordingSid);

		// TODO: change response
		log.auditLog("Call Sid: " + recording.getCallSid() + "  Duration: "
				+ recording.getDuration());

	}
}
