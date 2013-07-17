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
* Class mediator for getting the list of incoming phone numbers.
* For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers#instance-post
*/
public class UpdateIncomingPhoneNumber extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //Sid o the phone umber to be updated
    private String incomingPhoneNumberSid;

    //Optional parameters
    //See http://www.twilio.com/docs/api/rest/incoming-phone-numbers#instance-post-optional-parameters
    //for the full specification.
    private String friendlyName;
    private String apiVersion;
    private String voiceUrl;
    private String voiceMethod;
    private String voiceFallbackUrl;
    private String voiceFallbackMethod;
    private String statusCallback;
    private String statusCallbackMethod;
    private String voiceCallerIdLookup;
    private String voiceApplicationSid;
    private String smsUrl;
    private String smsMethod;
    private String smsFallbackUrl;
    private String smsFallbackMethod;
    private String smsApplicationSid;
    private String transferToAccountSid;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        getParameters(messageContext);

        Map<String, String> params = createParameterMap();

        try {
            updateIncomingPhoneNumber(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void updateIncomingPhoneNumber(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        // Get an incoming phone number allocated to the Twilio account by its Sid.
        IncomingPhoneNumber number = twilioRestClient.getAccount().getIncomingPhoneNumber(incomingPhoneNumberSid);

        number.update(params);

        //TODO: change response
        log.auditLog("Number Updated.");
    }


    /**
     * Create a map containing the parameters required to update the number, which has been defined
     *
     * @return The map containing the defined parameters
     */
    private Map<String, String> createParameterMap() {

        Map<String, String> params = new HashMap<String, String>();

        if (friendlyName != null) {
            params.put("FriendlyName", friendlyName);
        }

        if (apiVersion != null) {
            params.put("ApiVersion", apiVersion);
        }
        if (voiceUrl != null) {
            params.put("VoiceUrl", voiceUrl);
        }
        if (voiceMethod != null) {
            params.put("VoiceMethod", voiceMethod);
        }
        if (voiceFallbackUrl != null) {
            params.put("VoiceFallbackUrl", voiceFallbackUrl);
        }
        if (voiceFallbackMethod != null) {
            params.put("VoiceFallbackMethod", voiceFallbackMethod);
        }
        if (statusCallback != null) {
            params.put("StatusCallback", statusCallback);
        }
        if (statusCallbackMethod != null) {
            params.put("StatusCallbackMethod", statusCallbackMethod);
        }
        if (voiceCallerIdLookup != null) {
            params.put("VoiceCallerIdLookup", voiceCallerIdLookup);
        }
        if (voiceApplicationSid != null) {
            params.put("VoiceApplicationSid", voiceApplicationSid);
        }
        if (smsUrl != null) {
            params.put("SmsUrl", smsUrl);
        }
        if (smsMethod != null) {
            params.put("SmsMethod", smsMethod);
        }
        if (smsFallbackUrl != null) {
            params.put("SmsFallbackUrl", smsFallbackUrl);
        }
        if (smsFallbackMethod != null) {
            params.put("SmsFallbackMethod", smsFallbackMethod);
        }
        if (smsApplicationSid != null) {
            params.put("SmsApplicationSid", smsApplicationSid);
        }
        if (transferToAccountSid != null) {
            params.put("AccountSid", transferToAccountSid);
        }
        return params;
    }

    /**
     * Populates the parameters from the properties from the message context (If provided)
     *
     * @param messageContext SynapseMessageContext
     */
    private void getParameters(MessageContext messageContext) {
        //Must be provided
        incomingPhoneNumberSid = (String) messageContext.getProperty("TwilioIncomingPhoneNumberSid");

        //Parameters to be updated
        friendlyName = (String) messageContext.getProperty("TwilioIPNFriendlyName");
        apiVersion = (String) messageContext.getProperty("TwilioIPNApiVersion");
        voiceUrl = (String) messageContext.getProperty("TwilioIPNVoiceUrl");
        voiceMethod = (String) messageContext.getProperty("TwilioIPNVoiceMethod");
        voiceFallbackUrl = (String) messageContext.getProperty("TwilioIPNVoiceFallbackUrl");
        voiceFallbackMethod = (String) messageContext.getProperty("TwilioIPNVoiceFallbackMethod");
        statusCallback = (String) messageContext.getProperty("TwilioIPNStatusCallback");
        statusCallbackMethod = (String) messageContext.getProperty("TwilioIPNStatusCallbackMethod");
        voiceCallerIdLookup = (String) messageContext.getProperty("TwilioIPNVoiceCallerIdLookup");
        voiceApplicationSid = (String) messageContext.getProperty("TwilioIPNVoiceCallerIdLookup");
        smsUrl = (String) messageContext.getProperty("TwilioIPNSmsUrl");
        smsMethod = (String) messageContext.getProperty("TwilioIPNSmsMethod");
        smsFallbackUrl = (String) messageContext.getProperty("TwilioIPNSmsFallbackUrl");
        smsFallbackMethod = (String) messageContext.getProperty("TwilioIPNSmsFallbackMethod");
        smsApplicationSid = (String) messageContext.getProperty("TwilioIPNSmsStatusCallback");
        transferToAccountSid = (String) messageContext.getProperty("TwilioIPNSmsTransferToAccountSid");
    }
}
