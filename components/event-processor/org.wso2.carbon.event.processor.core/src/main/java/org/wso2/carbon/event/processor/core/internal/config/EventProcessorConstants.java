/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.config;


public interface EventProcessorConstants {

    String EP_CONF_NS = "http://wso2.org/carbon/eventprocessor";

    String EP_ELE_CONF_ADAPTOR_NAME_SPACE_PREFIX = "adaptor";
    String EP_ELE_ROOT_ELEMENT = "queryPlan";

    String EP_ELE_DESC  = "description";
    String EP_ELE_SIDDHI_CONFIG = "siddhiConfiguration";
    String EP_ELE_IMP_STREAMS = "importedStreams";
    String EP_ELE_EXP_STREAMS = "exportedStreams";
    String EP_ELE_QUERIES   = "queryExpressions";
    String EP_ELE_STREAM = "stream";
    String EP_ELE_PROPERTY = "property";

    String EP_ATTR_AS  = "as";
    String EP_ATTR_NAME = "name";

}
