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
/*
 * Class mediator for exchanging a phone number between accounts.
 * For more information, http://www.twilio.com/docs/api/rest/subaccounts#exchanging-numbers
 */
public class ExchangePhoneNumber extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// Must be provided
		String phoneNumberSid = (String) messageContext
				.getProperty("TwilioPhoneNumberSid");
		String transferAccountSid = (String) messageContext
				.getProperty("TwilioExchangePhoneNumberToAccountSid");

		Map<String, String> params = new HashMap<String, String>();
		params.put("AccountSid", transferAccountSid);

		try {
			exchangeNumber(accountSid, authToken, phoneNumberSid, log, params);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void exchangeNumber(String accountSid, String authToken,
			String phoneNumberSid, SynapseLog log, Map<String, String> params)
			throws TwilioRestException, IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get the phone number that needs to be exchanged.
		IncomingPhoneNumber number = twilioRestClient.getAccount()
				.getIncomingPhoneNumber(phoneNumberSid);

		// update the retrieved phone number to reflect the changes made
		number.update(params);
		// TODO: change response
		log.auditLog("Number exchanged successfully.");

	}
}
