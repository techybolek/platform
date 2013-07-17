package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.ConnectApp;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting a connect app instance based on the ConnectAppSid parameter
* For more information, see http://www.twilio.com/docs/api/rest/connect-apps
*/
public class getConnectApp extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String connectAppSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        connectAppSid = (String) messageContext.getProperty("TwilioConnectAppSid");

        try {
            getConnectApp(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getConnectApp(SynapseLog log) throws IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Retrieve the matching Connect App
        ConnectApp connectApp =twilioRestClient.getAccount().getConnectApp(connectAppSid);

        //TODO: change response
        log.auditLog(connectApp.getFriendlyName());
    }

}
