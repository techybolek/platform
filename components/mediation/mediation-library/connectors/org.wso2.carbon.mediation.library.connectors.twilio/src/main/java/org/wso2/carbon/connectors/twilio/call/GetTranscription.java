package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Transcription;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
/*
* Class mediator for getting a call transcription.
* For more information, see http://www.twilio.com/docs/api/rest/transcription
*/
public class GetTranscription extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;
    private String transcriptionSid;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");
        transcriptionSid = (String) messageContext.getProperty("TwilioTranscriptionSid");

        try {
            getTranscription(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getTranscription(SynapseLog log) throws IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the transcription by it's Sid
        Transcription transcription = twilioRestClient.getAccount().getTranscription(transcriptionSid);

        //TODO: Change response
        log.auditLog("Transcription Text" + transcription.getTranscriptionText());

    }
}
