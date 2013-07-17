package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for deleting an application from the account it's bound to.
* For more information, see http://www.twilio.com/docs/api/rest/applications
*/
public class RemoveApplication extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String applicationSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        applicationSid = (String) messageContext.getProperty("TwilioApplicationSid");

        try {
            removeApplication(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }


        return true;
    }

    private void removeApplication(SynapseLog log) throws TwilioRestException, IllegalArgumentException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        twilioRestClient.getAccount().getApplication(applicationSid).delete();

        //TODO: change response
        log.auditLog("Application " + applicationSid + " has been removed from the account.");

    }

}
