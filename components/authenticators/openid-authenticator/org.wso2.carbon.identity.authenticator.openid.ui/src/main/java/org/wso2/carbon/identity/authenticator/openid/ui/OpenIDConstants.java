package org.wso2.carbon.identity.authenticator.openid.ui;

public final class OpenIDConstants {

    public final static String NS = "http://schema.openid.net";
    public final static String OPENID_URL = "http://specs.openid.net/auth/2.0";
    public final static String ATTR_MODE = "openid.mode";
    public final static String ATTR_IDENTITY = "openid.identity";
    public final static String ATTR_RESPONSE_NONCE = "openid.response_nonce";
    public final static String ATTR_OP_ENDPOINT = "openid.op_endpoint";
    public final static String ATTR_NS = "openid.ns";
    public final static String ATTR_CLAIM_ID = "openid.claimed_id";
    public final static String ATTR_RETURN_TO = "openid.return_to";
    public final static String ATTR_ASSOC_HANDLE = "openid.assoc_handle";
    public final static String ATTR_SIGNED = "openid.signed";
    public final static String ATTR_SIG = "openid.sig";
    public final static String OPENID_IDENTIFIER = "openid_identifier";
    public final static String ASSOCIATE = "associate";
    public final static String CHECKID_SETUP = "checkid_setup";
    public final static String CHECKID_IMMEDIATE = "checkid_immediate";
    public final static String CHECK_AUTHENTICATION = "check_authentication";
    public final static String DISC = "openid-disc";
    public static final String PREFIX = "openid";
    public final static String ASSERTION = "openidAssertion";
    public final static String COMPLETE = "complete";
    public final static String ONLY_ONCE = "Only Once";
    public final static String ONCE = "once";
    public final static String ALWAYS = "always";
    public final static String DENY = "Deny";
    public final static String ACTION = "_action";
    public final static String OPENID_RESPONSE = "id_res";
    public static final String AUTHENTICATED_AND_APPROVED = "authenticatedAndApproved";
    public final static String CANCEL = "cancel";
    public final static String FALSE = "false";
    public final static String PARAM_LIST = "parameterlist";
    public final static String PASSWORD = "password";
    public static final String SERVICE_NAME_STS_OPENID = "sts-openid-ut";
    public static final String SERVICE_NAME_MEX_OPENID = "mex-openid-ut";
    public static final String SERVICE_NAME_MEX_IC_OPENID = "mex-openid-ic";
    public static final String SERVICE_NAME_STS_IC_OPENID = "sts-openid-ic";

    public static final String SIMPLE_REGISTRATION = "sreg";
    public static final String ATTRIBUTE_EXCHANGE = "ax";
    public static final String PAPE = "pape";

    public static class PapeAttributes {

        public final static String AUTH_POLICIES = "auth_policies";
        public final static String NIST_AUTH_LEVEL = "nist_auth_level";
        public final static String AUTH_AGE = "auth_age";
        public final static String PHISHING_RESISTANCE = "http://schemas.openid.net/pape/policies/2007/06/phishing-resistant";
        public final static String MULTI_FACTOR = "http://schemas.openid.net/pape/policies/2007/06/multi-factor";
        public final static String MULTI_FACTOR_PHYSICAL = "http://schemas.openid.net/pape/policies/2007/06/multi-factor-physical";
        public final static String XMPP_BASED_MULTIFACTOR_AUTH = "xmpp_based_multifactor_auth";
        public final static String INFOCARD_BASED_MULTIFACTOR_AUTH = "infocard_based_multifactor_auth";
    }

    public static class SimpleRegAttributes {

        // As per the OpenID Simple Registration Extension 1.0 specification fields below should be
        // included in the Identity Provider's response when "openid.mode" is "id_res"

        public final static String NS_SREG = "http://openid.net/sreg/1.0";
        public final static String NS_SREG_1 = "http://openid.net/extensions/sreg/1.1";
        public final static String SREG = "openid.sreg.";
        public final static String OP_SREG = "openid.ns.sreg";
    }

    public static class ExchangeAttributes extends SimpleRegAttributes {

        public final static String NS = "http://axschema.org";
        public final static String NS_AX = "http://openid.net/srv/ax/1.0";
        public final static String EXT = "openid.ns.ext1";
        public final static String MODE = "openid.ext1.mode";
        public final static String TYPE = "openid.ext1.type.";
        public final static String VALUE = "openid.ext1.value.";
        public final static String FETCH_RESPONSE = "fetch_response";
    }
}
