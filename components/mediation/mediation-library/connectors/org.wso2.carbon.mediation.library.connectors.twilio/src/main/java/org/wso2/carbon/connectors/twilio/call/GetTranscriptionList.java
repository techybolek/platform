package org.wso2.carbon.connectors.twilio.call;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Transcription;
import com.twilio.sdk.resource.list.TranscriptionList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
/*
* Class mediator for getting a list of Call transcription instances.
* For more information, see http://www.twilio.com/docs/api/rest/transcription
*/
public class GetTranscriptionList extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        try {
            getTranscriptionList(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getTranscriptionList(SynapseLog log) throws
            IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the Transcription List
        TranscriptionList transcriptions = twilioRestClient.getAccount().getTranscriptions();

        // Loop over transcriptions and print out a property
        for (Transcription transcription : transcriptions) {
            log.auditLog("Transcription Text" + transcription.getTranscriptionText());
        }

    }
}
