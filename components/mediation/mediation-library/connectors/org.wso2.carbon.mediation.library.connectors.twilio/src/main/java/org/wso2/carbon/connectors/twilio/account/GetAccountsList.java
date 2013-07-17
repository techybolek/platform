package org.wso2.carbon.connectors.twilio.account;


import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.list.AccountList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for retrieving a list of all accounts for an accountSID.
* For more information, see http://www.twilio.com/docs/api/rest/account
*/
public class GetAccountsList extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //Optional parameters. For more information, see http://www.twilio.com/docs/api/rest/account#list-get-filters
    private String friendlyName;
    private String status;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        friendlyName = (String) messageContext.getProperty("TwilioAccountFriendlyName");
        status = (String) messageContext.getProperty("TwilioAccountStatus");

        // Build a filter for the AccountList, i.e. filter parameters are passed as a Map
        Map<String, String> filter = new HashMap<String, String>();

        if ( friendlyName != null) {
            filter.put("FriendlyName", friendlyName);
        }
        if (status != null) {
            filter.put("Status", status);
        }

        try {
            getAccountList(log, filter);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getAccountList(SynapseLog log, Map<String, String> filter) throws
            IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the AccountList
        AccountList accounts = twilioRestClient.getAccounts(filter);

        // Loop over accounts and print out a property
        for (Account account : accounts) {
            //TODO: edit response
            log.auditLog("Friendly Name: " + account.getFriendlyName()+ "   Date Created: " + account.getDateCreated());
        }

    }
}
