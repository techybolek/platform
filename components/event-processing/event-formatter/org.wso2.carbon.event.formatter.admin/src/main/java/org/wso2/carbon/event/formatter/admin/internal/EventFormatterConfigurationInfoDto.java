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

package org.wso2.carbon.event.formatter.admin.internal;

/**
 * Event Builder Configuration Details are stored in this class
 */

public class EventFormatterConfigurationInfoDto {

    private String EventFormatterName;

    private String MappingType;

    private String outTransportAdaptorName;

    private String inputStreamId;

    private boolean enableTracing;

    private boolean enableStats;


    public String getEventFormatterName() {
        return EventFormatterName;
    }

    public void setEventFormatterName(String eventFormatterName) {
        EventFormatterName = eventFormatterName;
    }

    public String getMappingType() {
        return MappingType;
    }

    public void setMappingType(String mappingType) {
        MappingType = mappingType;
    }

    public String getOutTransportAdaptorName() {
        return outTransportAdaptorName;
    }

    public void setOutTransportAdaptorName(String outTransportAdaptorName) {
        this.outTransportAdaptorName = outTransportAdaptorName;
    }

    public String getInputStreamId() {
        return inputStreamId;
    }

    public void setInputStreamId(String inputStreamId) {
        this.inputStreamId = inputStreamId;
    }

    public boolean isEnableTracing() {
        return enableTracing;
    }

    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public void setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
    }
}
