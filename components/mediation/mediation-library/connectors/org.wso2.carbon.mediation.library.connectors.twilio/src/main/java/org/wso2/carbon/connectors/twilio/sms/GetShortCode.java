package org.wso2.carbon.connectors.twilio.sms;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.ShortCode;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.connector.twilio.AbstractTwilioConnector;
import org.wso2.carbon.mediation.library.connectors.core.ConnectException;

/*
 * Class mediator for getting the short code based on the Sid.
 * For more information, see http://www.twilio.com/docs/api/rest/short-codes
 */
public class GetShortCode extends AbstractTwilioConnector {

	public void connect(MessageContext messageContext) throws ConnectException {

		SynapseLog log = getLog(messageContext);

		String accountSid = (String) messageContext.getProperty("TwilioAccountSid");
		String authToken = (String) messageContext.getProperty("TwilioAuthToken");
		String shortCodeSid = (String) messageContext.getProperty("TwilioShortCodeSid");

		try {
			TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid,
					authToken);
			ShortCode shortCode = twilioRestClient.getAccount()
					.getShortCode(shortCodeSid);
			// TODO: change response
			log.auditLog("Message: " + shortCode.getShortCode());
		} catch (Exception e) {
			// TODO: handle exception
			log.auditError(e.getMessage());
			throw new SynapseException(e);
		}

	}

}
