package org.wso2.carbon.identity.application.authentication.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

public class ApplicationAuthenticatorsConfiguration {
	
	private static Log log = LogFactory.getLog(ApplicationAuthenticatorsConfiguration.class);
	private static ApplicationAuthenticatorsConfiguration instance;
	private boolean isSingleFactor = true;
	
	private Map<String, AuthenticatorConfig> authenticatorConfigMap =
            new Hashtable<String, AuthenticatorConfig>();
	
    private static final String AUTHENTICATORS_FILE_NAME = "application-authenticators.xml";

    //Constant definitions for Elements
    private static final String ELEM_AUTHENTICATOR = "Authenticator";
    private static final String ELEM_STATUS = "Status";

    //Constant definitions for attributes
    private static final String AUTHENTICATOR_ATTR_NAME = "name";
    private static final String AUTHENTICATOR_ATTR_FACTOR = "factor";
    private static final String AUTHENTICATOR_ATTR_DISABLED = "disabled";
    
    private static final String STATUS_ATTR_VALUE = "value";
    private static final String STATUS_ATTR_LOGIN_PAGE = "loginPage";

    /**
     * this class is used to represent an authenticator configuration in the runtime
     */
    public static class AuthenticatorConfig {

        private String name;

        private int factor;

        private boolean disabled;
        
        private Map<String, String> statusMap;

        private AuthenticatorConfig(String name, int factor, boolean disabled, Map<String, String> statusMap) {
            this.name = name;
            this.factor = factor;
            this.disabled = disabled;
            this.statusMap = statusMap;
        }

        public String getName() {
            return name;
        }

        public boolean isDisabled() {
            return disabled;
        }

		public int getFactor() {
        	return factor;
        }

		public Map<String, String> getStatusMap() {
        	return statusMap;
        }
    }
    
    private ApplicationAuthenticatorsConfiguration() {
        initialize();
    }

    /**
     * Read the authenticator info from the file and populate the in-memory model
     */
    private void initialize() {
        String authenticatorsFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                        "security" + File.separator + AUTHENTICATORS_FILE_NAME;
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(new File(authenticatorsFilePath));
            OMElement documentElement = new StAXOMBuilder(fileInputStream).getDocumentElement();

            // for each every authenticator defined, create a AuthenticatorConfig instance
            for (Iterator authenticatorElements = documentElement.getChildrenWithLocalName(ELEM_AUTHENTICATOR);
                 authenticatorElements.hasNext();) {
                AuthenticatorConfig authenticatorConfig = processAuthenticatorElement((OMElement) authenticatorElements.next());
                if (authenticatorConfig != null) {
                    this.authenticatorConfigMap.put(authenticatorConfig.getName(), authenticatorConfig);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("application-authenticators.xml file is not available");
        } catch (XMLStreamException e) {
            log.error("Error reading the application-authenticators.xml");
        }
        finally{
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                log.warn("Unable to close the file input stream created for application-authenticators.xml");
            }
        }
    }

    /**
     * Create AuthenticatorConfig elements for each authenticator entry
     * @param authenticatorElem OMElement for Authenticator
     * @return  AuthenticatorConfig object
     */
    private AuthenticatorConfig processAuthenticatorElement(OMElement authenticatorElem) {
        // read the name of the authenticator. this is a mandatory attribute.
        OMAttribute nameAttr = authenticatorElem.getAttribute(new QName(AUTHENTICATOR_ATTR_NAME));
        // if the name is not given, do not register this authenticator
        if(nameAttr == null){
            log.warn("Each Authenticator Configuration should have a unique name attribute. +" +
                     "This Authenticator will not be registered.");
            return null;
        }
        String authenticatorName = nameAttr.getAttributeValue();

        // check whether the disabled attribute is set
        boolean disabled = false;
        if(authenticatorElem.getAttribute(new QName(AUTHENTICATOR_ATTR_DISABLED)) != null){
            disabled = Boolean.parseBoolean(authenticatorElem.getAttribute(
                    new QName(AUTHENTICATOR_ATTR_DISABLED)).getAttributeValue());
        }

        // read the factor
        int factor = 0;
        for(Iterator factorElemItr = authenticatorElem.getChildrenWithLocalName(AUTHENTICATOR_ATTR_FACTOR);
            factorElemItr.hasNext();){
            factor = Integer.parseInt(((OMElement)factorElemItr.next()).getText());
            if (isSingleFactor && factor != 1) {
            	isSingleFactor = false;
            }
        }
        
        // read the Status
        Map<String, String> statusMap = new Hashtable<String, String>();
        for(Iterator statusElemItr = authenticatorElem.getChildrenWithLocalName(ELEM_STATUS);
            statusElemItr.hasNext();){
            OMElement statusElement = (OMElement)statusElemItr.next();
            String value = statusElement.getAttributeValue(new QName(STATUS_ATTR_VALUE));
            String loginPage = statusElement.getAttributeValue(new QName(STATUS_ATTR_LOGIN_PAGE));
            statusMap.put(value, loginPage);
        }

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig(authenticatorName,
                factor, disabled, statusMap);

        return authenticatorConfig;
    }
    
    public static ApplicationAuthenticatorsConfiguration getInstance(){
        if(instance == null){
            instance = new ApplicationAuthenticatorsConfiguration();
        }
        return instance;
    }

    /**
     * Return the authenticator config for the given name
     * @param authenticatorName name of the authenticator
     * @return  AuthenticatorConfig object
     */
    public AuthenticatorConfig getAuthenticatorConfig(String authenticatorName){
        return authenticatorConfigMap.get(authenticatorName);
    }

	public boolean isSingleFactor() {
    	return isSingleFactor;
    }
} 
