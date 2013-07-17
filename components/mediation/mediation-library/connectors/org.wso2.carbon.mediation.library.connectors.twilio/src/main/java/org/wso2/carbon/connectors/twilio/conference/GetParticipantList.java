package org.wso2.carbon.connectors.twilio.conference;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Conference;
import com.twilio.sdk.resource.instance.Participant;
import com.twilio.sdk.resource.list.ParticipantList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting a list of participants from a given conference.
* For more information, see http://www.twilio.com/docs/api/rest/conference#instance-subresources-participants
*/
public class GetParticipantList extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;
    private String conferenceSid;


    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //Get the conference Sid
        conferenceSid = (String) messageContext.getProperty("TwilioConferenceSid");

        try {
            getParticipantList(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getParticipantList(SynapseLog log) throws IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the conference
        Conference conference = twilioRestClient.getAccount().getConference(conferenceSid);

        //Get the participants list by the defined filter
        ParticipantList participantList = conference.getParticipants();

        //TODO: change response
        for (Participant participant : participantList) {
            log.auditLog("Participant Call Sid: " + participant.getCallSid());
        }
    }
}
