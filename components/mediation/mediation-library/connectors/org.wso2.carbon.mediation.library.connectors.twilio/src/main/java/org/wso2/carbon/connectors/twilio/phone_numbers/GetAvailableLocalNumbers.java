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
 * Class mediator for getting available local numbers in an account.
 * For more information, http://www.twilio.com/docs/api/rest/available-phone-numbers
 */
public class GetAvailableLocalNumbers extends AbstractTwilioConnector {

	// Basic filter parameters
	// See
	// http://www.twilio.com/docs/api/rest/available-phone-numbers#local-get-basic-filters

	// Advance filter parameters (only for numbers in the Unites States and
	// Canada).
	// See
	// https://www.twilio.com/docs/api/rest/available-phone-numbers#local-get-advanced-filters

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");
		// Must be provided
		String country = (String) messageContext.getProperty("TwilioCountry");
		// getParameters(messageContext);

		Map<String, String> filter = createParameterMap(messageContext);

		try {
			getAvailableLocalNumbers(accountSid, authToken, country, log, filter);
		} catch (TwilioRestException e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getAvailableLocalNumbers(String accountSid, String authToken,
			String country, SynapseLog log, Map<String, String> filter)
			throws IllegalArgumentException, TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		AvailablePhoneNumberList numbers = twilioRestClient.getAccount()
				.getAvailablePhoneNumbers(filter, country, "Local");

		// Note: This is the list containing the list of numbers.
		// If there are no numbers matching the filter, the list will be empty
		List<AvailablePhoneNumber> list = numbers.getPageData();

		// TODO: change response
		if (list.isEmpty()) {
			log.auditLog("No numbers matching the filter");
		}

		for (AvailablePhoneNumber number : list) {
			log.auditLog("Friendly Name:" + number.getFriendlyName()
					+ "    Phone Number: " + number.getPhoneNumber());
		}
	}

	/**
	 * Create a map containing the parameters required to filter the list of
	 * numbers, which has been defined
	 * 
	 * @return The map containing the defined parameters
	 */
	private Map<String, String> createParameterMap(MessageContext messageContext) {

		// BasicFilters
		String areaCode = (String) messageContext
				.getProperty("TwilioPhoneNumberAreaCode");
		String contains = (String) messageContext
				.getProperty("TwilioPhoneNumberContains");
		String inRegion = (String) messageContext
				.getProperty("TwilioPhoneNumberInRegion");
		String inPostalCode = (String) messageContext
				.getProperty("TwilioPhoneNumberInPostalCode");

		// Advance Filters
		String nearLatLong = (String) messageContext
				.getProperty("TwilioPhoneNumberNearLatLong");
		String nearNumber = (String) messageContext
				.getProperty("TwilioPhoneNumberNearNumber");
		String inLata = (String) messageContext.getProperty("TwilioPhoneNumberInLata");
		String inRateCenter = (String) messageContext
				.getProperty("TwilioPhoneNumberInRateCenter");
		String distance = (String) messageContext
				.getProperty("TwilioPhoneNumberDistance");

		Map<String, String> filter = new HashMap<String, String>();

		// Basic
		if (areaCode != null) {
			filter.put("AreaCode", areaCode);
		}
		if (contains != null) {
			filter.put("Contains", contains);
		}
		if (inRegion != null) {
			filter.put("InRegion", inRegion);
		}
		if (inPostalCode != null) {
			filter.put("InRateCenter", inPostalCode);
		}

		// Advance
		if (nearLatLong != null) {
			filter.put("NearLatLong", nearLatLong);
		}
		if (nearNumber != null) {
			filter.put("NearNumber", nearNumber);
		}
		if (inLata != null) {
			filter.put("InLata", inLata);
		}
		if (inRateCenter != null) {
			filter.put("InRateCenter", inRateCenter);
		}
		if (distance != null) {
			filter.put("Distance", distance);
		}
		return filter;
	}

}
