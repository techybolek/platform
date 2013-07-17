package org.wso2.carbon.connectors.twilio.sms;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Sms;
import com.twilio.sdk.resource.list.SmsList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for getting a list of SMSs, with support for filters.
* For more information, see http://www.twilio.com/docs/api/rest/sms#list
*/
public class GetSmsList extends AbstractMediator {

    //authentication parameters
    private String accountSid;
    private String authToken;

    //optional parameters. For more information, see
    //http://www.twilio.com/docs/api/rest/sms#list-get-filters
    private String to;
    private String from;
    private String dateSent;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        to = (String) messageContext.getProperty("TwilioSMSTo");
        from = (String) messageContext.getProperty("TwilioSMSFrom");
        dateSent = (String) messageContext.getProperty("TwilioSMSDateSent");

        //map for holding optional parameters
        Map<String, String> filter = new HashMap<String, String>();

        //null-checking and addition to map
        if (to != null) {
            filter.put("To", to);
        }
        if (from != null) {
            filter.put("From", from);
        }
        if (dateSent != null) {
            filter.put("DateSent", dateSent);
        }

        try {
            getSmsList(log, filter);
        } catch (Exception e) {
            //TODO: handle the exception
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getSmsList(SynapseLog log, Map<String, String> filter) throws
            IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        SmsList messages = twilioRestClient.getAccount().getSmsMessages(filter);

        //iterate through the list obtained from the api query
        //TODO: change response.
        for (Sms message : messages) {
            log.auditLog("Message Sid: " + message.getSid() + "  Message: " + message.getBody());
        }
    }

}
