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

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="auth" uri="tld/identity-authoization.tld"%>

<script type="text/javascript" src="js/util.js"></script>
<script type="text/javascript" src="js/cust_permissions.js"></script>

<div id="middle">
	<h2>Custom Permissions</h2>
	<h3>Add Permissions</h3>
	<div id="workArea" style="padding-bottom: 70px; margin-top: 10px;">

		<auth:module config="<%=config%>" request="<%=request%>" action="1"></auth:module>
		<form id="addForm" action="add-permissions-controller.jsp"
			method="get">
			<input type="hidden" name="op" value="add" />
			<table class="styledLeft" style="width: 65%; clear: both;">
				<thead>
					<tr>
						<th>Module</th>
						<th>Resource</th>
						<th>Action</th>
						<th>Role</th>
						<th></th>
					</tr>
				</thead>
				<tbody>

					<tr id="permRow_1">
						<td><select id="permModule_1" name="permModule_1">
								<c:forEach items="${modules }" var="module">

									<option id="${module.moduleId }"
										value="${module.moduleId },${module.moduleName}">${module.moduleName
										}</option>
								</c:forEach>
						</select></td>
						<td><input type="text" id="permResource_1"
							name="permResource_1"></td>
						<td><input type="text" id="permAction_1" name="permAction_1"></td>
						<td><input type="text" id="permRole_1" name="permRole_1"></td>
						<td id="permAddMore_1"><a id="addMorePermissions_1"
							style="background-image: url(../admin/images/add.gif);"
							class="icon-link" href="javascript:void(0);">&nbsp;</a> <a
							id="removeThisPermissions_1"
							style="background-image: url(../admin/images/delete.gif); display: none;"
							class="icon-link" href="javascript:void(0);">&nbsp;</a></td>
					</tr>
					<tr id="buttonPanel">
						<td colspan="5" class="buttonRow"><input type="button"
							onclick="finishAdding();" value="Finish" class="button">
							<input type="button" onclick="cancellAdding();" value="Cancel"
							class="button"></td>
					</tr>
				</tbody>
			</table>
		</form>
	</div>
</div>