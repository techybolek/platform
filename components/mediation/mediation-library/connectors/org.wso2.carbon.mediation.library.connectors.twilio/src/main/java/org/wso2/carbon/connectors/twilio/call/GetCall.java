package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Call;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting a Call instance based on its Sid.
* For more information, see http://www.twilio.com/docs/api/rest/call
*/
public class GetCall extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;
    private String callSid;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //The Sid of the required Call, must be provided
        callSid = (String) messageContext.getProperty("TwilioCallSid");

        try {
            getCall(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getCall(SynapseLog log) throws IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        // Get an call object from its sid.
        Call call = twilioRestClient.getAccount().getCall(callSid);

        //TODO: change response
        log.auditLog("Called To: " + call.getTo() + "   From: " + call.getFrom());

    }
}
