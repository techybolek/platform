package org.wso2.carbon.connectors.twilio.phone_numbers;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;

/*
 * Class mediator for removing an incoming a phone numbers.
 * For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers#instance-delete
 */
public class RemoveIncomingPhoneNumber extends AbstractTwilioConnector {

	// //Authorization details
	// private String accountSid;
	// private String authToken;
	//
	// //Sid of the required number
	// private String incomingPhoneNumberSid;

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// Must be provided
		String incomingPhoneNumberSid = (String) messageContext
				.getProperty("TwilioIncomingPhoneNumberSid");

		try {
			removeIncomingPhoneNumber(accountSid, authToken, incomingPhoneNumberSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}
	}

	private void removeIncomingPhoneNumber(String accountSid, String authToken,
			String incomingPhoneNumberSid, SynapseLog log) throws TwilioRestException,
			IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get an incoming phone number allocated to the Twilio account by its
		// Sid.
		IncomingPhoneNumber number = twilioRestClient.getAccount()
				.getIncomingPhoneNumber(incomingPhoneNumberSid);

		number.delete();

		// TODO: change response
		log.auditLog("Number Deleted.");
	}
}
