/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.connector.jira.template;

import org.wso2.carbon.connector.jira.JiraConstants;
import org.wso2.carbon.connector.jira.issue.JiraAddWorkLogMediator;

import static org.wso2.carbon.connector.jira.template.JiraTemplateUtil.fillAuthParams;
import static org.wso2.carbon.connector.jira.template.JiraTemplateUtil.lookupFunctionParam;

import org.apache.synapse.MessageContext;

/**
 *
 */
public class JiraAddWorkLogTemplate extends JiraAddWorkLogMediator {
	@Override
	public boolean mediate(MessageContext synCtx) {
		fillAuthParams(synCtx, this);		
		setIssueUri(lookupFunctionParam(synCtx, JiraConstants.FUNC_ISSUE_URI));
		setCommentBody(lookupFunctionParam(synCtx, JiraConstants.FUNC_COMMENT_BODY));
		setMinutesSpent(Integer.parseInt(lookupFunctionParam(synCtx, JiraConstants.FUNC_MINUTES_SPENT)));
		setVisibility(lookupFunctionParam(synCtx, JiraConstants.FUNC_VISIBILITY));
		setWorklogUri(lookupFunctionParam(synCtx, JiraConstants.FUNC_WORKLOG_URI));
		setStartDate(lookupFunctionParam(synCtx, JiraConstants.FUNC_START_DATE));
		
		return super.mediate(synCtx);
	}
}
