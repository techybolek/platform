package org.wso2.carbon.connectors.twilio.phone_numbers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.AvailablePhoneNumber;
import com.twilio.sdk.resource.list.AvailablePhoneNumberList;
/*
 * Class mediator for getting available toll-free numbers.
 * For more information, see http://www.twilio.com/docs/api/rest/available-phone-numbers#toll-free
 */
public class GetAvailableTollFreeNumbers extends AbstractTwilioConnector {

	// filter parameters
	// See
	// http://www.twilio.com/docs/api/rest/available-phone-numbers#toll-free-get-filters

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// Must be provided
		String country = (String) messageContext.getProperty("TwilioCountry");

		// BasicFilters
		String areaCode = (String) messageContext
				.getProperty("TwilioPhoneNumberAreaCode");
		String contains = (String) messageContext
				.getProperty("TwilioPhoneNumberContains");

		Map<String, String> filter = new HashMap<String, String>();

		if (areaCode != null) {
			filter.put("AreaCode", areaCode);
		}
		if (contains != null) {
			filter.put("Contains", contains);
		}

		try {
			getAvailableTollFreeNumbers(accountSid, authToken, country, log, filter);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getAvailableTollFreeNumbers(String accountSid, String authToken,
			String country, SynapseLog log, Map<String, String> filter)
			throws IllegalArgumentException, TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		AvailablePhoneNumberList numbers = twilioRestClient.getAccount()
				.getAvailablePhoneNumbers(filter, country, "TollFree");

		// Note: This is the list containing the list of numbers.
		// If there are no numbers matching the filter, the list will be empty
		List<AvailablePhoneNumber> list = numbers.getPageData();

		// TODO: Change response
		if (list.isEmpty()) {
			log.auditLog("No numbers matching the filter");
		}

		for (AvailablePhoneNumber number : list) {
			log.auditLog("Friendly Name:" + number.getFriendlyName()
					+ "    Phone Number: " + number.getPhoneNumber());
		}
	}
}
