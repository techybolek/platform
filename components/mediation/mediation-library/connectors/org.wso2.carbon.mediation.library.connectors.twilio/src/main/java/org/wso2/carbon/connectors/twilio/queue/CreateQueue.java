package org.wso2.carbon.connectors.twilio.queue;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.QueueFactory;
import com.twilio.sdk.resource.instance.Queue;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;
/*
* Class mediator for creating a new queue instance.
* For more information, see http://www.twilio.com/docs/api/rest/queue
*/
public class CreateQueue extends AbstractMediator {

    private String accountSid;
    private String authToken;

    //optional parameters
    //See http://www.twilio.com/docs/api/rest/queue#list-post-parameters-optional
    //for the full specification.
    private String friendlyName;
    private String maxSize;


    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        friendlyName = (String) messageContext.getProperty("TwilioQueueFriendlyName");
        maxSize = (String) messageContext.getProperty("TwilioQueueMaxSize");

        //Map for holding optional parameters
        Map<String, String> params = new HashMap<String, String>();

        if(friendlyName != null){
            params.put("FriendlyName", friendlyName);
        }

        if(maxSize != null){
            params.put("MaxSize", maxSize);
        }

        try {
            createQueue(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void createQueue(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        QueueFactory queueFactory = twilioRestClient.getAccount().getQueueFactory();

        Queue queue = queueFactory.create(params);

        //TODO: change response
        log.auditLog("Queue"+queue.getSid()+" creation successful.");
    }

}
