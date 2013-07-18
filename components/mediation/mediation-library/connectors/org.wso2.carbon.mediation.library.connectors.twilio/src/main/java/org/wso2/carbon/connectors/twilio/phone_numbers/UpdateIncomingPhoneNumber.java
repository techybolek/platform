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
 * Class mediator for getting the list of incoming phone numbers.
 * For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers#instance-post
 */
public class UpdateIncomingPhoneNumber extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");
		// Must be provided
		String incomingPhoneNumberSid = (String) messageContext
				.getProperty("TwilioIncomingPhoneNumberSid");
		// //Optional parameters
		// //See
		// http://www.twilio.com/docs/api/rest/incoming-phone-numbers#instance-post-optional-parameters

		Map<String, String> params = getParameters(messageContext);

		try {
			updateIncomingPhoneNumber(accountSid, authToken, incomingPhoneNumberSid, log,
					params);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void updateIncomingPhoneNumber(String accountSid, String authToken,
			String incomingPhoneNumberSid, SynapseLog log, Map<String, String> params)
			throws TwilioRestException, IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get an incoming phone number allocated to the Twilio account by its
		// Sid.
		IncomingPhoneNumber number = twilioRestClient.getAccount()
				.getIncomingPhoneNumber(incomingPhoneNumberSid);

		number.update(params);

		// TODO: change response
		log.auditLog("Number Updated.");
	}

	/**
	 * Populates the parameters from the properties from the message context (If
	 * provided)
	 * 
	 * @param messageContext
	 *            SynapseMessageContext
	 */
	private Map<String, String> getParameters(MessageContext messageContext) {

		// Parameters to be updated
		String friendlyName = (String) messageContext
				.getProperty("TwilioIPNFriendlyName");
		String apiVersion = (String) messageContext.getProperty("TwilioIPNApiVersion");
		String voiceUrl = (String) messageContext.getProperty("TwilioIPNVoiceUrl");
		String voiceMethod = (String) messageContext.getProperty("TwilioIPNVoiceMethod");
		String voiceFallbackUrl = (String) messageContext
				.getProperty("TwilioIPNVoiceFallbackUrl");
		String voiceFallbackMethod = (String) messageContext
				.getProperty("TwilioIPNVoiceFallbackMethod");
		String statusCallback = (String) messageContext
				.getProperty("TwilioIPNStatusCallback");
		String statusCallbackMethod = (String) messageContext
				.getProperty("TwilioIPNStatusCallbackMethod");
		String voiceCallerIdLookup = (String) messageContext
				.getProperty("TwilioIPNVoiceCallerIdLookup");
		String voiceApplicationSid = (String) messageContext
				.getProperty("TwilioIPNVoiceCallerIdLookup");
		String smsUrl = (String) messageContext.getProperty("TwilioIPNSmsUrl");
		String smsMethod = (String) messageContext.getProperty("TwilioIPNSmsMethod");
		String smsFallbackUrl = (String) messageContext
				.getProperty("TwilioIPNSmsFallbackUrl");
		String smsFallbackMethod = (String) messageContext
				.getProperty("TwilioIPNSmsFallbackMethod");
		String smsApplicationSid = (String) messageContext
				.getProperty("TwilioIPNSmsStatusCallback");
		String transferToAccountSid = (String) messageContext
				.getProperty("TwilioIPNSmsTransferToAccountSid");

		Map<String, String> params = new HashMap<String, String>();

		if (friendlyName != null) {
			params.put("FriendlyName", friendlyName);
		}

		if (apiVersion != null) {
			params.put("ApiVersion", apiVersion);
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
		if (transferToAccountSid != null) {
			params.put("AccountSid", transferToAccountSid);
		}
		return params;
	}
}
