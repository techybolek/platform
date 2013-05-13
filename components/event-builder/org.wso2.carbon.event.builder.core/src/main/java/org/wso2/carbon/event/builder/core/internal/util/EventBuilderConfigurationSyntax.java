/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal.util;

public class EventBuilderConfigurationSyntax {

    public static final String EB_CONF_NS = "http://wso2.org/carbon/eventbuilder";
    public static final String EB_ELEMENT_ROOT_ELEMENT = "eventBuilder";
    public static final String EB_CONFIG_DIRECTORY = "eventbuilders";

    public static final String EB_ELEMENT_PROPERTY = "property";
    public static final String EB_ELEMENT_TRANSPORT_TYPE = "transportType";
    public static final String EB_ELEMENT_FROM = "from";
    public static final String EB_ELEMENT_MAPPING = "mapping";
    public static final String EB_ELEMENT_TO="to";

    public static final String EB_ELEMENT_CONF_EB_NS_PREFIX = "builder";

    public static final String EB_ATTR_NAME = "name";
    public static final String EB_ATTR_TYPE = "type";
    public static final String EB_ATTR_TA_NAME = "transportAdaptorName";
    public static final String EB_ATTR_TA_TYPE = "transportAdaptorType";
    public static final String EB_ATTR_STREAM_NAME = "streamName";
    public static final String EB_ATTR_VERSION="version";

    public static final String META_DATA_VAL = "metadata";
    public static final String CORRELATION_DATA_VAL = "correlation";
    public static final String PAYLOAD_DATA_VAL = "payload";

    public static final String ATTR_TYPE_FLOAT = "float";
    public static final String ATTR_TYPE_DOUBLE = "double";
    public static final String ATTR_TYPE_INTEGER = "integer";
    public static final String ATTR_TYPE_LONG = "long";
    public static final String ATTR_TYPE_STRING = "string";
    public static final String ATTR_TYPE_BOOL = "boolean";
}