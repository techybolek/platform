package org.wso2.carbon.registry.rest.api.security;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class UnAuthorizedException extends WebApplicationException {
	
	public UnAuthorizedException(String message) {
		super(Response.status(Response.Status.BAD_REQUEST)
	             .entity(message).type(MediaType.APPLICATION_XML_TYPE).build());
	}

}
