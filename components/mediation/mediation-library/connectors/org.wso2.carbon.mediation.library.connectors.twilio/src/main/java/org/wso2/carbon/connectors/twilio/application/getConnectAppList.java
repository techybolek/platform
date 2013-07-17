package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.ConnectApp;
import com.twilio.sdk.resource.list.ConnectAppList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting a list of connect app instances
* For more information, see http://www.twilio.com/docs/api/rest/connect-apps
*/
public class getConnectAppList extends AbstractMediator {

    private String accountSid;
    private String authToken;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        try {
            getConnectAppList(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getConnectAppList(SynapseLog log) throws IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get a list of matching Connect Apps pertaining to the AccountSID
        ConnectAppList connectApps = twilioRestClient.getAccount().getConnectApps();

        //TODO: change response
        for (ConnectApp connectApp : connectApps) {
            log.auditLog("Connect App: "+connectApp.getFriendlyName());
        }
    }

}
