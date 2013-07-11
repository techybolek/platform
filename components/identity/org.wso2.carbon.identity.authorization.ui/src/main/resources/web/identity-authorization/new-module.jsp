<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->

<%@ page isELIgnored="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="auth" uri="tld/identity-authoization.tld"%>

<script type="text/javascript" src="js/util.js"></script>
<script type="text/javascript" src="js/cust_permissions.js"></script>

<div id="middle">
	<h2>Custom Permissions</h2>
	<h3>Add New Application</h3>
	<div id="workArea" style="padding-bottom: 70px; margin-top: 10px;">

		<form id="moduleNewForm"
			action="/carbon/identity-authorization/new-module-controller.jsp"
			method="post">

			<input type="hidden" value="0" id="numberOfActions"
				name="numberOfActions" />
			<table class="styledLeft" style="width: 30%; clear: both;">
				<thead>
					<tr>
						<th colspan="1">Module Name</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td><input type="text" name="moduleName"></td>
					</tr>
				</tbody>
			</table>
			<table class="styledLeft" style="width: 30%; clear: both; margin-top: 10px;">
				<thead>
					<tr>
						<th colspan="1">Actions</th>
					</tr>
				</thead>
				<tbody>
					<tr id="newActionRow_0">
						<td><input type="text" name="newAction_0" /></td>
					</tr>

					<tr id="buttonPanel">
						<td colspan="2"><input type="submit" value="Finish"
							class="button" /> <input type="button"
							onclick="cancellAdding();" value="Cancel" class="button" /> <a
							id="addMoreAction_1"
							style="background-image: url(../admin/images/add.gif);"
							class="icon-link" href="javascript:void(0);">Add New Action</a></td>
					</tr>
				</tbody>
			</table>
		</form>
	</div>
</div>
