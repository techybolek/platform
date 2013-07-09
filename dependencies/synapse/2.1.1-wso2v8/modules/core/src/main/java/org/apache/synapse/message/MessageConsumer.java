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

package org.apache.synapse.message;

public interface MessageConsumer {
    /**
     * Cleans up this consumer
     * @return
     */
    public boolean cleanup();

    /**
     * Returns the actual message consumer.
     * @return
     */
    public Object getConsumer();

    /**
     * Returns the type of the message consumer.
     * @return
     */
    public int getConsumerType();

    /**
     * Sets the name of the message processor to which this consumer belongs.
     * @param processor
     */
    public void setProcessorName(String processor);

    /**
     * Returns the name of the message processor.
     * @return
     */
    public String processorName();

    /**
     * Sets the name of the message store to which the message processor of this consumer belongs.
     * @param store
     */
    public void setStoreName(String store);

    /**
     * Returns the name of the message store.
     * @return
     */
    public String storeName();

    /**
     * Indicates if this message consumer has encountered a connection error to the message store.
     * @return
     */
    public boolean isConnectionError();
}
