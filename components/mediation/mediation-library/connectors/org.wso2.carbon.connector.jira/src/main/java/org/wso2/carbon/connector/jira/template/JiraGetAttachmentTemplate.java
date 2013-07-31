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

import static org.wso2.carbon.connector.jira.JiraConstants.FUNC_ATTACHMENT_URI;
import static org.wso2.carbon.connector.jira.template.JiraTemplateUtil.fillAuthParams;
import static org.wso2.carbon.connector.jira.template.JiraTemplateUtil.lookupFunctionParam;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.jira.issue.JiraGetAttachmentMediator;

/**
 *
 */
public class JiraGetAttachmentTemplate extends JiraGetAttachmentMediator {
	@Override
	public boolean mediate(MessageContext synCtx) {
		fillAuthParams(synCtx, this);
		setAttachmentUri(lookupFunctionParam(synCtx, FUNC_ATTACHMENT_URI));
		
	    return super.mediate(synCtx);
	}
}
