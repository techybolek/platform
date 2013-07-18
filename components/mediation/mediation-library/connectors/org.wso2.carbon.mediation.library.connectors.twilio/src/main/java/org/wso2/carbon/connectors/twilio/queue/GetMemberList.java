package org.wso2.carbon.connectors.twilio.queue;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Member;
import com.twilio.sdk.resource.list.MemberList;
/*
 * Class mediator for getting a list of member instances contained in a queue.
 * For more information, see http://www.twilio.com/docs/api/rest/queue
 */
public class GetMemberList extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");
		String queueSid = (String) messageContext.getProperty("TwilioQueueSid");

		try {
			getMemberList(accountSid, authToken, queueSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getMemberList(String accountSid, String authToken, String queueSid,
			SynapseLog log) throws TwilioRestException, IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		MemberList memberList = twilioRestClient.getAccount().getQueue(queueSid)
				.getMembers();

		// TODO: change response
		for (Member member : memberList) {
			log.auditLog(member.getCallSid() + " : " + member.getPosition());
		}
	}

}
