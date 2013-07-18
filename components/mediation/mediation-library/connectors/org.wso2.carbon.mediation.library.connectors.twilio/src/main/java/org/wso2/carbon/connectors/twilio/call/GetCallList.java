package org.wso2.carbon.connectors.twilio.call;

import java.util.HashMap;
import java.util.Map;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.resource.list.CallList;

/*
 * Class mediator for getting a list of Call instances.
 * For more information, see http://www.twilio.com/docs/api/rest/call
 */
public class GetCallList extends AbstractTwilioConnector {

	@Override
	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		// Get parameters from the messageContext
		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");

		// optional parameters. See
		// http://www.twilio.com/docs/api/rest/call#list-get-filters
		// for specifications.
		String to = (String) messageContext.getProperty("TwilioCallListByTo");
		String from = (String) messageContext.getProperty("TwilioCallListByFrom");
		String status = (String) messageContext.getProperty("TwilioCallListByStatus");
		String startTime = (String) messageContext
				.getProperty("TwilioCallListByStartTime");
		String parentCallSid = (String) messageContext
				.getProperty("TwilioCallListByParentCallSid");

		// Build a filter for the CallList.
		Map<String, String> filter = new HashMap<String, String>();
		if (to != null) {
			filter.put("To", to);
		}
		if (from != null) {
			filter.put("From", from);
		}
		if (status != null) {
			filter.put("Status", status);
		}
		if (startTime != null) {
			filter.put("StartTime", startTime);
		}
		if (parentCallSid != null) {
			filter.put("ParentCallSid", parentCallSid);
		}

		try {
			getCallList(accountSid, authToken, log, filter);
		} catch (Exception e) {
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

	private void getCallList(String accountSid, String authToken, SynapseLog log,
			Map<String, String> filter) throws IllegalArgumentException,
			TwilioRestException {

		TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

		// Get the call list according to the given parameters
		CallList calls = twilioRestClient.getAccount().getCalls(filter);

		// TODO: change response
		// Loop over calls and print out a property
		for (Call call : calls) {
			log.auditLog("Called To: " + call.getTo() + "   From: " + call.getFrom()
					+ "   Call Direction: " + call.getDirection());
		}

	}
}
