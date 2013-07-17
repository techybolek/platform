package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Application;
import com.twilio.sdk.resource.list.ApplicationList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for getting an application instance based on the ApplicationSid parameter
* For more information, see http://www.twilio.com/docs/api/rest/applications
*/
public class getApplicationList extends AbstractMediator {

    private String accountSid;
    private String authToken;

    //optional parameter to be used as a list filter.
    //See http://www.twilio.com/docs/api/rest/applications#list-get-filters for specifications.
    private String friendlyName;

    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        friendlyName = (String) messageContext.getProperty("TwilioApplicationFriendlyName");

        //filter map for specifying optional parameters
        Map<String, String> filter = new HashMap<String, String>();

        if (friendlyName != null) {
            filter.put("FriendlyName", friendlyName);
        }

        try {
            getApplicationList(log);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getApplicationList(SynapseLog log) throws
            IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        ApplicationList applications =twilioRestClient.getAccount().getApplications();

        //loop over the retrieved ApplicationList object instance
        //TODO: change response
        for(Application application:applications){
            log.auditLog("Application: "+application.getSid());
        }
    }

}
