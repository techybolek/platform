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

package org.wso2.carbon.transport.adaptor.manager.core.internal.util;

public final class TransportAdaptorManagerConstants {

    private TransportAdaptorManagerConstants(){}

    public static final String TM_CONF_NS = "http://wso2.org/carbon/transportadaptormanager";
    public static final String TM_ELE_ROOT_ELEMENT = "transportAdaptor";
    public static final String TM_ELE_DIRECTORY = "transportadaptors";

    public static final String TM_ELE_INPUT_PROPERTY = "input";
    public static final String TM_ELE_OUTPUT_PROPERTY = "output";
    public static final String TM_ELE_PROPERTY = "property";
    public static final String TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX = "ta";

    public static final String TM_ATTR_NAME = "name";
    public static final String TM_ATTR_TYPE = "type";
}