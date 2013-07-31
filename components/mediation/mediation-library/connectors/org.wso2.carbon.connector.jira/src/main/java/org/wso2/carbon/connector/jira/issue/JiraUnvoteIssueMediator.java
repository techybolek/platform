package org.wso2.carbon.connector.jira.issue;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.JiraMediator;
import org.wso2.carbon.connector.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;

public class JiraUnvoteIssueMediator extends JiraMediator {

	private String votesUri;
	
	public String getVotesUri() {
		return votesUri;
	}

	public void setVotesUri(String votesUri) {
		this.votesUri = votesUri;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {
        JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
        try {
	        client.getIssueClient().unvote(new URI(getVotesUri())).claim();
        } catch (URISyntaxException e) {
	        e.printStackTrace();
        }

		return true;
	}

}
