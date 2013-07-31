package org.wso2.carbon.mediation.library.connectors.jira.issue;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.JiraMediator;

public class JiraCreateIssuesMediator extends JiraMediator {

	@Override
	public boolean mediate(MessageContext synCtx) {

		return true;
	}

}
