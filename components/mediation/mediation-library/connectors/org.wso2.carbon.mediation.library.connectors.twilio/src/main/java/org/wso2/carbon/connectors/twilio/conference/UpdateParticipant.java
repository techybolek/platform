package org.wso2.carbon.connectors.twilio.conference;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Conference;
import com.twilio.sdk.resource.instance.Participant;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for updating a particulars of a participant in a given conference.
* For more information, seehttp://www.twilio.com/docs/api/rest/participant
*/
public class UpdateParticipant extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;
    private String conferenceSid;
    private String callSidOfParticipant;

    //Optional parameter
    //Also see http://www.twilio.com/docs/api/rest/participant#instance-post
    private String muted;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //Get the conference Sid
        conferenceSid = (String) messageContext.getProperty("TwilioConferenceSid");

        //Get the call sid of the participant
        callSidOfParticipant = (String) messageContext.getProperty("TwilioCallSidOfParticipant");

        muted = (String) messageContext.getProperty("TwilioParticipantMuted");

        try {
            updateParticipant(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void updateParticipant(SynapseLog log) throws TwilioRestException, IllegalArgumentException {
        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the conference
        Conference conference = twilioRestClient.getAccount().getConference(conferenceSid);

        //Get the participant by his call sid.
        // Refer https://www.twilio.com/docs/api/rest/participant
        Participant participant = conference.getParticipant(callSidOfParticipant);

        Map<String, String> params = new HashMap<String, String>();
        params.put("Muted", muted);

        //update the matching participant instance
        participant.update(params);
        //TODO: change response
        log.auditLog("Is Participant Muted: " + participant.isMuted());

    }
}