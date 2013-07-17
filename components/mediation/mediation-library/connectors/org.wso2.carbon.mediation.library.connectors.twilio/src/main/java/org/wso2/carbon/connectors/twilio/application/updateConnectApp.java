package org.wso2.carbon.connectors.twilio.application;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.ConnectApp;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
* Class mediator for updating a connect app instance with optional parameters
* For more information, see http://www.twilio.com/docs/api/rest/connect-apps
*/
public class updateConnectApp extends AbstractMediator {

    private String accountSid;
    private String authToken;

    //Mandatory parameter
    private String connectAppSid;

    //optional parameters for updating the ConnectApp retrieved by the Sid.
    //See http://www.twilio.com/docs/api/rest/connect-apps#instance-post-optional-parameters for
    //specifications.
    private String friendlyName;
    private String authorizeRedirectUrl;
    private String deauthorizeCallbackUrl;
    private String deauthorizeCallbackMethod;
    private String permissions;
    private String description;
    private String companyName;
    private String homepageUrl;


    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        getParameters(messageContext);

        Map<String, String> params = createParameterMap();

        try {
            updateConnectApp(log, params);
        } catch (Exception e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void updateConnectApp(SynapseLog log, Map<String, String> params) throws
            IllegalArgumentException, TwilioRestException {

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        //Retrieve the matching Connect App based on its Sid
        ConnectApp connectApp = twilioRestClient.getAccount().getConnectApp(connectAppSid);

        //update the relevant connect app with the parameters specified.
        connectApp.update(params);


        //TODO: change response
        log.auditLog(connectApp.toString());
    }

    /**
     * Create a map containing the parameters required to update the application, which has been defined
     * @return The map containing the defined parameters
     */
    private Map<String, String> createParameterMap() {
        Map<String, String> params = new HashMap<String, String>();

        if (friendlyName != null) {
            params.put("FriendlyName", friendlyName);
        }
        if (authorizeRedirectUrl != null) {
            params.put("VoiceMethod", authorizeRedirectUrl);
        }
        if (deauthorizeCallbackUrl != null) {
            params.put("VoiceFallbackUrl", deauthorizeCallbackUrl);
        }
        if (deauthorizeCallbackMethod != null) {
            params.put("VoiceFallbackMethod", deauthorizeCallbackMethod);
        }
        if (permissions != null) {
            params.put("StatusCallback", permissions);
        }
        if (description != null) {
            params.put("StatusCallbackMethod", description);
        }
        if (companyName != null) {
            params.put("VoiceCallerIdLookup", companyName);
        }
        if (homepageUrl != null) {
            params.put("SmsUrl", homepageUrl);
        }
        return params;
    }

    /**
     * Populates the parameters from the properties from the message context (If provided)
     * @param messageContext SynapseMessageContext
     */
    private void getParameters(MessageContext messageContext) {

        connectAppSid = (String) messageContext.getProperty("TwilioConnectAppSid");

        friendlyName = (String) messageContext.getProperty("TwilioConnectAppFriendlyName");
        authorizeRedirectUrl = (String) messageContext.getProperty("TwilioConnectAppAuthorizeRedirectUrl");
        deauthorizeCallbackUrl = (String) messageContext.getProperty("TwilioConnectAppDeauthorizeCallbackUrl");
        deauthorizeCallbackMethod = (String) messageContext.getProperty("TwilioConnectAppDeauthorizeCallbackMethod");
        permissions = (String) messageContext.getProperty("TwilioConnectAppPermissions");
        description = (String) messageContext.getProperty("TwilioConnectAppDescription");
        companyName = (String) messageContext.getProperty("TwilioConnectAppCompanyName");
        homepageUrl = (String) messageContext.getProperty("TwilioConnectAppHomepageUrl");
    }

}
