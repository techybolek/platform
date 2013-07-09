/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.dto;

import org.wso2.carbon.context.CarbonContext;

import java.util.Date;

/**
 *
 */
public class StatusHolder {

    public static final String TYPE_PUBLISH = "PUBLISH";

    public static final String TYPE_POLICY = "POLICY";

    /**
     * Status type
     */
    private String type;    
    /**
     * key to identify status. basically policy Id
     */
    private String key;
    
    /**
     * whether this is success status or not
     */
    private boolean success;

    /**
     * the user who is involved with this
     */
    private String user;

    /**
     * time instance
     */
    private String timeInstance;

    /**
     * message
     */
    private String message;

    public static final String STATUS_HOLDER_NAME = "status_holder";

    public StatusHolder(String type, String key, String message) {
        this.type = type;
        this.key = key;
        this.user = CarbonContext.getCurrentContext().getUsername();
        this.message = message;
        this.success = false;
        this.timeInstance = (new Date()).toString();
    }

    public StatusHolder(String type, String key) {
        this.type = type;
        this.key = key;
        this.user = CarbonContext.getCurrentContext().getUsername();
        this.success = true;
        this.timeInstance = (new Date()).toString();
    }

    public StatusHolder(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTimeInstance() {
        return timeInstance;
    }

    public void setTimeInstance(String timeInstance) {
        this.timeInstance = timeInstance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
