/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement;

import java.util.Map;
import java.util.Properties;

/**
 * This listener would be fired after an admin action is done
 */
public interface EntitlementNotificationHandler {

    /**
     * init entitlement notification module
     *
     * @param properties properties
     */
    public void init(Properties properties);

    /**
     * Handles the notifications
     *
     * @param action Admin action that is performed
     * @param data  <code>Map</code> data that is passed
     * @param result Results of the admin service,  whether it is success or not
     * @param message If it is not success, error message or any other message
     */
    public void handle(String action, Map<String, String> data, boolean result, String message);
}
