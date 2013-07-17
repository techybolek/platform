package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Recording;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.io.InputStream;

/*
* Class mediator for getting a recording instance based on its Sid.
* For more information, see http://www.twilio.com/docs/api/rest/recording
*/
public class GetRecording extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;
    private String recordingSid;
    private String recordingReturnType;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //The Sid of the recording, must be provided
        recordingSid = (String) messageContext.getProperty("TwilioRecordingSid");
        recordingReturnType = (String) messageContext.getProperty("TwilioRecordingReturnType");

        try {
            if (recordingReturnType != null) {
                getRecording(log, recordingReturnType);
            } else {
                getRecording(log);
            }
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getRecording(SynapseLog log, String recordingReturnType) throws IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        // Get an recording object from its sid.
        Recording recording = twilioRestClient.getAccount().getRecording(recordingSid);

        InputStream inputStream = recording.getMedia(recordingReturnType);

        //TODO: change response
        log.auditLog("Call Sid: " + recording.getCallSid() + "  Duration: " + recording.getDuration());

    }

    private void getRecording(SynapseLog log) throws IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        // Get an recording object from its sid.
        Recording recording = twilioRestClient.getAccount().getRecording(recordingSid);

        //TODO: change response
        log.auditLog("Call Sid: " + recording.getCallSid() + "  Duration: " + recording.getDuration());

    }
}
