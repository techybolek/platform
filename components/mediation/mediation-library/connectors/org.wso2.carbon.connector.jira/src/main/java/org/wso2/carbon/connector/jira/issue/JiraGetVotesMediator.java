package org.wso2.carbon.connector.jira.issue;

import static org.wso2.carbon.connector.jira.JiraConstants.CONTEXT_VOTES;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.JiraMediator;
import org.wso2.carbon.connector.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Votes;

public class JiraGetVotesMediator extends JiraMediator {

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
		Votes votes = null;
        try {
	        votes = client.getIssueClient().getVotes(new URI(getVotesUri())).claim();
        } catch (URISyntaxException e) {
	        e.printStackTrace();
        }

		synCtx.setProperty(CONTEXT_VOTES, votes);

		return true;
	}

}
