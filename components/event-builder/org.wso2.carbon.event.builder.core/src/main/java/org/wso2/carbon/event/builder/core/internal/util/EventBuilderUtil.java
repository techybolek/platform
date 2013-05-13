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

import edu.emory.mathcs.backport.java.util.Collections;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.event.builder.core.internal.TupleInputMapping;

import java.util.HashMap;
import java.util.Map;

public class EventBuilderUtil {
    public static final String META_DATA_PREFIX = "meta_";
    public static final String CORRELATION_DATA_PREFIX = "correlation_";

    public static final String FROM_SUFFIX = "_from";
    public static final String TO_SUFFIX = "_to";
    public static final String TYPE_SUFFIX = "_type";

    public static final Map<String, TupleInputMapping.InputDataType> STRING_INPUT_DATA_TYPE_MAP = Collections.unmodifiableMap(new HashMap<String, TupleInputMapping.InputDataType>() {{
        put(EventBuilderConfigurationSyntax.META_DATA_VAL, TupleInputMapping.InputDataType.META_DATA);
        put(EventBuilderConfigurationSyntax.CORRELATION_DATA_VAL, TupleInputMapping.InputDataType.CORRELATION_DATA);
        put(EventBuilderConfigurationSyntax.PAYLOAD_DATA_VAL, TupleInputMapping.InputDataType.PAYLOAD_DATA);
    }});

    public static final Map<String, AttributeType> STRING_ATTRIBUTE_TYPE_MAP = Collections.unmodifiableMap(new HashMap<String, AttributeType>() {{
        put(EventBuilderConfigurationSyntax.ATTR_TYPE_BOOL, AttributeType.BOOL);
        put(EventBuilderConfigurationSyntax.ATTR_TYPE_STRING, AttributeType.STRING);
        put(EventBuilderConfigurationSyntax.ATTR_TYPE_DOUBLE, AttributeType.DOUBLE);
        put(EventBuilderConfigurationSyntax.ATTR_TYPE_FLOAT, AttributeType.FLOAT);
        put(EventBuilderConfigurationSyntax.ATTR_TYPE_INTEGER, AttributeType.INT);
        put(EventBuilderConfigurationSyntax.ATTR_TYPE_LONG, AttributeType.LONG);
    }});

}
