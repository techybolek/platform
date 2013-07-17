package org.wso2.carbon.connectors.twilio.queue;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Member;
import com.twilio.sdk.verbs.TwiMLException;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
/*
* Class mediator for getting a member based on its callSid parameter
* For more information, see http://www.twilio.com/docs/api/rest/member
*/
public class GetMember extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String queueSid;
    private String callSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        queueSid = (String) messageContext.getProperty("TwilioQueueSid");
        callSid = (String) messageContext.getProperty("TwilioQueuedCallSid");

        try {
            getMember(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getMember(SynapseLog log) throws IllegalArgumentException, TwilioRestException{
        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        Member member = twilioRestClient.getAccount().getQueue(queueSid).getMember(callSid);

        //TODO: change response
        log.auditLog(callSid+" wait time: "+member.getWaitTime());
    }

}
