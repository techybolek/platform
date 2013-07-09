/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.message.store.persistence.jms;

import org.quartz.JobDataMap;

public class HandlerContext {
    private MessageProcessingHandler owner;
    private int maxAttempts = -1;
    private JobDataMap properties;

    public HandlerContext(MessageProcessingHandler owner, int maxAttempts, JobDataMap properties) {
        this.owner = owner;
        this.maxAttempts = maxAttempts;
        this.properties = properties;
    }

    public MessageProcessingHandler getOwner() {
        return owner;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public JobDataMap getProperties() {
        return properties;
    }
}
