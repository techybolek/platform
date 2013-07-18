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
import com.twilio.sdk.resource.instance.Call;
/*
 * Class mediator for modifying a live call.
 * For more information, see http://www.twilio.com/docs/api/rest/change-call-state
 */
public class ModifyLiveCall extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// Must be provided
		String callSid = (String) messageContext.getProperty("TwilioCallSid");

		// Optional parameters. For specifications and formats, see
		// http://www.twilio.com/docs/api/rest/change-call-state
		// Available parameters to be modified (Optional)
		String url = (String) messageContext.getProperty("TwilioCallUrl");
		String status = (String) messageContext.getProperty("TwilioCallStatus");
		String method = (String) messageContext.getProperty("TwilioCallMethod");

		// Map for optional parameters
		Map<String, String> params = new HashMap<String, String>();

		if (url != null) {
			params.put("Url", url);
		}
		if (status != null) {
			params.put("Status", status);
		}
		if (method != null) {
			params.put("Method", method);
		}

		try {
			modifyLiveCall(accountSid, authToken, callSid, log, params);
		} catch (Exception e) {
			log.auditError(e);
			throw new SynapseException(e);
		}

	}

	private void modifyLiveCall(String accountSid, String authToken, String callSid,
			SynapseLog log, Map<String, String> params) throws TwilioRestException,
			IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get the call to be modified
		Call call = twilioRestClient.getAccount().getCall(callSid);
		// update the matching live call with the specified parameters
		call.update(params);

		// TODO: chane response
		log.auditLog("Call " + callSid + " Modified.");
	}
}
