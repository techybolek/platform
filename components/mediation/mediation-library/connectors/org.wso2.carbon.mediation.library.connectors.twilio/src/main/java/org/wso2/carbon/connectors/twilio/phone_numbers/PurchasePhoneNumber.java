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
import com.twilio.sdk.resource.factory.IncomingPhoneNumberFactory;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;

/*
 * Class mediator for purchasing a phone numbers.
 * For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers#list-post
 */
public class PurchasePhoneNumber extends AbstractTwilioConnector {

	
	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// getParameters(messageContext);

		Map<String, String> params = createParameterMap(messageContext);

		try {
			purchasePhoneNumber(accountSid, authToken, log, params);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void purchasePhoneNumber(String accountSid, String authToken, SynapseLog log,
			Map<String, String> params) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		IncomingPhoneNumberFactory numberFactory = twilioRestClient.getAccount()
				.getIncomingPhoneNumberFactory();

		IncomingPhoneNumber number = numberFactory.create(params);

		// TODO: change response
		log.auditLog("Successfully purchased the number.Friendly Name:"
				+ number.getFriendlyName() + "    Phone Number: "
				+ number.getPhoneNumber() + "     Sid: " + number.getSid());

	}

	/**
	 * Create a map containing the parameters required to purchase the number,
	 * which has been defined
	 * 
	 * @return The map containing the defined parameters
	 */
	private Map<String, String> createParameterMap(MessageContext messageContext) {
		String phoneNumber = (String) messageContext.getProperty("TwilioPhoneNumber");
		String areaCode = (String) messageContext
				.getProperty("TwilioPhoneNumberAreaCode");

		// Optional Parameters
		String friendlyName = (String) messageContext
				.getProperty("TwilioPhoneNumberFriendlyName");
		String voiceUrl = (String) messageContext
				.getProperty("TwilioPhoneNumberVoiceUrl");
		String voiceMethod = (String) messageContext
				.getProperty("TwilioPhoneNumberVoiceMethod");
		String voiceFallbackUrl = (String) messageContext
				.getProperty("TwilioPhoneNumberVoiceFallbackUrl");
		String voiceFallbackMethod = (String) messageContext
				.getProperty("TwilioPhoneNumberVoiceFallbackMethod");
		String statusCallback = (String) messageContext
				.getProperty("TwilioPhoneNumberStatusCallback");
		String statusCallbackMethod = (String) messageContext
				.getProperty("TwilioPhoneNumberStatusCallbackMethod");
		String voiceCallerIdLookup = (String) messageContext
				.getProperty("TwilioPhoneNumberVoiceCallerIdLookup");
		String voiceApplicationSid = (String) messageContext
				.getProperty("TwilioPhoneNumberVoiceCallerIdLookup");
		String smsUrl = (String) messageContext.getProperty("TwilioPhoneNumberSmsUrl");
		String smsMethod = (String) messageContext
				.getProperty("TwilioPhoneNumberSmsMethod");
		String smsFallbackUrl = (String) messageContext
				.getProperty("TwilioPhoneNumberSmsFallbackUrl");
		String smsFallbackMethod = (String) messageContext
				.getProperty("TwilioPhoneNumberSmsFallbackMethod");
		String smsApplicationSid = (String) messageContext
				.getProperty("TwilioPhoneNumberSmsStatusCallback");
		String apiVersion = (String) messageContext
				.getProperty("TwilioPhoneNumberApiVersion");

		Map<String, String> params = new HashMap<String, String>();

		// Exactly one of PhoneNumber or AreaCode must be provided
		if (phoneNumber != null) {
			params.put("PhoneNumber", phoneNumber);
		} else {
			params.put("AreaCode", areaCode);
		}

		if (friendlyName != null) {
			params.put("FriendlyName", friendlyName);
		}
		if (voiceUrl != null) {
			params.put("VoiceUrl", voiceUrl);
		}
		if (voiceMethod != null) {
			params.put("VoiceMethod", voiceMethod);
		}
		if (voiceFallbackUrl != null) {
			params.put("VoiceFallbackUrl", voiceFallbackUrl);
		}
		if (voiceFallbackMethod != null) {
			params.put("VoiceFallbackMethod", voiceFallbackMethod);
		}
		if (statusCallback != null) {
			params.put("StatusCallback", statusCallback);
		}
		if (statusCallbackMethod != null) {
			params.put("StatusCallbackMethod", statusCallbackMethod);
		}
		if (voiceCallerIdLookup != null) {
			params.put("VoiceCallerIdLookup", voiceCallerIdLookup);
		}
		if (voiceApplicationSid != null) {
			params.put("VoiceApplicationSid", voiceApplicationSid);
		}
		if (smsUrl != null) {
			params.put("SmsUrl", smsUrl);
		}
		if (smsMethod != null) {
			params.put("SmsMethod", smsMethod);
		}
		if (smsFallbackUrl != null) {
			params.put("SmsFallbackUrl", smsFallbackUrl);
		}
		if (smsFallbackMethod != null) {
			params.put("SmsFallbackMethod", smsFallbackMethod);
		}
		if (smsApplicationSid != null) {
			params.put("SmsApplicationSid", smsApplicationSid);
		}
		if (apiVersion != null) {
			params.put("ApiVersion", apiVersion);
		}

		return params;
	}

}
