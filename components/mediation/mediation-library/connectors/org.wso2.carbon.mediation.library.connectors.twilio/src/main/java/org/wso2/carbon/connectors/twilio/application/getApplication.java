package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Application;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting an application instance based on the ApplicationSid parameter
* For more information, see http://www.twilio.com/docs/api/rest/applications
*/
public class getApplication extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String applicationSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        applicationSid = (String) messageContext.getProperty("TwilioApplicationSid");

        try {
            getApplication(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getApplication(SynapseLog log) throws IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //retrieve the matching application
        Application application =twilioRestClient.getAccount().getApplication(applicationSid);

        //TODO: change response
        log.auditLog("Application Friendly Name: " + application.getFriendlyName());
    }

}
