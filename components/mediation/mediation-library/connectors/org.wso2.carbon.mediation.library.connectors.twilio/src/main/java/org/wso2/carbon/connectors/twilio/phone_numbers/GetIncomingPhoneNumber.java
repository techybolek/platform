package org.wso2.carbon.connectors.twilio.phone_numbers;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;

/*
 * Class mediator for getting an incoming phone number.
 * For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers
 */
public class GetIncomingPhoneNumber extends AbstractTwilioConnector {

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
			getIncomingPhoneNumber(accountSid, authToken, incomingPhoneNumberSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getIncomingPhoneNumber(String accountSid, String authToken,
			String incomingPhoneNumberSid, SynapseLog log)
			throws IllegalArgumentException, TwilioRestException {
		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get an incoming phone number allocated to the Twilio account by its
		// Sid.
		IncomingPhoneNumber number = twilioRestClient.getAccount()
				.getIncomingPhoneNumber(incomingPhoneNumberSid);

		// TODO: change response
		log.auditLog("Friendly Name:" + number.getFriendlyName() + "    Phone Number: "
				+ number.getPhoneNumber());
	}
}
