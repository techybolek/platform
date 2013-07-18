package org.wso2.carbon.connectors.twilio.account;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Account;

/*
 * Class mediator for retrieving an account or subaccount instance from its SID
 * For more information, see http://www.twilio.com/docs/api/rest/account and
 * http://www.twilio.com/docs/api/rest/subaccounts,
 */
public class GetAccount extends AbstractConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		// Authorization details
		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		String subAccountSid = (String) messageContext.getProperty("TwilioSubAccountSid");

		try {
			getAccount(accountSid,subAccountSid, authToken, log);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getAccount(String accountSid,String subAccountSid, String authToken, SynapseLog log)
			throws TwilioRestException, IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		Account account;
		// If a SubAccount is required
		if (subAccountSid != null) {
			account = twilioRestClient.getAccount(subAccountSid);
		} else {
			account = twilioRestClient.getAccount(); // Returns the main account
		}

		// TODO: change response
		log.auditLog("Friendly Name: " + account.getFriendlyName());
	}

}
