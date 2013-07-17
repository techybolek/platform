package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Call;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;
/*
* Class mediator for making a call.
* For more information, see http://www.twilio.com/docs/api/rest/making-calls
*/
public class MakeCall extends AbstractMediator{
    //Parameter details. For specifications and formats, see
    //http://www.twilio.com/docs/api/rest/making-calls#post-parameters-required and
    //http://www.twilio.com/docs/api/rest/making-calls#post-parameters-optional.

    //Authorization details
    private String accountSid;
    private String authToken;

    //Compulsory parameters
    private String to;
    private String from;

    //One of the below parameters must be provided
    private String callUrl;
    private String applicationSid;

    //Optional parameters
    private String method;
    private String fallbackUrl;
    private String fallbackMethod;
    private String statusCallback;
    private String statusCallbackMethod;
    private String sendDigits;
    private String ifMachine;
    private String timeout;
    private String record;

    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        getParameters(messageContext);

        Map<String, String> callParams = createParameterMap();

        try {
            makeCall(log, callParams);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void makeCall(SynapseLog log, Map<String, String> callParams) throws TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        CallFactory callFactory = twilioRestClient.getAccount().getCallFactory();

        Call call = callFactory.create(callParams);

        log.auditLog("Call Successful. Call Sid: " + call.getSid());
    }

    /**
     * Create a map containing the parameters required to make the call, which has been defined
     * @return The map containing the defined parameters
     */
    private Map<String, String> createParameterMap() {

        Map<String,String> callParams = new HashMap<String, String>();

        callParams.put("To", to);
        callParams.put("From", from);

        //Only one of the below must be provided
        if (callUrl != null){
            callParams.put("Url", callUrl);
        }
        else {
            callParams.put("ApplicationSid", applicationSid);
        }

        //These are optional parameters. Need to check whether the parameters have been defined
        if (method != null){
            callParams.put("Method", method);
        }
        if (fallbackUrl != null){
            callParams.put("FallbackUrl", fallbackUrl);
        }
        if (fallbackMethod != null){
            callParams.put("FallbackMethod", fallbackMethod);
        }
        if (statusCallback != null){
            callParams.put("StatusCallback", statusCallback);
        }
        if (statusCallbackMethod != null){
            callParams.put("StatusCallbackMethod",statusCallbackMethod);
        }
        if (sendDigits != null){
            callParams.put("SendDigits",sendDigits);
        }
        if (ifMachine != null){
            callParams.put("IfMachine", ifMachine);
        }
        if (timeout != null){
            callParams.put("Timeout", timeout);
        }
        if (record != null){
            callParams.put("Record",record);
        }

        return callParams;
    }

    /**
     * Populates the parameters from the properties from the message context (If provided)
     * @param messageContext SynapseMessageContext
     */
    private void getParameters(MessageContext messageContext) {

        //These are compulsory
        to = (String) messageContext.getProperty("TwilioCallTo");
        from =(String) messageContext.getProperty("TwilioCallFrom");

        //One of the below
        callUrl =(String) messageContext.getProperty("TwilioCallUrl");
        applicationSid = (String) messageContext .getProperty("TwilioApplicationSid");

        //Optional parameters
        method = (String) messageContext.getProperty("TwilioMethod");
        fallbackUrl = (String) messageContext.getProperty("TwilioFallbackUrl");
        fallbackMethod = (String) messageContext.getProperty("TwilioFallbackMethod");
        statusCallback = (String) messageContext.getProperty("TwilioStatusCallback");
        statusCallbackMethod = (String) messageContext.getProperty("TwilioStatusCallbackMethod");
        sendDigits = (String) messageContext.getProperty("TwilioSendDigits");
        ifMachine = (String) messageContext.getProperty("TwilioIfMachine");
        timeout = (String) messageContext.getProperty("TwilioTimeout");
        record = (String) messageContext.getProperty("TwilioRecord");
    }
}
