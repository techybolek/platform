package org.wso2.carbon.transport.adaptor.core.message.config;/*
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
import java.util.concurrent.ConcurrentHashMap;

public class OutputTransportMessageConfiguration{


    /**
     * logical name use to identify the transport
     */
    private String transportName;


    /**
     * Map contains the output message property configuration details
     */
    private Map<String, String> outputMessageProperties;




    public OutputTransportMessageConfiguration() {
        this.outputMessageProperties = new ConcurrentHashMap<String, String>();
    }


    public void addOutputMessageProperty(String name, String value) {
        this.outputMessageProperties.put(name, value);
    }

    public String getTransportName() {
        return this.transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public Map<String, String> getOutputMessageProperties() {
        return outputMessageProperties;
    }

    public void setOutputMessageProperties(Map<String, String> outputMessageProperties) {
        this.outputMessageProperties = outputMessageProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OutputTransportMessageConfiguration)) return false;

        OutputTransportMessageConfiguration that = (OutputTransportMessageConfiguration) o;

        if (outputMessageProperties != null ? !outputMessageProperties.equals(that.outputMessageProperties) : that.outputMessageProperties != null)
            return false;
        if (transportName != null ? !transportName.equals(that.transportName) : that.transportName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = transportName != null ? transportName.hashCode() : 0;
        result = 31 * result + (outputMessageProperties != null ? outputMessageProperties.hashCode() : 0);
        return result;
    }
}
