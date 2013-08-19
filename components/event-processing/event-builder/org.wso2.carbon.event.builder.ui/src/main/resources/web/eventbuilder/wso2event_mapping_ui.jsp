<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
  ~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  --%>

<fmt:bundle basename="org.wso2.carbon.event.builder.ui.i18n.Resources">
    <link type="text/css" href="css/cep.css" rel="stylesheet"/>
    <script type="text/javascript" src="js/event_builders.js"></script>

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr fromElementKey="inputWso2EventMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="event.builder.mapping.wso2event"/>
            </td>
        </tr>
        <tr fromElementKey="inputWso2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.meta"/></h6>
                <table class="styledLeft noBorders spacer-bot" id="inputMetaDataTable"
                       style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.name"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.valueof"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.type"/></th>
                    <th><fmt:message key="event.builder.mapping.actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noInputMetaData">
                    No Meta Data properties Defined
                </div>
                <table id="addMetaDataTable" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="event.builder.property.name"/> :
                        </td>
                        <td>
                            <input type="text" id="inputMetaDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.builder.property.valueof"/> :
                        </td>
                        <td>
                            <input type="text" id="inputMetaDataPropValueOf"/>
                        </td>
                        <td><fmt:message key="event.builder.property.type"/>:
                            <select id="inputMetaDataPropType">
                                <option value="java.lang.Integer">Integer</option>
                                <option value="java.lang.Long">Long</option>
                                <option value="java.lang.Double">Double</option>
                                <option value="java.lang.Float">Float</option>
                                <option value="java.lang.String">String</option>
                                <option value="java.lang.Boolean">Boolean</option>
                            </select>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputWso2EventProperty('Meta')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>


        <tr fromElementKey="inputWso2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.correlation"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="inputCorrelationDataTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.name"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.valueof"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.type"/></th>
                    <th><fmt:message key="event.builder.mapping.actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noInputCorrelationData">
                    No Correlation Data properties Defined
                </div>
                <table id="addCorrelationDataTable" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="event.builder.property.name"/> :
                        </td>
                        <td>
                            <input type="text" id="inputCorrelationDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.builder.property.valueof"/> :
                        </td>
                        <td>
                            <input type="text" id="inputCorrelationDataPropValueOf"/>
                        </td>
                        <td><fmt:message key="event.builder.property.type"/>:
                            <select id="inputCorrelationDataPropType">
                                <option value="java.lang.Integer">Integer</option>
                                <option value="java.lang.Long">Long</option>
                                <option value="java.lang.Double">Double</option>
                                <option value="java.lang.Float">Float</option>
                                <option value="java.lang.String">String</option>
                                <option value="java.lang.Boolean">Boolean</option>
                            </select>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputWso2EventProperty('Correlation')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
        <tr fromElementKey="inputWso2EventMapping">
            <td colspan="2">

                <h6><fmt:message key="property.data.type.payload"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="inputPayloadDataTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.name"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.valueof"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.type"/></th>
                    <th><fmt:message key="event.builder.mapping.actions"/></th>
                    </thead>
                </table>
                <div class="noDataDiv-plain" id="noInputPayloadData">
                    No Payload Data properties Defined
                </div>
                <table id="addPayloadDataTable" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message key="event.builder.property.name"/> :
                        </td>
                        <td>
                            <input type="text" id="inputPayloadDataPropName"/>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.builder.property.valueof"/> :
                        </td>
                        <td>
                            <input type="text" id="inputPayloadDataPropValueOf"/>
                        </td>
                        <td><fmt:message key="event.builder.property.type"/>:
                            <select id="inputPayloadDataPropType">
                                <option value="java.lang.Integer">Integer</option>
                                <option value="java.lang.Long">Long</option>
                                <option value="java.lang.Double">Double</option>
                                <option value="java.lang.Float">Float</option>
                                <option value="java.lang.String">String</option>
                                <option value="java.lang.Boolean">Boolean</option>
                            </select>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputWso2EventProperty('Payload')"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</fmt:bundle>