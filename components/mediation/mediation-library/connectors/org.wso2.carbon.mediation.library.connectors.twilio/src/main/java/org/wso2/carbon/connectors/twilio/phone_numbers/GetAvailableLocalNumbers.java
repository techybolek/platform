package org.wso2.carbon.connectors.twilio.phone_numbers;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.instance.AvailablePhoneNumber;
import com.twilio.sdk.resource.list.AvailablePhoneNumberList;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
* Class mediator for getting available local numbers in an account.
* For more information, http://www.twilio.com/docs/api/rest/available-phone-numbers
*/
public class GetAvailableLocalNumbers extends AbstractMediator {

    //Authorization details
    private String accountSid;
    private String authToken;

    //Country of the required numbers list
    private String country;

    //Basic filter parameters
    //See http://www.twilio.com/docs/api/rest/available-phone-numbers#local-get-basic-filters
    private String areaCode;
    private String contains;
    private String inRegion;
    private String inPostalCode;

    //Advance filter parameters (only for numbers in the Unites States and Canada).
    //See https://www.twilio.com/docs/api/rest/available-phone-numbers#local-get-advanced-filters
    private String nearLatLong;
    private String nearNumber;
    private String inLata;
    private String inRateCenter;
    private String distance;


    @Override
    public boolean mediate(MessageContext messageContext) {

        SynapseLog log = getLog(messageContext);

        //Get parameters from the messageContext
        accountSid = (String) messageContext.getProperty("TwilioAccountSid");
        authToken = (String) messageContext.getProperty("TwilioAuthToken");

        getParameters(messageContext);

        Map<String, String> filter = createParameterMap();

        try {
            getAvailableLocalNumbers(log, filter);
        } catch (TwilioRestException e) {
            log.auditError(e.getMessage());
            throw new SynapseException(e);
        }

        return true;
    }

    private void getAvailableLocalNumbers(SynapseLog log, Map<String, String> filter) throws
            IllegalArgumentException, TwilioRestException{

        TwilioRestClient twilioRestClient = new TwilioRestClient(accountSid, authToken);

        AvailablePhoneNumberList numbers =
                twilioRestClient.getAccount().getAvailablePhoneNumbers(filter, country, "Local");

        //Note: This is the list containing the list of numbers.
        // If there are no numbers matching the filter, the list will be empty
        List<AvailablePhoneNumber> list = numbers.getPageData();

        //TODO: change response
        if (list.isEmpty()){
            log.auditLog("No numbers matching the filter");
        }

        for(AvailablePhoneNumber number : list){
            log.auditLog("Friendly Name:" + number.getFriendlyName()
                    + "    Phone Number: " +number.getPhoneNumber());
        }
    }

    /**
     * Create a map containing the parameters required to filter the list of numbers, which has
     * been defined
     * @return The map containing the defined parameters
     */
    private Map<String, String> createParameterMap() {
        Map<String, String> filter = new HashMap<String, String>();

        //Basic
        if (areaCode != null) {
            filter.put("AreaCode", areaCode);
        }
        if (contains != null) {
            filter.put("Contains", contains);
        }
        if (inRegion != null) {
            filter.put("InRegion", inRegion);
        }
        if (inPostalCode != null) {
            filter.put("InRateCenter", inPostalCode);
        }

        //Advance
        if (nearLatLong != null) {
            filter.put("NearLatLong", nearLatLong);
        }
        if (nearNumber != null) {
            filter.put("NearNumber", nearNumber);
        }
        if (inLata != null) {
            filter.put("InLata", inLata);
        }
        if (inRateCenter != null) {
            filter.put("InRateCenter", inRateCenter);
        }
        if (distance != null) {
            filter.put("Distance", distance);
        }
        return filter;
    }


    /**
     * Populates the parameters from the properties from the message context (If provided)
     * @param messageContext SynapseMessageContext
     */
    private void getParameters(MessageContext messageContext) {
        //Must be provided
        country = (String) messageContext.getProperty("TwilioCountry");

        //BasicFilters
        areaCode = (String) messageContext.getProperty("TwilioPhoneNumberAreaCode");
        contains = (String) messageContext.getProperty("TwilioPhoneNumberContains");
        inRegion = (String) messageContext.getProperty("TwilioPhoneNumberInRegion");
        inPostalCode = (String) messageContext.getProperty("TwilioPhoneNumberInPostalCode");

        //Advance Filters
        nearLatLong = (String) messageContext.getProperty("TwilioPhoneNumberNearLatLong");
        nearNumber = (String) messageContext.getProperty("TwilioPhoneNumberNearNumber");
        inLata = (String) messageContext.getProperty("TwilioPhoneNumberInLata");
        inRateCenter = (String) messageContext.getProperty("TwilioPhoneNumberInRateCenter");
        distance = (String) messageContext.getProperty("TwilioPhoneNumberDistance");
    }
}
