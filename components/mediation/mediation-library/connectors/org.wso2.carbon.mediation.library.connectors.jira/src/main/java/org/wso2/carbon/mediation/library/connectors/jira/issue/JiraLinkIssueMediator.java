package org.wso2.carbon.mediation.library.connectors.jira.issue;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.JiraMediator;
import org.wso2.carbon.mediation.library.connectors.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput;

public class JiraLinkIssueMediator extends JiraMediator {

	private String fromIssueKey;
	private String toIssueKey;
	private String linkType;
	
	public String getFromIssueKey() {
		return fromIssueKey;
	}

	public void setFromIssueKey(String fromIssueKey) {
		this.fromIssueKey = fromIssueKey;
	}

	public String getToIssueKey() {
		return toIssueKey;
	}

	public void setToIssueKey(String toIssueKey) {
		this.toIssueKey = toIssueKey;
	}

	public String getLinkType() {
		return linkType;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		LinkIssuesInput linkIssueInput = new LinkIssuesInput(getFromIssueKey(), getToIssueKey(), getLinkType());
		client.getIssueClient().linkIssue(linkIssueInput).claim();
		
		return true;
	}

}
