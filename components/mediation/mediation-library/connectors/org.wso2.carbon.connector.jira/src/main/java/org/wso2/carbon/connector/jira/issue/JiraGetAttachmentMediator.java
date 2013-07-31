package org.wso2.carbon.connector.jira.issue;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.wso2.carbon.connector.jira.JiraConstants.CONTEXT_ATTACHMENT_ISTREAM;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.JiraMediator;
import org.wso2.carbon.connector.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;

public class JiraGetAttachmentMediator extends JiraMediator {
	private String attachmentUri;

	public String getAttachmentUri() {
		return attachmentUri;
	}

	public void setAttachmentUri(String attachmentUri) {
		this.attachmentUri = attachmentUri;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		InputStream iStream = null;
		try {
			iStream = client.getIssueClient().getAttachment(new URI(getAttachmentUri())).claim();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		synCtx.setProperty(CONTEXT_ATTACHMENT_ISTREAM, iStream);

		return true;
	}

}
