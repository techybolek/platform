package org.wso2.carbon.connectors.twilio.queue;

import com.twilio.sdk.TwilioRestClient;

import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Queue;
import com.twilio.sdk.resource.list.QueueList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
/*
* Class mediator for getting a list of queues belonging to the specified account.
* For more information, see http://www.twilio.com/docs/api/rest/queue
*/

public class GetQueueList extends AbstractMediator {

    private String accountSid;
    private String authToken;


    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        try {
            getQueueList(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getQueueList(SynapseLog log) throws IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        QueueList queueList = twilioRestClient.getAccount().getQueues();

        //TODO: change response
        for(Queue queue:queueList){
            log.auditLog(queue.toString());
        }
    }

}
