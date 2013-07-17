package org.wso2.carbon.connectors.twilio.account;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.AccountFactory;
import com.twilio.sdk.resource.instance.Account;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for creating a sub-account
* For more information, see http://www.twilio.com/docs/api/rest/subaccounts
*/
public class CreateSubAccount extends AbstractMediator {

    //Authentication details
    private String accountSid;
    private String authToken;

    //optional parameter.
    // For more information, see http://www.twilio.com/docs/api/rest/subaccounts#creating-subaccounts-post-parameters-optional
    private String friendlyName;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");
        friendlyName = (String) messageContext.getProperty("TwilioAccountFriendlyName");

        // Build a filter for the AccountList
        Map<String, String> params = new HashMap<String, String>();

        //If a friendly name has been provided for the account;
        //if not will use the current date and time (Default by Twilio).
        if (friendlyName != null) {
            params.put("FriendlyName", friendlyName);
        }

        try {
            createSubAccount(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void createSubAccount(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        AccountFactory accountFactory = twilioRestClient.getAccountFactory();
        Account account = accountFactory.create(params);

        //TODO: Edit response
        log.auditLog("AccountSid of new Account : " + account.getSid());
    }

}
