package org.wso2.carbon.connectors.twilio.account;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

/*
 * Class mediator for creating a sub-account
 * For more information, see http://www.twilio.com/docs/api/rest/subaccounts
 */
public class CreateSubAccount extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		// Authentication details
		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");
		// optional parameter.
		// For more information, see
		// http://www.twilio.com/docs/api/rest/subaccounts#creating-subaccounts-post-parameters-optional
		String friendlyName = (String) messageContext
				.getProperty("TwilioAccountFriendlyName");

		// Build a filter for the AccountList
		Map<String, String> params = new HashMap<String, String>();

		// If a friendly name has been provided for the account;
		// if not will use the current date and time (Default by Twilio).
		if (friendlyName != null) {
			params.put("FriendlyName", friendlyName);
		}

		try {
			createSubAccount(accountSid,authToken,log, params);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	

}
