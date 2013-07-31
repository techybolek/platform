package org.wso2.carbon.mediation.library.connectors.jira.template;

import static org.wso2.carbon.mediation.library.connectors.jira.template.JiraTemplateUtil.fillAuthParams;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.issue.JiraAddAttachmentsMediator;

public class JiraAddAttachmentsTemplate extends JiraAddAttachmentsMediator {

	@Override
	public boolean mediate(MessageContext synCtx) {
        fillAuthParams(synCtx, this);

        return super.mediate(synCtx);
	}
}
