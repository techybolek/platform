package org.wso2.carbon.connectors.twilio.phone_numbers;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import com.twilio.sdk.resource.list.IncomingPhoneNumberList;
/*
 * Class mediator for getting the list of incoming phone numbers.
 * For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers
 */
public class GetIncomingPhoneNumberList extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// Parameters for the filters
		String phoneNumber = (String) messageContext
				.getProperty("TwilioIncomingPhoneNumber");
		String friendlyName = (String) messageContext
				.getProperty("TwilioIncomingPhoneNumberFriendlyName");

		Map<String, String> filter = new HashMap<String, String>();

		if (phoneNumber != null) {
			filter.put("PhoneNumber", phoneNumber);
		}
		if (friendlyName != null) {
			filter.put("FriendlyName", friendlyName);
		}

		try {
			getIncomingPhoneNumberList(accountSid, authToken, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getIncomingPhoneNumberList(String accountSid, String authToken,
			SynapseLog log) throws IllegalArgumentException, TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		IncomingPhoneNumberList numbers = twilioRestClient.getAccount()
				.getIncomingPhoneNumbers();

		// TODO: change response
		// Loop over numbers and print out a property
		for (IncomingPhoneNumber number : numbers) {
			log.auditLog("Friendly Name:" + number.getFriendlyName()
					+ "    Phone Number: " + number.getPhoneNumber());
		}
	}
}
