package org.wso2.carbon.transport.adaptor.core.config;/*
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

import java.util.Map;

public interface InputTransportAdaptorConfiguration {

    public String getName();

    public void setName(String name);

    public String getType();

    public void setType(String type);

    public void setCommonAdaptorProperties(Map<String, String> commonAdaptorProperties);

    public Map<String, String> getCommonAdaptorProperties();

    public Map<String, String> getInputAdaptorProperties();

    public void setInputAdaptorProperties(Map<String, String> inputAdaptorProperties);

    public void addCommonAdaptorProperty(String name, String value);

    public void addInputAdaptorProperty(String name, String value);

}
