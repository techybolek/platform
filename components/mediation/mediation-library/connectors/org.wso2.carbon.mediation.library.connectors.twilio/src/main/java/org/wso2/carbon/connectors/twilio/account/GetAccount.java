package org.wso2.carbon.connectors.twilio.account;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for retrieving an account or subaccount instance from its SID
* For more information, see http://www.twilio.com/docs/api/rest/account and
* http://www.twilio.com/docs/api/rest/subaccounts,
*/
public class GetAccount extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;
    private String subAccountSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        subAccountSid = (String) messageContext.getProperty("TwilioSubAccountSid");

        try {
            getAccount(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getAccount(SynapseLog log) throws TwilioRestException, IllegalArgumentException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        Account account;
        //If a SubAccount is required
        if (subAccountSid != null) {
            account = twilioRestClient.getAccount(subAccountSid);
        }
        else {
            account = twilioRestClient.getAccount(); //Returns the main account
        }

        //TODO: change response
        log.auditLog("Friendly Name: " + account.getFriendlyName());
    }

}
