package org.wso2.carbon.connector.jira.issue;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.JiraMediator;

public class JiraAddAttachmentsMediator extends JiraMediator {

	@Override
    public boolean mediate(MessageContext synCtx) {

	    return true;
    }

}
