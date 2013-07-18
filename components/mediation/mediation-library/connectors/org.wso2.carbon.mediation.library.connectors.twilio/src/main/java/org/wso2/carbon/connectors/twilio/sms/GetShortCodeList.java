package org.wso2.carbon.connectors.twilio.sms;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.ShortCode;
import com.twilio.sdk.resource.list.ShortCodeList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.Map;

/*
 * Class mediator for getting a list of all short codes associated with an account.
 * For more information, http://www.twilio.com/docs/api/rest/short-codes#list
 */
public class GetShortCodeList extends AbstractMediator {

	public boolean mediate(MessageContext messageContext) {

		SynapseLog log = getLog(messageContext);

		// optional filters
		// See http://www.twilio.com/docs/api/rest/short-codes#list-get-filters
		// for
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		String shortCode = (String) messageContext.getProperty("TwilioShortCode");
		String friendlyName = (String) messageContext.getProperty("TwilioFriendlyName");

		// Map for holding optional filter parameters
		Map<String, String> filter = new HashMap<String, String>();

		// null-checking and addition to map
		if (shortCode != null) {
			filter.put("ShortCode", shortCode);
		}
		if (friendlyName != null) {
			filter.put("FriendlyName", friendlyName);
		}

		try {
			getShortCodeList(accountSid,authToken,log, filter);
		} catch (Exception e) {
			// TODO: handle exception
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

		return true;
	}

	private void getShortCodeList(String accountSid, String authToken, SynapseLog log,
			Map<String, String> filter) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		ShortCodeList shortCodeList = twilioRestClient.getAccount().getShortCodes(filter);

		// Iterate through list
		// TODO: change response type
		if (shortCodeList.getTotal() == 0) {
			log.auditLog("No short codes found in the account.");
		}
		for (ShortCode code : shortCodeList) {
			log.auditLog("Short Code: " + code.getShortCode());
		}
	}

}
