package org.wso2.carbon.connectors.twilio.phone_numbers;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import com.twilio.sdk.resource.list.IncomingPhoneNumberList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;
/*
* Class mediator for getting the list of incoming phone numbers.
* For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers
*/
public class GetIncomingPhoneNumberList extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //Filters for the results
    private String phoneNumber;
    private String friendlyName;



    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");


        //Parameters for the filters
        phoneNumber = (String) messageContext.getProperty("TwilioIncomingPhoneNumber");
        friendlyName = (String) messageContext.getProperty("TwilioIncomingPhoneNumberFriendlyName");

        Map<String,String> filter = new HashMap<String, String>();

        if ( phoneNumber != null){
            filter.put("PhoneNumber", phoneNumber);
        }
        if ( friendlyName != null){
            filter.put("FriendlyName", friendlyName);
        }

        try {
            getIncomingPhoneNumberList(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getIncomingPhoneNumberList(SynapseLog log)throws
            IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        IncomingPhoneNumberList numbers = twilioRestClient.getAccount().getIncomingPhoneNumbers();

        //TODO: change response
        // Loop over numbers and print out a property
        for (IncomingPhoneNumber number : numbers) {
            log.auditLog("Friendly Name:" + number.getFriendlyName()
                    + "    Phone Number: " + number.getPhoneNumber());
        }
    }
}
