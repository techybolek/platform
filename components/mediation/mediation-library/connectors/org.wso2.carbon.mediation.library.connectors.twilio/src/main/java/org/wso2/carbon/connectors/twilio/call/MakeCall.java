package org.wso2.carbon.connectors.twilio.call;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Call;
/*
 * Class mediator for making a call.
 * For more information, see http://www.twilio.com/docs/api/rest/making-calls
 */
public class MakeCall extends AbstractTwilioConnector {
	// Parameter details. For specifications and formats, see
	// http://www.twilio.com/docs/api/rest/making-calls#post-parameters-required
	// and
	// http://www.twilio.com/docs/api/rest/making-calls#post-parameters-optional.

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		Map<String, String> callParams = createParameterMap(messageContext);

		try {
			makeCall(accountSid, authToken, log, callParams);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void makeCall(String accountSid, String authToken, SynapseLog log,
			Map<String, String> callParams) throws TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		CallFactory callFactory = twilioRestClient.getAccount().getCallFactory();

		Call call = callFactory.create(callParams);

		log.auditLog("Call Successful. Call Sid: " + call.getSid());
	}

	/**
	 * Create a map containing the parameters required to make the call, which
	 * has been defined
	 * 
	 * @return The map containing the defined parameters
	 */
	private Map<String, String> createParameterMap(MessageContext messageContext) {

		// These are compulsory
		String to = (String) messageContext.getProperty("TwilioCallTo");
		String from = (String) messageContext.getProperty("TwilioCallFrom");

		// One of the below
		String callUrl = (String) messageContext.getProperty("TwilioCallUrl");
		String applicationSid = (String) messageContext
				.getProperty("TwilioApplicationSid");

		// Optional parameters
		String method = (String) messageContext.getProperty("TwilioMethod");
		String fallbackUrl = (String) messageContext.getProperty("TwilioFallbackUrl");
		String fallbackMethod = (String) messageContext
				.getProperty("TwilioFallbackMethod");
		String statusCallback = (String) messageContext
				.getProperty("TwilioStatusCallback");
		String statusCallbackMethod = (String) messageContext
				.getProperty("TwilioStatusCallbackMethod");
		String sendDigits = (String) messageContext.getProperty("TwilioSendDigits");
		String ifMachine = (String) messageContext.getProperty("TwilioIfMachine");
		String timeout = (String) messageContext.getProperty("TwilioTimeout");
		String record = (String) messageContext.getProperty("TwilioRecord");

		Map<String, String> callParams = new HashMap<String, String>();

		callParams.put("To", to);
		callParams.put("From", from);

		// Only one of the below must be provided
		if (callUrl != null) {
			callParams.put("Url", callUrl);
		} else {
			callParams.put("ApplicationSid", applicationSid);
		}

		// These are optional parameters. Need to check whether the parameters
		// have been defined
		if (method != null) {
			callParams.put("Method", method);
		}
		if (fallbackUrl != null) {
			callParams.put("FallbackUrl", fallbackUrl);
		}
		if (fallbackMethod != null) {
			callParams.put("FallbackMethod", fallbackMethod);
		}
		if (statusCallback != null) {
			callParams.put("StatusCallback", statusCallback);
		}
		if (statusCallbackMethod != null) {
			callParams.put("StatusCallbackMethod", statusCallbackMethod);
		}
		if (sendDigits != null) {
			callParams.put("SendDigits", sendDigits);
		}
		if (ifMachine != null) {
			callParams.put("IfMachine", ifMachine);
		}
		if (timeout != null) {
			callParams.put("Timeout", timeout);
		}
		if (record != null) {
			callParams.put("Record", record);
		}

		return callParams;
	}

}
