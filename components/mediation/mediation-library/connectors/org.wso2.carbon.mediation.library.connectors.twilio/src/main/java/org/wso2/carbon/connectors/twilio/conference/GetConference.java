package org.wso2.carbon.connectors.twilio.conference;


import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Conference;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

/*
* Class mediator for getting Conference instance based on its Sid.
* For more information, see http://www.twilio.com/docs/api/rest/conference
*/
public class GetConference extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //unique Sid for a conference
    private String conferenceSid;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        //Get the conference Sid
        conferenceSid = (String) messageContext.getProperty("TwilioConferenceSid");

        try {
            getConference(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getConference(SynapseLog log) throws IllegalArgumentException, TwilioRestException {
        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Get the conference
        Conference conference = twilioRestClient.getAccount().getConference(conferenceSid);

        //TODO: change response
        log.auditLog("Conference Name: " + conference.getFriendlyName() + "     Status: "
                + conference.getStatus());
    }
}
