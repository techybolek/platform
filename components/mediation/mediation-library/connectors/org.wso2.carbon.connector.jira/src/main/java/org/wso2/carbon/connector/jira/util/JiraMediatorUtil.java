/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.jira.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class JiraMediatorUtil {

	private static final DomDriver domDriver = new DomDriver("UTF-8");
	private static final XStream xstream = new XStream(domDriver);

	private static final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

	/**
	 * Returns a client instance to access the JIRA REST API. The client is
	 * created with basic HTTP authentication
	 * 
	 * @param uri
	 *            the URI of the JIRA server
	 * @param username
	 *            the username of the user who is accessing JIRA
	 * @param password
	 *            the password
	 * @return a {@link JiraRestClient} instance that is able to access the JIRA
	 *         REST API
	 */
	public static JiraRestClient getClient(String uri, String username, String password) {
		// TODO Check whether this method can be optimized.
		URI jiraServerUri = null;
		try {
			jiraServerUri = new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return factory.createWithBasicHttpAuthentication(jiraServerUri, username, password);
	}

	/**
	 * Gets the {@link Transition} object that has the given transition name
	 * 
	 * @param transitions
	 *            a collection that implements {@code Iterable} which contains
	 *            the available transitions
	 * @param transitionName
	 *            the name of the transition that is needed
	 * @return a {@code Transition} object of the given name or null if such a
	 *         transition does not exist or the transitions collection is empty
	 * 
	 */
	public static Transition getTransitionByName(Iterable<Transition> transitions,
			String transitionName) {
		for (Transition transition : transitions) {
			if (transition.getName().equals(transitionName)) {
				return transition;
			}
		}
		return null;
	}

	public static void getPojoFromJson(String json, Object pojo) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			pojo = mapper.readValue(json, pojo.getClass());
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String getXmlFromPojo(Object obj) {
		Class<?> objClassType = obj.getClass();
		xstream.alias(objClassType.getSimpleName().toLowerCase(), objClassType);

		return xstream.toXML(obj);
	}
	
	public static void preparePayload(MessageContext messageContext, OMElement element) {
		SOAPBody soapBody = messageContext.getEnvelope().getBody();
		for (Iterator itr = soapBody.getChildElements(); itr.hasNext();) {
			OMElement child = (OMElement) itr.next();
			child.detach();
		}
		for (Iterator itr = element.getChildElements(); itr.hasNext();) {
			OMElement child = (OMElement) itr.next();
			soapBody.addChild(child);
		}
	}

}
