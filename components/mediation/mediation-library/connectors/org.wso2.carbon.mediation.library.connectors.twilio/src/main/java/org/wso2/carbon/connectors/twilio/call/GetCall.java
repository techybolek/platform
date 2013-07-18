package org.wso2.carbon.connectors.twilio.call;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Call;

/*
 * Class mediator for getting a Call instance based on its Sid.
 * For more information, see http://www.twilio.com/docs/api/rest/call
 */
public class GetCall extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// The Sid of the required Call, must be provided
		String callSid = (String) messageContext.getProperty("TwilioCallSid");

		try {
			getCall(accountSid, authToken, callSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getCall(String accountSid, String authToken, String callSid,
			SynapseLog log) throws IllegalArgumentException, TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get an call object from its sid.
		Call call = twilioRestClient.getAccount().getCall(callSid);

		// TODO: change response
		log.auditLog("Called To: " + call.getTo() + "   From: " + call.getFrom());

	}
}
