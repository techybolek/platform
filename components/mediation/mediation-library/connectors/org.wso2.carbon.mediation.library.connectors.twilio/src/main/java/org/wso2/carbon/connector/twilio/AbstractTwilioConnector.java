package org.wso2.carbon.connector.twilio;

import java.util.Map;

import org.apache.synapse.SynapseLog;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.AccountFactory;
import com.twilio.sdk.resource.instance.Account;

public abstract class AbstractTwilioConnector extends AbstractConnector {

	protected void updateAccount(String accountSid, String subAccountSid,
			String authToken, SynapseLog log, Map<String, String> params)
			throws TwilioRestException, IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		Account account;
		// If a sub account need to be updated
		if (subAccountSid != null) {
			account = twilioRestClient.getAccount(subAccountSid);
		} else {
			account = twilioRestClient.getAccount(); // If the main account
														// needs to be updated
		}
		account.update(params);

		// TODO: change response
		log.auditLog("Account " + account.getSid() + " was updated");
	}

	protected void createSubAccount(String accountSid, String authToken, SynapseLog log,
			Map<String, String> params) throws TwilioRestException,
			IllegalArgumentException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		AccountFactory accountFactory = twilioRestClient.getAccountFactory();
		Account account = accountFactory.create(params);

		log.auditLog("AccountSid of new Account : " + account.getSid());
	}
}
