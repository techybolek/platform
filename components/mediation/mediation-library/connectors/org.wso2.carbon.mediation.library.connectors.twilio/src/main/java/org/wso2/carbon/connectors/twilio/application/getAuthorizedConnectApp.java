package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.AuthorizedConnectApp;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting an authorized connect app instance based on the ApplicationSid parameter
* For more information, see http://www.twilio.com/docs/api/rest/authorized-connect-apps
*/
public class getAuthorizedConnectApp extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String authorizedConnectAppSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        authorizedConnectAppSid = (String) messageContext.getProperty("TwilioAuthorizedConnectAppSid");

        try {
            getAuthorizedConnectApp(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getAuthorizedConnectApp(SynapseLog log) throws
            IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //retrieve the matching Authorized Connect App
        AuthorizedConnectApp authorizedApp =
                twilioRestClient.getAccount().getAuthorizedConnectApp(authorizedConnectAppSid);

        //TODO: change response
        log.auditLog(authorizedApp.getFriendlyName());
    }

}
