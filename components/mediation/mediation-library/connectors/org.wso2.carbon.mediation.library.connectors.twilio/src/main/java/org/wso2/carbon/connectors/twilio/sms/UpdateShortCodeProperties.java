package org.wso2.carbon.connectors.twilio.sms;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.ShortCode;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for updating the properties of a short code.
* For more information, see http://www.twilio.com/docs/api/rest/short-codes
*/
public class UpdateShortCodeProperties extends AbstractMediator {

    private String accountSid;
    private String authToken;
    private String shortCodeSid;

    //optional parameters for updating the short code
    //For more details, see http://www.twilio.com/docs/api/rest/short-codes#instance-post-optional-parameters
    private String friendlyName;
    private String apiVersion;
    private String smsUrl;
    private String smsMethod;
    private String smsFallBackUrl;
    private String smsFallBackMethod;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");
        shortCodeSid = (String) messageContext.getProperty("TwilioShortCodeSid");

        friendlyName = (String) messageContext.getProperty("TwilioFriendlyName");
        apiVersion = (String) messageContext.getProperty("TwilioApiVersion");
        smsUrl = (String) messageContext.getProperty("TwilioSmsUrl");
        smsMethod = (String) messageContext.getProperty("TwilioSmsMethod");
        smsFallBackMethod = (String) messageContext.getProperty("TwilioSmsFallBackUrl");
        smsFallBackUrl = (String) messageContext.getProperty("TwilioSmsFallBackMethod");

        //optional parameters passed through this map
        Map<String, String> params = new HashMap<String, String>();

        if (friendlyName != null) {
            params.put("FriendlyName", friendlyName);
        }
        if (apiVersion != null) {
            params.put("ApiVersion", apiVersion);
        }
        if (smsUrl != null) {
            params.put("SmsUrl", smsUrl);
        }
        if (smsMethod != null) {
            params.put("SmsMethod", smsMethod);
        }
        if (smsFallBackUrl != null) {
            params.put("SmsFallBackUrl", smsFallBackUrl);
        }
        if (smsFallBackMethod != null) {
            params.put("SmsFallBackMethod", smsFallBackMethod);
        }

        try {
            updateShortCode(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void updateShortCode(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        ShortCode shortCode = twilioRestClient.getAccount().getShortCode(shortCodeSid);
        shortCode.update(params);

        //TODO: change response
        log.auditLog("Short code " + shortCodeSid + " updated.");
    }

}
