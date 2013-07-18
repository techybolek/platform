package org.wso2.carbon.connectors.twilio.account;


import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.list.AccountList;

/*
* Class mediator for retrieving a list of all accounts for an accountSID.
* For more information, see http://www.twilio.com/docs/api/rest/account
*/
public class GetAccountsList extends AbstractConnector {


	public void connect(MessageContext messageContext) throws ConnectException {

        SynapseLog log = getLog(messageContext);

        //Authorization details
        //Get parameters from the messageContext
        String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        String authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //Optional parameters. For more information, see http://www.twilio.com/docs/api/rest/account#list-get-filters
        String friendlyName = (String) messageContext.getProperty("TwilioAccountFriendlyName");
        String status = (String) messageContext.getProperty("TwilioAccountStatus");

        // Build a filter for the AccountList, i.e. filter parameters are passed as a Map
        Map<String, String> filter = new HashMap<String, String>();

        if ( friendlyName != null) {
            filter.put("FriendlyName", friendlyName);
        }
        if (status != null) {
            filter.put("Status", status);
        }

        try {
            getAccountList(accountSid,authToken,log, filter);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

    }

    private void getAccountList(String accountSid,String authToken,SynapseLog log, Map<String, String> filter) throws
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
