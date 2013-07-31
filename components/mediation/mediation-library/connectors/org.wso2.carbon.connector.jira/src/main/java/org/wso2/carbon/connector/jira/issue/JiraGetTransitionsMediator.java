package org.wso2.carbon.connector.jira.issue;

import static org.wso2.carbon.connector.jira.JiraConstants.CONTEXT_TRANSITIONS;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.JiraMediator;
import org.wso2.carbon.connector.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;

public class JiraGetTransitionsMediator extends JiraMediator {

	private String issueId;

	@Override
	public boolean mediate(MessageContext synCtx) {
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		Issue issue = client.getIssueClient().getIssue(getIssueId()).claim();
		Iterable<Transition> transitions = client.getIssueClient().getTransitions(issue).claim();

		synCtx.setProperty(CONTEXT_TRANSITIONS, transitions);

		return true;
	}

	public String getIssueId() {
		return issueId;
	}

	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}
}
