package org.wso2.carbon.connectors.twilio.phone_numbers;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;
/*
* Class mediator for exchanging a phone number between accounts.
* For more information, http://www.twilio.com/docs/api/rest/subaccounts#exchanging-numbers
*/
public class ExchangePhoneNumber extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //Sid of the required number
    private String phoneNumberSid;

    //Account Sid of the account that the phone number should be exchanged to
    private String transferAccountSid;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //Must be provided
        phoneNumberSid = (String) messageContext.getProperty("TwilioPhoneNumberSid");
        transferAccountSid = (String) messageContext.getProperty("TwilioExchangePhoneNumberToAccountSid");

        Map<String, String> params = new HashMap<String, String>();
        params.put("AccountSid", transferAccountSid);

        try {
            exchangeNumber(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }


        return true;
    }

    private void exchangeNumber(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        // Get the phone number that needs to be exchanged.
        IncomingPhoneNumber number = twilioRestClient.getAccount().getIncomingPhoneNumber(phoneNumberSid);

        //update the retrieved phone number to reflect the changes made
        number.update(params);
        //TODO: change response
        log.auditLog("Number exchanged successfully.");

    }
}
