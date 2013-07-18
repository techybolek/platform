package org.wso2.carbon.connectors.twilio.queue;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Queue;
/*
 * Class mediator for getting a queue instance based on the QueueSid parameter
 * For more information, see http://www.twilio.com/docs/api/rest/queue
 */
public class GetQueue extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		String queueSid = (String) messageContext.getProperty("TwilioQueueSid");

		try {
			getQueue(accountSid, authToken, queueSid, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		};
	}

	private void getQueue(String accountSid, String authToken, String queueSid,
			SynapseLog log) throws IllegalArgumentException, TwilioRestException {
		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		Queue queue = twilioRestClient.getAccount().getQueue(queueSid);

		// TODO: change response
		log.auditLog(queue.toString());
	}

}
