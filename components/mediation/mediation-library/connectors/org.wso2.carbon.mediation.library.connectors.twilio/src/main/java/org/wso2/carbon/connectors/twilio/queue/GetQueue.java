package org.wso2.carbon.connectors.twilio.queue;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Queue;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
/*
* Class mediator for getting a queue instance based on the QueueSid parameter
* For more information, see http://www.twilio.com/docs/api/rest/queue
*/
public class GetQueue extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String queueSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        queueSid = (String) messageContext.getProperty("TwilioQueueSid");

        try {
            getQueue(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getQueue(SynapseLog log) throws IllegalArgumentException, TwilioRestException{
        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        Queue queue =twilioRestClient.getAccount().getQueue(queueSid);

        //TODO: change response
        log.auditLog(queue.toString());
    }

}
