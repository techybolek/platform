package org.wso2.carbon.connectors.twilio.queue;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Member;
/*
 * Class mediator for getting a member based on its callSid parameter
 * For more information, see http://www.twilio.com/docs/api/rest/member
 */
public class GetMember extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		String queueSid = (String) messageContext.getProperty("TwilioQueueSid");
		String callSid = (String) messageContext.getProperty("TwilioQueuedCallSid");

		try {
			getMember(accountSid, authToken, queueSid, callSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getMember(String accountSid, String authToken, String queueSid,
			String callSid, SynapseLog log) throws IllegalArgumentException,
			TwilioRestException {
		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		Member member = twilioRestClient.getAccount().getQueue(queueSid)
				.getMember(callSid);

		// TODO: change response
		log.auditLog(callSid + " wait time: " + member.getWaitTime());
	}

}
