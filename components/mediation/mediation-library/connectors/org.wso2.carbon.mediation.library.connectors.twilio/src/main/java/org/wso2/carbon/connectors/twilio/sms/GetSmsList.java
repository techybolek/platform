package org.wso2.carbon.connectors.twilio.sms;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Sms;
import com.twilio.sdk.resource.list.SmsList;

/*
 * Class mediator for getting a list of SMSs, with support for filters.
 * For more information, see http://www.twilio.com/docs/api/rest/sms#list
 */
public class GetSmsList extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// optional parameters. For more information, see
		// http://www.twilio.com/docs/api/rest/sms#list-get-filters
		String to = (String) messageContext.getProperty("TwilioSMSTo");
		String from = (String) messageContext.getProperty("TwilioSMSFrom");
		String dateSent = (String) messageContext.getProperty("TwilioSMSDateSent");

		// map for holding optional parameters
		Map<String, String> filter = new HashMap<String, String>();

		// null-checking and addition to map
		if (to != null) {
			filter.put("To", to);
		}
		if (from != null) {
			filter.put("From", from);
		}
		if (dateSent != null) {
			filter.put("DateSent", dateSent);
		}

		try {
			getSmsList(accountSid, authToken, log, filter);
		} catch (Exception e) {
			// TODO: handle the exception
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getSmsList(String accountSid, String authToken, SynapseLog log,
			Map<String, String> filter) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		SmsList messages = twilioRestClient.getAccount().getSmsMessages(filter);

		// iterate through the list obtained from the api query
		// TODO: change response.
		for (Sms message : messages) {
			log.auditLog("Message Sid: " + message.getSid() + "  Message: "
					+ message.getBody());
		}
	}

}
