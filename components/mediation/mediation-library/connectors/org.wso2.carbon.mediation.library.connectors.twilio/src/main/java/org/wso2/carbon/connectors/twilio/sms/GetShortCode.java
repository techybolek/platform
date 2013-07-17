package org.wso2.carbon.connectors.twilio.sms;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.ShortCode;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting the short code based on the Sid.
* For more information, see http://www.twilio.com/docs/api/rest/short-codes
*/
public class GetShortCode extends AbstractMediator {

    //mandatory parameters
    private String accountSid;
    private String authToken;

    //the short code Sid used to uniquely identify the short code
    private String shortCodeSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");
        shortCodeSid = (String) messageContext.getProperty("TwilioShortCodeSid");

        try {
            getShortCode(log);
        } catch (Exception e) {
            //TODO: handle exception
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getShortCode(SynapseLog log) throws IllegalArgumentException, TwilioRestException {
        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        ShortCode shortCode = twilioRestClient.getAccount().getShortCode(shortCodeSid);

        //TODO: change response
        log.auditLog("Message: " + shortCode.getShortCode());
    }

}
