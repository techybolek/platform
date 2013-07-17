package org.wso2.carbon.connectors.twilio.sms;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Sms;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting a an SMS resource given its Sid.
* For more information, see http://www.twilio.com/docs/api/rest/sms
*/
public class GetSms extends AbstractMediator {
    //authentication parameters
    private String accountSid;
    private String authToken;

    //the unique identifier for messages
    private String messageSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        messageSid = (String) messageContext.getProperty("TwilioSMSMessageSid");

        try {
            getSms(log);
        } catch (Exception e) {
            //TODO: handle the exception
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getSms(SynapseLog log) throws IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //retrieving the Sms object associated with the Sid
        Sms message = twilioRestClient.getAccount().getSms(messageSid);

        //TODO: change response
        log.auditLog("From: " + message.getFrom() + "   Message: " + message.getBody());
    }

}
