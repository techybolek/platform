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

package org.wso2.carbon.event.builder.admin.internal.util;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.internal.config.DeploymentStatus;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EventBuilderAdminConstants {
    public static final String META_DATA_PREFIX = "meta_";
    public static final String CORRELATION_DATA_PREFIX = "correlation_";
    public static final String XPATH_PREFIX_NS_PREFIX = "prefix_";
    public static final String FROM_SUFFIX = "_from";
    public static final String TO_SUFFIX = "_to";
    public static final String MAPPING_SUFFIX = "_mapping";
    public static final String STATUS_DEPLOYED = "DEPLOYED";
    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_WAITING_FOR_DEPENDENCY = "WAITING FOR DEPENDENCY";
    public static final String JAVA_LANG_PACKAGE_PREFIX = "java.lang.";

    public static final Map<DeploymentStatus, String> DEP_STATUS_MAP = Collections.unmodifiableMap(new HashMap<DeploymentStatus, String>() {{
        put(DeploymentStatus.DEPLOYED, STATUS_DEPLOYED);
        put(DeploymentStatus.WAITING_FOR_DEPENDENCY, STATUS_WAITING_FOR_DEPENDENCY);
        put(DeploymentStatus.ERROR, STATUS_ERROR);
    }});
    public static final Map<AttributeType, String> ATTRIBUTE_TYPE_STRING_MAP = Collections.unmodifiableMap(new HashMap<AttributeType, String>() {{
        put(AttributeType.BOOL, EventBuilderConstants.CLASS_FOR_BOOLEAN);
        put(AttributeType.STRING, EventBuilderConstants.CLASS_FOR_STRING);
        put(AttributeType.DOUBLE, EventBuilderConstants.CLASS_FOR_DOUBLE);
        put(AttributeType.FLOAT, EventBuilderConstants.CLASS_FOR_FLOAT);
        put(AttributeType.INT, EventBuilderConstants.CLASS_FOR_INTEGER);
        put(AttributeType.LONG, EventBuilderConstants.CLASS_FOR_LONG);
    }});


}
