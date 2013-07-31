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

package org.wso2.carbon.mediation.library.connectors.jira;

import java.net.URI;
import java.net.URISyntaxException;

import static org.wso2.carbon.mediation.library.connectors.jira.JiraConstants.CONTEXT_ROLE;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.ProjectRole;

public class JiraGetProjectRoleMediator extends JiraMediator {

	private String roleUri;

	@Override
	public boolean mediate(MessageContext mc) {
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		ProjectRole projectRole = null;
        try {
	        projectRole = client.getProjectRolesRestClient().getRole(new URI(getRoleUri())).claim();
        } catch (URISyntaxException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		mc.setProperty(CONTEXT_ROLE, projectRole);

		return true;
	}

	public String getRoleUri() {
		return roleUri;
	}

	public void setRoleUri(String roleUri) {
		this.roleUri = roleUri;
	}

}
