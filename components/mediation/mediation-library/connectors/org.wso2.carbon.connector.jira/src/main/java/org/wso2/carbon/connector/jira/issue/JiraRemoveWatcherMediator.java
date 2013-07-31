package org.wso2.carbon.connector.jira.issue;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.JiraMediator;
import org.wso2.carbon.connector.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;

public class JiraRemoveWatcherMediator extends JiraMediator {
	private String watcherUsername;
	private String watchersUri;

	public String getWatcherUsername() {
		return watcherUsername;
	}

	public void setWatcherUsername(String watcherUsername) {
		this.watcherUsername = watcherUsername;
	}

	public String getWatchersUri() {
		return watchersUri;
	}

	public void setWatchersUri(String watchersUri) {
		this.watchersUri = watchersUri;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		try {
			client.getIssueClient().removeWatcher(new URI(getWatchersUri()), getWatcherUsername())
			      .claim();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return true;
	}

}
