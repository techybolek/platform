package org.wso2.carbon.connectors.twilio.phone_numbers;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.IncomingPhoneNumberFactory;
import com.twilio.sdk.resource.instance.IncomingPhoneNumber;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.snmp4j.agent.mo.snmp.SNMPv2MIB;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for purchasing a phone numbers.
* For more information, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers#list-post
*/
public class PurchasePhoneNumber extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    // Exactly One of the below parameters must be provided
    // For more information, see https://www.twilio.com/docs/api/rest/incoming-phone-numbers#list-post-required-parameters
    private String phoneNumber;
    private String areaCode;

    //Optional Parameters
    //For the full specification, see http://www.twilio.com/docs/api/rest/incoming-phone-numbers#list-post-optional-parameters
    private String friendlyName;
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
    private String apiVersion;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        getParameters(messageContext);

        Map<String, String> params = createParameterMap();

        try {
            purchasePhoneNumber(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }


        return true;
    }

    private void purchasePhoneNumber(SynapseLog log, Map<String, String> params) throws
            IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        IncomingPhoneNumberFactory numberFactory = twilioRestClient.getAccount().getIncomingPhoneNumberFactory();

        IncomingPhoneNumber number = numberFactory.create(params);

        //TODO: change response
        log.auditLog("Successfully purchased the number.Friendly Name:" + number.getFriendlyName()
                + "    Phone Number: " + number.getPhoneNumber() + "     Sid: " + number.getSid());

    }


    /**
     * Create a map containing the parameters required to purchase the number, which has been defined
     *
     * @return The map containing the defined parameters
     */
    private Map<String, String> createParameterMap() {

        Map<String, String> params = new HashMap<String, String>();

        //Exactly one of PhoneNumber or AreaCode must be provided
        if (phoneNumber != null) {
            params.put("PhoneNumber", phoneNumber);
        } else {
            params.put("AreaCode", areaCode);
        }

        if (friendlyName != null) {
            params.put("FriendlyName", friendlyName);
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
        if (apiVersion != null) {
            params.put("ApiVersion", apiVersion);
        }

        return params;
    }

    /**
     * Populates the parameters from the properties from the message context (If provided)
     *
     * @param messageContext SynapseMessageContext
     */
    private void getParameters(MessageContext messageContext) {

        phoneNumber = (String) messageContext.getProperty("TwilioPhoneNumber");
        areaCode = (String) messageContext.getProperty("TwilioPhoneNumberAreaCode");


        //Optional Parameters
        friendlyName = (String) messageContext.getProperty("TwilioPhoneNumberFriendlyName");
        voiceUrl = (String) messageContext.getProperty("TwilioPhoneNumberVoiceUrl");
        voiceMethod = (String) messageContext.getProperty("TwilioPhoneNumberVoiceMethod");
        voiceFallbackUrl = (String) messageContext.getProperty("TwilioPhoneNumberVoiceFallbackUrl");
        voiceFallbackMethod = (String) messageContext.getProperty("TwilioPhoneNumberVoiceFallbackMethod");
        statusCallback = (String) messageContext.getProperty("TwilioPhoneNumberStatusCallback");
        statusCallbackMethod = (String) messageContext.getProperty("TwilioPhoneNumberStatusCallbackMethod");
        voiceCallerIdLookup = (String) messageContext.getProperty("TwilioPhoneNumberVoiceCallerIdLookup");
        voiceApplicationSid = (String) messageContext.getProperty("TwilioPhoneNumberVoiceCallerIdLookup");
        smsUrl = (String) messageContext.getProperty("TwilioPhoneNumberSmsUrl");
        smsMethod = (String) messageContext.getProperty("TwilioPhoneNumberSmsMethod");
        smsFallbackUrl = (String) messageContext.getProperty("TwilioPhoneNumberSmsFallbackUrl");
        smsFallbackMethod = (String) messageContext.getProperty("TwilioPhoneNumberSmsFallbackMethod");
        smsApplicationSid = (String) messageContext.getProperty("TwilioPhoneNumberSmsStatusCallback");
        apiVersion = (String) messageContext.getProperty("TwilioPhoneNumberApiVersion");

    }
}
