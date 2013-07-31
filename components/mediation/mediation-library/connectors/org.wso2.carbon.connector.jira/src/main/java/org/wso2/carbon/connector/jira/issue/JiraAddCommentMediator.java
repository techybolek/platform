package org.wso2.carbon.connector.jira.issue;

import org.apache.synapse.MessageContext;
import org.joda.time.DateTime;
import org.wso2.carbon.connector.jira.JiraMediator;
import org.wso2.carbon.connector.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Visibility;

public class JiraAddCommentMediator extends JiraMediator {

	private String issueId;
	private String commentBody;

	public String getCommentBody() {
		return commentBody;
	}

	public void setCommentBody(String commentBody) {
		this.commentBody = commentBody;
	}

	public String getIssueId() {
		return issueId;
	}

	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		Issue issue = client.getIssueClient().getIssue(getIssueId()).claim();
		BasicUser author = client.getUserClient().getUser(getUsername()).claim();
		DateTime now = new DateTime();
		Comment comment =
		                  new Comment(issue.getSelf(), getCommentBody(), author, null, now, now,
		                              new Visibility(Visibility.Type.GROUP, "All"), null);
		client.getIssueClient().addComment(issue.getCommentsUri(), comment).claim();

		return true;
	}

}
