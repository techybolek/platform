/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.message.tracer.api.internal.conf;

public class EventStreamDef {

    public static final String STREAM_NAME = "BAM_MESSAGE_TRACE";

    public static final String VERSION = "1.0.0";

    public static final String NICK_NAME = "MessageTracerAgent";

    public static final String DESCRIPTION = "Publish Message Tracing Event";

    public String getStreamName() {
        return STREAM_NAME;
    }

    public String getVersion() {
        return VERSION;
    }

    public String getNickName() {
        return NICK_NAME;
    }

    public String getDescription() {
        return DESCRIPTION;
    }
}
