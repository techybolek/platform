package org.wso2.carbon.connectors.twilio.queue;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Queue;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;
/*
* Class mediator for updating a queue instance
* For more information, see http://www.twilio.com/docs/api/rest/queue
*/
public class UpdateQueue extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String queueSid;

    //optional parameters
    private String friendlyName;
    private String maxSize;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        queueSid = (String) messageContext.getProperty("TwilioQueueSid");

        friendlyName = (String) messageContext.getProperty("TwilioQueueFriendlyName");
        maxSize = (String) messageContext.getProperty("TwilioQueueMaxSize");

        Map<String, String> params = new HashMap<String, String>();

        if(friendlyName != null){
            params.put("FriendlyName", friendlyName);
        }

        if(maxSize != null){
            params.put("MaxSize", maxSize);
        }


        try {
            updateQueue(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void updateQueue(SynapseLog log, Map<String, String> params) throws
            TwilioRestException ,IllegalArgumentException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        Queue queue =twilioRestClient.getAccount().getQueue(queueSid);
        queue.update(params);

        //TODO: change response
        log.auditLog("Queue editing successful.");
    }

}
