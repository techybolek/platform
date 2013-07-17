package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.resource.list.CallList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for getting a list of Call instances.
* For more information, see http://www.twilio.com/docs/api/rest/call
*/
public class GetCallList extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //optional parameters. See  http://www.twilio.com/docs/api/rest/call#list-get-filters
    //for specifications.
    private String to;
    private String from;
    private String status;
    private String startTime;
    private String parentCallSid;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        to = (String) messageContext.getProperty("TwilioCallListByTo");
        from = (String) messageContext.getProperty("TwilioCallListByFrom");
        status = (String) messageContext.getProperty("TwilioCallListByStatus");
        startTime = (String) messageContext.getProperty("TwilioCallListByStartTime");
        parentCallSid = (String) messageContext.getProperty("TwilioCallListByParentCallSid");

        // Build a filter for the CallList.
        Map<String,String> filter = new HashMap<String, String>();
        if ( to != null){
            filter.put("To", to);
        }
        if (from != null){
            filter.put("From", from);
        }
        if (status != null){
            filter.put("Status", status);
        }
        if (startTime != null){
            filter.put("StartTime", startTime);
        }
        if (parentCallSid != null){
            filter.put("ParentCallSid", parentCallSid);
        }

        try {
            getCallList(log, filter);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getCallList(SynapseLog log, Map<String, String> filter) throws
            IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the call list according to the given parameters
        CallList calls = twilioRestClient.getAccount().getCalls(filter);

        //TODO: change response
        // Loop over calls and print out a property
        for (Call call : calls) {
            log.auditLog("Called To: " + call.getTo() + "   From: " +call.getFrom()
                    + "   Call Direction: "+ call.getDirection());
        }

    }
}
