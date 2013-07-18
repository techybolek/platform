package org.wso2.carbon.connectors.twilio.queue;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Member;

/*
 * Class mediator for dequeuing a member from a queue instance.
 * For more information, see http://www.twilio.com/docs/api/rest/member
 */

public class DequeueMember extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		String queueSid = (String) messageContext.getProperty("TwilioQueueSid");
		String callSid = (String) messageContext.getProperty("TwilioQueuedCallSid");
		String url = (String) messageContext.getProperty("TwilioQueuedCallUrl");

		Map<String, String> params = new HashMap<String, String>();
		params.put("Url", url);
		params.put("Method", "POST");

		try {
			dequeueMember(accountSid, authToken, queueSid, callSid, log, params);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void dequeueMember(String accountSid, String authToken, String queueSid,
			String callSid, SynapseLog log, Map<String, String> params)
			throws TwilioRestException, IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		Member member = twilioRestClient.getAccount().getQueue(queueSid)
				.getMember(callSid);
		member.update(params);

		// TODO: change response
		log.auditLog(callSid + " wait time: " + member.getWaitTime());
	}

}
