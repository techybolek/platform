package org.wso2.carbon.mediation.library.connectors.jira.issue;

import static org.wso2.carbon.mediation.library.connectors.jira.JiraConstants.INTERNAL_DATETIME_FORMAT;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.synapse.MessageContext;
import org.joda.time.format.DateTimeFormat;
import org.wso2.carbon.mediation.library.connectors.jira.JiraMediator;
import org.wso2.carbon.mediation.library.connectors.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput;

public class JiraAddWorkLogMediator extends JiraMediator {
	private String issueUri;
	private String commentBody;
	private Integer minutesSpent;
	private String visibility;
	private String worklogUri;
	private String startDate;

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getWorklogUri() {
		return worklogUri;
	}

	public void setWorklogUri(String worklogUri) {
		this.worklogUri = worklogUri;
	}

	public String getIssueUri() {
		return issueUri;
	}

	public void setIssueUri(String issueUri) {
		this.issueUri = issueUri;
	}

	public String getCommentBody() {
		return commentBody;
	}

	public void setCommentBody(String comment) {
		this.commentBody = comment;
	}

	public Integer getMinutesSpent() {
		return minutesSpent;
	}

	public void setMinutesSpent(Integer minutesSpent) {
		this.minutesSpent = minutesSpent;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	@Override
	public boolean mediate(MessageContext synCtx) {
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		WorklogInput worklogInput = null;
		try {
			worklogInput = WorklogInput.create(new URI(getIssueUri()), getCommentBody(),
			                                   DateTimeFormat.forPattern(INTERNAL_DATETIME_FORMAT).parseDateTime(getStartDate()),
			                                   getMinutesSpent().intValue());
			client.getIssueClient().addWorklog(new URI(getWorklogUri()), worklogInput);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return true;
	}

}
