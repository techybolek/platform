package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Recording;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for deleting a recording instance based on its Sid.
* For more information, see http://www.twilio.com/docs/api/rest/recording#instance-delete
*/
public class DeleteRecording extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;
    private String recordingSid;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //The Sid of the recording, must be provided
        recordingSid = (String) messageContext.getProperty("TwilioRecordingSid");

        try {
            deleteRecording(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void deleteRecording(SynapseLog log) throws IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        // Get an recording object from its sid.
        Recording recording = twilioRestClient.getAccount().getRecording(recordingSid);
        boolean deleted = recording.delete();

        //TODO: change response
        if (deleted) {
            log.auditLog("Recording deleted successfully");
        }

    }
}
