package org.apache.amber.oauth2.as.validator;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.validators.AbstractValidator;

import javax.servlet.http.HttpServletRequest;

public class SAML20BearerAssertionValidator extends AbstractValidator<HttpServletRequest> {
    public SAML20BearerAssertionValidator() {
        requiredParams.add(OAuth.OAUTH_ASSERTION);
    }
}
