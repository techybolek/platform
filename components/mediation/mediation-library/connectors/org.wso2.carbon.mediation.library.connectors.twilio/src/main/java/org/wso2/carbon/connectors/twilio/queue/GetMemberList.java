package org.wso2.carbon.connectors.twilio.queue;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Member;
import com.twilio.sdk.resource.list.MemberList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
/*
* Class mediator for getting a list of member instances contained in a queue.
* For more information, see http://www.twilio.com/docs/api/rest/queue
*/
public class GetMemberList extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String queueSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");
        queueSid = (String) messageContext.getProperty("TwilioQueueSid");

        try {
            getMemberList(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getMemberList(SynapseLog log) throws TwilioRestException,IllegalArgumentException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        MemberList memberList = twilioRestClient.getAccount().getQueue(queueSid).getMembers();

        //TODO: change response
        for(Member member:memberList){
            log.auditLog(member.getCallSid()+" : "+member.getPosition());
        }
    }

}
