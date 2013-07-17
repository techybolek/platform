package org.wso2.carbon.connectors.twilio.account;


import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for Updating an account/subaccount.
* For more information, see http://www.twilio.com/docs/api/rest/account and
* http://www.twilio.com/docs/api/rest/subaccounts
*/
public class UpdateAccount extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //If a sub account need to be updated this is required. Else main account will be updated.
    //Only friendly name of main account can be updated.
    private String subAccountSid;

    //Optional parameters for the filter. See  http://www.twilio.com/docs/api/rest/account#instance-post
    private String friendlyName;
    private String status;


    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        friendlyName = (String) messageContext.getProperty("TwilioAccountFriendlyName");
        status = (String) messageContext.getProperty("TwilioAccountStatus");
        subAccountSid = (String) messageContext.getProperty("TwilioSubAccountSid");

        // Creates a Map containing the parameters which are needed to be updated
        Map<String, String> params = new HashMap<String, String>();

        if ( friendlyName != null) {
            params.put("FriendlyName", friendlyName);
        }
        if (status != null) {
            params.put("Status", status);
        }


        try {
            updateAccount(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void updateAccount(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        Account account;
        //If a sub account need to be updated
        if ( subAccountSid != null) {
            account = twilioRestClient.getAccount(subAccountSid);
        }
        else {
            account = twilioRestClient.getAccount();     //If the main account needs to be updated
        }
        account.update(params);

        //TODO: change response
        log.auditLog("Account "+ account.getSid() + " was updated");
    }
}
