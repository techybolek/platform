package org.wso2.carbon.mediation.library.connectors.jira.issue;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.JiraMediator;
import org.wso2.carbon.mediation.library.connectors.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;

public class JiraVoteIssueMediator extends JiraMediator {

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
	        client.getIssueClient().vote(new URI(getVotesUri())).claim();
        } catch (URISyntaxException e) {
	        e.printStackTrace();
        }
        
		return true;
	}

}
