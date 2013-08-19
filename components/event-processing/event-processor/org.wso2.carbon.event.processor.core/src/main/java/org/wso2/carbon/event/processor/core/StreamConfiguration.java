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

package org.wso2.carbon.event.processor.core;


import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;

public class StreamConfiguration {
    // name, version mapped to databridge - to interface with outside
    private String name;
    private String version;

    // for imported streams : as
    // for exported streams : valueOf
    private String siddhiStreamName;


    public StreamConfiguration(String name, String version) {
        this.name = name;
        this.version = version;
        this.siddhiStreamName = name;
    }

    public StreamConfiguration(String name) {
        this.name = name;
        this.version = "1.0.0";
        this.siddhiStreamName = name;
    }

    public StreamConfiguration(String name, String siddhiStreamName, String version) {
        this.name = name;
        this.siddhiStreamName = siddhiStreamName;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getStreamId() {
        return EventProcessorUtil.getStreamId(name, version);
    }

    public String getVersion() {
        return version;
    }

    public String getSiddhiStreamName() {
        return siddhiStreamName;
    }

    public void setSiddhiStreamName(String siddhiStreamName) {
        this.siddhiStreamName = siddhiStreamName;
    }
}
