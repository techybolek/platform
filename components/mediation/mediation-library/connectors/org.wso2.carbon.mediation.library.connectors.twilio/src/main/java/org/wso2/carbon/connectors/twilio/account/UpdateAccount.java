package org.wso2.carbon.connectors.twilio.account;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

/*
 * Class mediator for Updating an account/subaccount.
 * For more information, see http://www.twilio.com/docs/api/rest/account and
 * http://www.twilio.com/docs/api/rest/subaccounts
 */
public class UpdateAccount extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Authorization details
		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// If a sub account need to be updated this is required. Else main
		// account will be updated.
		// Only friendly name of main account can be updated.
		String friendlyName = (String) messageContext
				.getProperty("TwilioAccountFriendlyName");
		String status = (String) messageContext.getProperty("TwilioAccountStatus");
		String subAccountSid = (String) messageContext.getProperty("TwilioSubAccountSid");

		// Creates a Map containing the parameters which are needed to be
		// updated
		Map<String, String> params = new HashMap<String, String>();

		if (friendlyName != null) {
			params.put("FriendlyName", friendlyName);
		}
		if (status != null) {
			params.put("Status", status);
		}

		try {
			super.updateAccount(accountSid, subAccountSid, authToken, log, params);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

}
