/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.startup.publisher;

public class APIStartupPublisherConstants {
	
	/*API Management configuration in for startup publishing APIs */
    
	public static final String API_STARTUP_PUBLISHER = "StartupAPIPublisher.";
	
	public static final String API_STARTUP_PUBLISHER_ENABLED = API_STARTUP_PUBLISHER + "Enabled";
        
    public static final String API_STARTUP_PUBLISHER_LOCAL_API = API_STARTUP_PUBLISHER + "LocalAPI.";

    public static final String API_STARTUP_PUBLISHER_API_LOCAL_PROVIDER = API_STARTUP_PUBLISHER_LOCAL_API + "Provider";
    
    public static final String API_STARTUP_PUBLISHER_API_LOCAL_VERSION = API_STARTUP_PUBLISHER_LOCAL_API + "Version";
    
    public static final String API_STARTUP_PUBLISHER_API_LOCAL_CONTEXT = API_STARTUP_PUBLISHER_LOCAL_API + "Context";
    
    public static final String API_STARTUP_PUBLISHER_API = API_STARTUP_PUBLISHER + "API.";

    public static final String API_STARTUP_PUBLISHER_API_PROVIDER = API_STARTUP_PUBLISHER_API + "Provider";
    
    public static final String API_STARTUP_PUBLISHER_API_VERSION = API_STARTUP_PUBLISHER_API + "Version";
    
    public static final String API_STARTUP_PUBLISHER_API_CONTEXT = API_STARTUP_PUBLISHER_API + "Context";
    
    public static final String API_STARTUP_PUBLISHER_API_ENDPOINT = API_STARTUP_PUBLISHER_API + "Endpoint";
}
