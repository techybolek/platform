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

package org.wso2.carbon.mediation.library.connectors.jira;

public class JiraConstants {

	// names of the context variables. for getting only.
	public static final String CONTEXT_PASSWORD = "jira.password";
	public static final String CONTEXT_USERNAME = "jira.username";
	public static final String CONTEXT_URI = "jira.uri";

	// names of the context variables. for setting and getting.
	public static final String CONTEXT_CLIENT = "jira.client";
	public static final String CONTEXT_BASIC_ISSUE = "jira.basicIssue";
	public static final String CONTEXT_ISSUE = "jira.issue";
	public static final String CONTEXT_SESSION = "jira.session";
	public static final String CONTEXT_PROJECT = "jira.project";
	public static final String CONTEXT_PROJECTS = "jira.projects";
	public static final String CONTEXT_ROLE = "jira.projectRole";
	public static final String CONTEXT_ROLES = "jira.projectRoles";
	public static final String CONTEXT_SEARCH_RESULT = "jira.searchResult";
	public static final String CONTEXT_USER = "jira.user";
	public static final String CONTEXT_TRANSITIONS = "jira.issue.transitions";
	public static final String CONTEXT_TRANSITION = "jira.issue.transition";
	public static final String CONTEXT_VOTES = "jira.issue.votes";
	public static final String CONTEXT_WATCHERS = "jira.issue.watchers";
	public static final String CONTEXT_ATTACHMENT_ISTREAM = "jira.issue.attachment.istream";

	// names of the function parameters.
	public static final String FUNC_ISSUE_ID = "jira.issueId";
	public static final String FUNC_JQL_QUERY = "jira.jqlQuery";
	public static final String FUNC_PROJECT_KEY = "jira.projectKey";
	public static final String FUNC_COMMENT_BODY = "jira.commentBody";
	public static final String FUNC_WATCHER_USERNAME = "jira.watcherUsername";
	public static final String FUNC_WATCHERS_URI = "jira.watcherUri";
	public static final String FUNC_PROJECT_URI = "jira.projectUri";	
	public static final String FUNC_ISSUE_URI = "jira.issueUri";
	public static final String FUNC_ROLE_URI = "jira.roleUri";	
	public static final String FUNC_COMMENTS_URI = "jira.commentsUri";
	public static final String FUNC_WORKLOG_URI = "jira.worklogUri";
	public static final String FUNC_MINUTES_SPENT = "jira.worklog.minutesSpent";
	public static final String FUNC_VISIBILITY = "jira.visibility";
	public static final String FUNC_START_DATE = "jira.worklog.startDate";
	public static final String FUNC_ATTACHMENT_URI = "jira.issue.attachmentUri";
	public static final String FUNC_REQD_USERNAME = "jira.requiredUsername";
	public static final String FUNC_ISSUE_FIELD_LIST = "jira.issueFields";	
	
	// constants internal to the class mediators
	public static final String INTERNAL_DATETIME_FORMAT = "dd/MM/yyyy";

}
