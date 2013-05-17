package org.wso2.carbon.transport.adaptor.core.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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


/**
 * This class is used to wrap the input transport adaptor configuration and output transport adaptor configuration
 */
public class InternalTransportAdaptorConfiguration {

    private Map<String, String> propertyList;

    public Map<String, String> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(Map<String, String> propertyList) {
        this.propertyList = propertyList;
    }

    public InternalTransportAdaptorConfiguration() {
        this.propertyList = new ConcurrentHashMap<String, String>();
    }

    public void addTransportAdaptorProperty(String name, String value) {
        this.propertyList.put(name, value);
    }

}
