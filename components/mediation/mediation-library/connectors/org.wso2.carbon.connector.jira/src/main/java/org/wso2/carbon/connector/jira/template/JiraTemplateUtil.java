package org.wso2.carbon.connector.jira.template;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.template.TemplateContext;
import org.wso2.carbon.connector.jira.JiraMediator;

import java.util.Stack;

import static org.wso2.carbon.connector.jira.JiraConstants.*;

public class JiraTemplateUtil {

    @SuppressWarnings("unchecked")
    public static String lookupFunctionParam(MessageContext context, String paramName) {
        Stack<TemplateContext> funcStack = (Stack<TemplateContext>) context.getProperty(SynapseConstants.SYNAPSE__FUNCTION__STACK);
        TemplateContext currentFuncHolder = funcStack.peek();
        return (String) currentFuncHolder.getParameterValue(paramName);
    }

    public static void fillAuthParams(MessageContext context, JiraMediator jiraMediator) {
        jiraMediator.setPassword((String) context.getProperty(CONTEXT_PASSWORD));
        jiraMediator.setUsername((String) context.getProperty(CONTEXT_USERNAME));
        jiraMediator.setUri((String) context.getProperty(CONTEXT_URI));
    }

}
