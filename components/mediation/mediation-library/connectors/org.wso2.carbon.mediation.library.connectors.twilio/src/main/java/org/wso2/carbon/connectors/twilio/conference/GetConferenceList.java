package org.wso2.carbon.connectors.twilio.conference;


import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Conference;
import com.twilio.sdk.resource.list.ConferenceList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for getting Conference instances.
* For more information, see http://www.twilio.com/docs/api/rest/conference
*/
public class GetConferenceList extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //optional parameters for filtering resultant conferences.
    //See http://www.twilio.com/docs/api/rest/conference#list-get-filters
    private String status;
    private String friendlyName;
    private String dateCreated;
    private String dateUpdated;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        status = (String) messageContext.getProperty("TwilioStatus");
        friendlyName = (String) messageContext.getProperty("TwilioFriendlyName");
        dateCreated = (String) messageContext.getProperty("TwilioDateCreated");
        dateUpdated = (String) messageContext.getProperty("TwilioDateUpdated");


        // Build a filter for the ConferenceList
        Map<String, String> filter = new HashMap<String, String>();
        if(status != null){
            filter.put("Status", status );
        }
        if(friendlyName != null){
            filter.put("FriendlyName", friendlyName);
        }
        if(dateCreated != null){
            filter.put("DateCreated", dateUpdated );
        }if(dateUpdated != null){
            filter.put("DateUpdated", dateUpdated );
        }

        try {
            getConferenceList(log, filter);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getConferenceList(SynapseLog log, Map<String, String> filter)throws
            IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        ConferenceList conferences = twilioRestClient.getAccount().getConferences(filter);

        //TODO: change response
        // Loop over conferences and print out a property
        for (Conference conference : conferences) {
            log.auditLog("Conference Name: " + conference.getFriendlyName() + "     Status: "
                    + conference.getStatus());
        }
    }
}