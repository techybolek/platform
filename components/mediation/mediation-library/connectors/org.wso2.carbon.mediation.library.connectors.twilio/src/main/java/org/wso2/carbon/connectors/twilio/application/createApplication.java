package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.ApplicationFactory;
import com.twilio.sdk.resource.instance.Application;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for updating an application instance with optional parameters
* For more information, see http://www.twilio.com/docs/api/rest/applications
*/
public class createApplication extends AbstractMediator {

    private String accountSid;
    private String authToken;

    //Required
    private String friendlyName;

    //optional parameters for creating the application retrieved by the Sid. See
    // http://www.twilio.com/docs/api/rest/applications#list-post-optional-parameters
    private String apiVersion;
    private String voiceUrl;
    private String voiceMethod;
    private String voiceFallbackUrl;
    private String voiceFallbackMethod;
    private String statusCallback;
    private String statusCallbackMethod;
    private String voiceCallerIdLookup;
    private String smsUrl;
    private String smsMethod;
    private String smsFallbackUrl;
    private String smsFallbackMethod;
    private String smsStatusCallback;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        getParameters(messageContext);


        Map<String, String> params = createParameterMap();

        try {
            createApplication(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void createApplication(SynapseLog log, Map<String, String> params) throws
            IllegalArgumentException,TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //creating a new application under the specified AccountSID
        ApplicationFactory appFactory = twilioRestClient.getAccount().getApplicationFactory();
        Application application = appFactory.create(params);

        //TODO: change response
        log.auditLog("Application "+application.getSid()+" created successfully");
    }

    /**
     * Create a map containing the parameters required to create the application, which has been defined
     * @return The map containing the defined parameters
     */
    private Map<String, String> createParameterMap() {

        //creating the map for optional parameters
        Map<String, String> params = new HashMap<String, String>();
        params.put("FriendlyName", friendlyName);

        //null-checking and addition to map
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
        if (smsStatusCallback != null) {
            params.put("SmsStatusCallback", smsStatusCallback);
        }
        return params;
    }

    /**
     * Populates the parameters from the properties from the message context (If provided)
     * @param messageContext SynapseMessageContext
     */
    private void getParameters(MessageContext messageContext) {
        friendlyName = (String) messageContext.getProperty("TwilioApplicationFriendlyName");

        apiVersion = (String) messageContext.getProperty("TwilioApplicationApiVersion");
        voiceUrl = (String) messageContext.getProperty("TwilioApplicationVoiceUrl");
        voiceMethod = (String) messageContext.getProperty("TwilioApplicationVoiceMethod");
        voiceFallbackUrl = (String) messageContext.getProperty("TwilioApplicationVoiceFallbackUrl");
        voiceFallbackMethod = (String) messageContext.getProperty("TwilioApplicationVoiceFallbackMethod");
        statusCallback = (String) messageContext.getProperty("TwilioApplicationStatusCallback");
        statusCallbackMethod = (String) messageContext.getProperty("TwilioApplicationStatusCallbackMethod");
        voiceCallerIdLookup = (String) messageContext.getProperty("TwilioApplicationVoiceCallerIdLookup");
        smsUrl = (String) messageContext.getProperty("TwilioApplicationSmsUrl");
        smsMethod = (String) messageContext.getProperty("TwilioApplicationSmsMethod");
        smsFallbackUrl = (String) messageContext.getProperty("TwilioApplicationSmsFallbackUrl");
        smsFallbackMethod = (String) messageContext.getProperty("TwilioApplicationSmsFallbackMethod");
        smsStatusCallback = (String) messageContext.getProperty("TwilioApplicationSmsStatusCallback");
    }

}
