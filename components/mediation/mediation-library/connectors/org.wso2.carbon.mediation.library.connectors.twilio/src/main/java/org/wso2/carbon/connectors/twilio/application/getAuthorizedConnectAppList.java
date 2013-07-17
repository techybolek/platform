package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.AuthorizedConnectApp;
import com.twilio.sdk.resource.list.AuthorizedConnectAppList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting a list of authorized connect app instances
* For more information, see http://www.twilio.com/docs/api/rest/authorized-connect-apps
*/
public class getAuthorizedConnectAppList extends AbstractMediator {

    private String accountSid;
    private String authToken;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        try {
            getAuthorizedConnectAppList(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getAuthorizedConnectAppList(SynapseLog log) throws
            IllegalArgumentException, TwilioRestException{
        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //retrieve the list of Authorized Connect Apps pertaining to the account
        AuthorizedConnectAppList authorizedApps = twilioRestClient.getAccount().getAuthorizedConnectApps();

        //TODO: change response
        for (AuthorizedConnectApp authorizedApp : authorizedApps) {
            log.auditLog("Connect App: " + authorizedApp.getFriendlyName());
        }
    }

}
