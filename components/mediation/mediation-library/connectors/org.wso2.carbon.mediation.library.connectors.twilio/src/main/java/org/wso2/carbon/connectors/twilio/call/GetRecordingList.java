package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Recording;
import com.twilio.sdk.resource.list.RecordingList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for getting a recording instance list
* For more information, see http://www.twilio.com/docs/api/rest/recording#instance
*/
public class GetRecordingList extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    private String callSid;
    private String dateCreated;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //The Sid of the recording, must be provided
        callSid = (String) messageContext.getProperty("TwilioRecordingCallSid");
        dateCreated = (String) messageContext.getProperty("TwilioRecordingDateCreated");

        Map<String, String> filter = new HashMap<String, String>();

        if (callSid != null) {
            filter.put("CallSid", callSid);
        }
        if (dateCreated != null) {
            filter.put("DateCreated", dateCreated);
        }

        try {
            getRecordingList(log, filter);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getRecordingList(SynapseLog log, Map<String, String> filter) throws IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        RecordingList recordings = twilioRestClient.getAccount().getRecordings(filter);

        //TODO: change response
        // Loop over recordings and print out a property
        for (Recording recording : recordings) {
            log.auditLog("Call Sid: " + recording.getCallSid() + "  Duration: " + recording.getDuration());
        }
    }
}
