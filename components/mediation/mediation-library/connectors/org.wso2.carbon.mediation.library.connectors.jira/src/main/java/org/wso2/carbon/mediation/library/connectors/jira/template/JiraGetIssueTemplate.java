package org.wso2.carbon.mediation.library.connectors.jira.template;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.jira.JiraGetIssueMediator;

import static org.wso2.carbon.mediation.library.connectors.jira.JiraConstants.FUNC_ISSUE_ID;
import static org.wso2.carbon.mediation.library.connectors.jira.template.JiraTemplateUtil.fillAuthParams;
import static org.wso2.carbon.mediation.library.connectors.jira.template.JiraTemplateUtil.lookupFunctionParam;

public class JiraGetIssueTemplate extends JiraGetIssueMediator {

    @Override
    public boolean mediate(MessageContext synCtx) {
        fillAuthParams(synCtx, this);
        setIssueId(lookupFunctionParam(synCtx, FUNC_ISSUE_ID));
        return super.mediate(synCtx);
    }

}
