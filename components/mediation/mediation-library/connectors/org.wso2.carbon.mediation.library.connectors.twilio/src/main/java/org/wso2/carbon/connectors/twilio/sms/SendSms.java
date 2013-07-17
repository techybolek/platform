package org.wso2.carbon.connectors.twilio.sms;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for sending an SMS.
* For more information, see http://www.twilio.com/docs/api/rest/sending-sms
*/
public class SendSms extends AbstractMediator {

    private String accountSid;
    private String authToken;

    //required parameters
    //see http://www.twilio.com/docs/api/rest/sending-sms#post-parameters-required for more details.
    private String to;
    private String from;
    private String body;

    // optional parameters
    //see http://www.twilio.com/docs/api/rest/sending-sms#post-parameters-optional for more details.
    private String statusCallBackUrl;
    private String applicationSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        to = (String) messageContext.getProperty("TwilioSMSTo");
        from = (String) messageContext.getProperty("TwilioSMSFrom");
        body = (String) messageContext.getProperty("TwilioSMSBody");

        statusCallBackUrl = (String) messageContext.getProperty("TwilioSMSStatusCallBackUrl");
        applicationSid = (String) messageContext.getProperty("TwilioApplicationSid");


        //the map used for passing parameters
        Map<String, String> params = new HashMap<String, String>();

        //add the optional parameters to the map
        params.put("To", to);
        params.put("From", from);
        params.put("Body", body);

        if (applicationSid != null) {
            params.put("ApplicationSid", applicationSid);
        }
        if (statusCallBackUrl != null) {
            params.put("StatusCallback", statusCallBackUrl);
        }

        try {
            sendSms(log, params);
        } catch (Exception e) {
            //TODO: handle the exception
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void sendSms(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Creates a SMS and sends it
        SmsFactory messageFactory = twilioRestClient.getAccount().getSmsFactory();
        Sms message = messageFactory.create(params);

        //TODO: change response type
        log.auditLog("Message Sid: " + message.getSid() + "     Status: " + message.getStatus());

    }

}
