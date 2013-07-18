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
import com.twilio.sdk.resource.factory.QueueFactory;
import com.twilio.sdk.resource.instance.Queue;
/*
 * Class mediator for creating a new queue instance.
 * For more information, see http://www.twilio.com/docs/api/rest/queue
 */
public class CreateQueue extends AbstractTwilioConnector {



	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// //optional parameters
		// //See
		// http://www.twilio.com/docs/api/rest/queue#list-post-parameters-optional
		String friendlyName = (String) messageContext
				.getProperty("TwilioQueueFriendlyName");
		String maxSize = (String) messageContext.getProperty("TwilioQueueMaxSize");

		// Map for holding optional parameters
		Map<String, String> params = new HashMap<String, String>();

		if (friendlyName != null) {
			params.put("FriendlyName", friendlyName);
		}

		if (maxSize != null) {
			params.put("MaxSize", maxSize);
		}

		try {
			createQueue(accountSid, authToken, log, params);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void createQueue(String accountSid, String authToken, SynapseLog log,
			Map<String, String> params) throws TwilioRestException,
			IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		QueueFactory queueFactory = twilioRestClient.getAccount().getQueueFactory();

		Queue queue = queueFactory.create(params);

		// TODO: change response
		log.auditLog("Queue" + queue.getSid() + " creation successful.");
	}

}
