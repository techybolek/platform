package org.wso2.carbon.connector.jira.template;

import static org.wso2.carbon.connector.jira.template.JiraTemplateUtil.fillAuthParams;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.issue.JiraAddAttachmentsMediator;

public class JiraAddAttachmentsTemplate extends JiraAddAttachmentsMediator {

	@Override
	public boolean mediate(MessageContext synCtx) {
        fillAuthParams(synCtx, this);

        return super.mediate(synCtx);
	}
}
