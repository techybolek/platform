/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.output.transport.adaptor.manager.core.internal.util;

public final class OutputTransportAdaptorManagerConstants {

    private OutputTransportAdaptorManagerConstants() {
    }

    public static final String TM_CONF_NS = "http://wso2.org/carbon/transportadaptormanager";
    public static final String TM_ELE_ROOT_ELEMENT = "outputtransportAdaptor";
    public static final String TM_ELE_DIRECTORY = "outputtransportadaptors";

    public static final String TM_ELE_PROPERTY = "property";

    public static final String TM_ATTR_NAME = "name";
    public static final String TM_ATTR_TYPE = "type";

    public static final String TM_ATTR_TRACING = "trace";
    public static final String TM_ATTR_STATISTICS = "statistics";
    public static final String TM_VALUE_ENABLE ="enable";
    public static final String TM_VALUE_DISABLE ="disable";
}