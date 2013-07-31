package org.wso2.carbon.connector.jira.issue;

import static org.wso2.carbon.connector.jira.JiraConstants.CONTEXT_ISSUE;
import static org.wso2.carbon.connector.jira.JiraConstants.CONTEXT_TRANSITION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.synapse.MessageContext;
import org.joda.time.DateTime;
import org.wso2.carbon.connector.jira.JiraMediator;
import org.wso2.carbon.connector.jira.util.JiraMediatorUtil;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;

public class JiraTransitionMediator extends JiraMediator {
	private String comment;
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
    public boolean mediate(MessageContext synCtx) {
		JiraRestClient client = JiraMediatorUtil.getClient(getUri(), getUsername(), getPassword());
		Issue issue = (Issue)synCtx.getProperty(CONTEXT_ISSUE);
		Transition transition = (Transition)synCtx.getProperty(CONTEXT_TRANSITION);

		List<FieldInput> transitionIssueFields = new ArrayList<FieldInput>();
		CollectionUtils.addAll(transitionIssueFields, transition.getFields().iterator());
		User user = client.getUserClient().getUser(getUsername()).claim();
		Comment comment = new Comment(issue.getCommentsUri(), getComment(), user, null, new DateTime(), new DateTime(), new Visibility(Visibility.Type.GROUP , "Group visibility"), null);
		TransitionInput transitionInput = new TransitionInput(transition.getId(), transitionIssueFields, comment);
		client.getIssueClient().transition(issue.getSelf(), transitionInput).claim();
		
	    return true;
    }

}
