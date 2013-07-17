package org.wso2.carbon.connectors.twilio.conference;


import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Conference;
import com.twilio.sdk.resource.instance.Participant;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting a participant of a given conference.
* For more information, see http://www.twilio.com/docs/api/rest/conference#instance-subresources-participants
*/
public class GetParticipant extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    private String conferenceSid;
    private String callSidOfParticipant;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //Get the conference Sid
        conferenceSid = (String) messageContext.getProperty("TwilioConferenceSid");
        callSidOfParticipant = (String) messageContext.getProperty("TwilioCallSidOfParticipant");

        try {
            getParticipant(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getParticipant(SynapseLog log) throws IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the conference
        Conference conference = twilioRestClient.getAccount().getConference(conferenceSid);

        //TODO: change response
        //Get the participant by his call sid.
        // Refer https://www.twilio.com/docs/api/rest/participant
        Participant participant = conference.getParticipant(callSidOfParticipant);

        log.auditLog("Is Participant Muted: " + participant.isMuted());
    }
}
