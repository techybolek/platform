package org.wso2.carbon.connectors.twilio.sms;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Sms;

/*
 * Class mediator for getting a an SMS resource given its Sid.
 * For more information, see http://www.twilio.com/docs/api/rest/sms
 */
public class GetSms extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		String messageSid = (String) messageContext.getProperty("TwilioSMSMessageSid");

		try {
			getSms(accountSid, authToken, messageSid, log);
		} catch (Exception e) {
			// TODO: handle the exception
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getSms(String accountSid, String authToken, String messageSid,
			SynapseLog log) throws IllegalArgumentException, TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// retrieving the Sms object associated with the Sid
		Sms message = twilioRestClient.getAccount().getSms(messageSid);

		// TODO: change response
		log.auditLog("From: " + message.getFrom() + "   Message: " + message.getBody());
	}

}
