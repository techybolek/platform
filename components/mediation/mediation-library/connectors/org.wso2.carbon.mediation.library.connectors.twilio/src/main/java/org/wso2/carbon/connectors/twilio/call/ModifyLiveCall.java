package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Call;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;
/*
* Class mediator for modifying a live call.
* For more information, see http://www.twilio.com/docs/api/rest/change-call-state
*/
public class ModifyLiveCall extends AbstractMediator {

    private String accountSid;
    private String authToken;

    private String callSid;

    //Optional parameters. For specifications and formats, see
    //http://www.twilio.com/docs/api/rest/change-call-state
    private String url;
    private String status;
    private String method;

    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //Must be provided
        callSid = (String) messageContext.getProperty("TwilioCallSid");

        //Available parameters to be modified (Optional)
        url = (String) messageContext.getProperty("TwilioCallUrl");
        status = (String) messageContext.getProperty("TwilioCallStatus");
        method = (String) messageContext.getProperty("TwilioCallMethod");

        //Map for optional parameters
        Map<String,String> params = new HashMap<String, String>();

        if (url != null){
            params.put("Url", url);
        }
        if (status != null){
            params.put("Status", status);
        }
        if (method != null){
            params.put("Method", method);
        }

        try {
            modifyLiveCall(log, params);
        } catch (Exception e) {
            log.auditError(e);
            throw new SynapseException(e);
        }

        return true;
    }

    private void modifyLiveCall(SynapseLog log, Map<String, String> params) throws
            TwilioRestException, IllegalArgumentException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the call to be modified
        Call call = twilioRestClient.getAccount().getCall(callSid);
        //update the matching live call with the specified parameters
        call.update(params);

        //TODO: chane response
        log.auditLog("Call "+callSid+" Modified.");
    }
}
