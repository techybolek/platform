package org.wso2.carbon.connectors.twilio.queue;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Member;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
 * Class mediator for dequeuing a member from a queue instance.
 * For more information, see http://www.twilio.com/docs/api/rest/member
 */

public class DequeueMember extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String queueSid;
    private String callSid;
    private String url;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        queueSid = (String) messageContext.getProperty("TwilioQueueSid");
        callSid = (String) messageContext.getProperty("TwilioQueuedCallSid");
        url = (String) messageContext.getProperty("TwilioQueuedCallUrl");

        Map<String, String> params = new HashMap<String, String>();
        params.put("Url", url);
        params.put("Method", "POST");

        try {
            dequeueMember(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void dequeueMember(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        Member member = twilioRestClient.getAccount().getQueue(queueSid).getMember(callSid);
        member.update(params);

        //TODO: change response
        log.auditLog(callSid+" wait time: "+member.getWaitTime());
    }

}
