package org.wso2.carbon.connectors.twilio.phone_numbers;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for removing an incoming a phone numbers.
* For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers#instance-delete
*/
public class RemoveIncomingPhoneNumber extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //Sid of the required number
    private String incomingPhoneNumberSid;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //Must be provided
        incomingPhoneNumberSid = (String) messageContext.getProperty("TwilioIncomingPhoneNumberSid");

        try {
            removeIncomingPhoneNumber(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void removeIncomingPhoneNumber(SynapseLog log) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        // Get an incoming phone number allocated to the Twilio account by its Sid.
        IncomingPhoneNumber number = twilioRestClient.getAccount().getIncomingPhoneNumber(incomingPhoneNumberSid);

        number.delete();

        //TODO: change response
        log.auditLog("Number Deleted.");
    }
}
