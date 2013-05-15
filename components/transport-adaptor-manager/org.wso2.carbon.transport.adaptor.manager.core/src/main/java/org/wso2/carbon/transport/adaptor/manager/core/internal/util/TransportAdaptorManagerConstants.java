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

public interface TransportAdaptorManagerConstants {

    String TM_CONF_NS = "http://wso2.org/carbon/transportmanager";
    String TM_ELE_ROOT_ELEMENT = "transportAdaptor";
    String TM_ELE_DIRECTORY = "transportadaptors";

    String TM_ELE_INPUT_PROPERTY = "input";
    String TM_ELE_OUTPUT_PROPERTY = "output";
    String TM_ELE_COMMON_PROPERTY = "commonproperty";
    String TM_ELE_PROPERTY = "property";
    String TM_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX = "adaptor";

    String TM_ATTR_NAME = "name";
    String TM_ATTR_TYPE = "type";
}