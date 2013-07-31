package org.wso2.carbon.connector.jira.issue;

import static org.wso2.carbon.connector.jira.JiraConstants.CONTEXT_WATCHERS;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.JiraMediator;
import org.wso2.carbon.connector.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Watchers;

public class JiraGetWatchersMediator extends JiraMediator {

	private String watchersUri;
	
	public String getWatchersUri() {
		return watchersUri;
	}

	public void setWatchersUri(String watchersUri) {
		this.watchersUri = watchersUri;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {
        JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
        Watchers watchers = null;
        try {
	        watchers = client.getIssueClient().getWatchers(new URI(getWatchersUri())).claim();
        } catch (URISyntaxException e) {
	        e.printStackTrace();
        }

        synCtx.setProperty(CONTEXT_WATCHERS, watchers);
		return true;
	}

}
