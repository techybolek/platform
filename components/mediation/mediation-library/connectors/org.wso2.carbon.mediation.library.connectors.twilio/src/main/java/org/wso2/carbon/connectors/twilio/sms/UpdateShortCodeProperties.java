package org.wso2.carbon.connectors.twilio.sms;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.ShortCode;

/*
 * Class mediator for updating the properties of a short code.
 * For more information, see http://www.twilio.com/docs/api/rest/short-codes
 */
public class UpdateShortCodeProperties extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// optional parameters for updating the short code
		// For more details, see
		// http://www.twilio.com/docs/api/rest/short-codes#instance-post-optional-parameters
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");
		String shortCodeSid = (String) messageContext.getProperty("TwilioShortCodeSid");

		String friendlyName = (String) messageContext.getProperty("TwilioFriendlyName");
		String apiVersion = (String) messageContext.getProperty("TwilioApiVersion");
		String smsUrl = (String) messageContext.getProperty("TwilioSmsUrl");
		String smsMethod = (String) messageContext.getProperty("TwilioSmsMethod");
		String smsFallBackMethod = (String) messageContext
				.getProperty("TwilioSmsFallBackUrl");
		String smsFallBackUrl = (String) messageContext
				.getProperty("TwilioSmsFallBackMethod");

		// optional parameters passed through this map
		Map<String, String> params = new HashMap<String, String>();

		if (friendlyName != null) {
			params.put("FriendlyName", friendlyName);
		}
		if (apiVersion != null) {
			params.put("ApiVersion", apiVersion);
		}
		if (smsUrl != null) {
			params.put("SmsUrl", smsUrl);
		}
		if (smsMethod != null) {
			params.put("SmsMethod", smsMethod);
		}
		if (smsFallBackUrl != null) {
			params.put("SmsFallBackUrl", smsFallBackUrl);
		}
		if (smsFallBackMethod != null) {
			params.put("SmsFallBackMethod", smsFallBackMethod);
		}

		try {
			updateShortCode(accountSid, authToken, shortCodeSid, log, params);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void updateShortCode(String accountSid, String authToken,
			String shortCodeSid, SynapseLog log, Map<String, String> params)
			throws TwilioRestException, IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		ShortCode shortCode = twilioRestClient.getAccount().getShortCode(shortCodeSid);
		shortCode.update(params);

		// TODO: change response
		log.auditLog("Short code " + shortCodeSid + " updated.");
	}

}
