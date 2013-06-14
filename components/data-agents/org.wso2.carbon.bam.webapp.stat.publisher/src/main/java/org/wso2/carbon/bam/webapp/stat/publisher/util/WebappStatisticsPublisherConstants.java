/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.bam.webapp.stat.publisher.util;

import org.wso2.carbon.core.RegistryResources;

public class WebappStatisticsPublisherConstants {

    public static final String RESPONSE_TIME = "response_time";
    public static final String REQUEST_COUNT = "request_count";
    public static final String RESPONSE_COUNT = "response_count";
    public static final String FAULT_COUNT = "fault_count";

    public  static final String BAM_CONFIG_PROPERTIES_FILE = "webappStat.properties";
    public static final String BAM_SERVICE_PUBLISH_PROPERTY = "WebappDataPublishing";
    public static final String BAM_SERVICE_PUBLISH_ENABLED = "enable";

    public static final String CLOUD_DEPLOYMENT_PROP = "IsCloudDeployment";

    public static final String SERVER_CONFIG_BAM_URL = "BamServerURL";

    public static final String DEFAULT_BAM_SERVER_URL = "tcp://127.0.0.1:7611";

    // Registry persistence related constants
    public static final String WEBAPP_STATISTICS_REG_PATH = RegistryResources.COMPONENTS
            + "org.wso2.carbon.bam.webapp.stat.publisher/webapp_stats/";
    public static final String ENABLE_WEBAPP_STATS_EVENTING = "EnableWebappStats";

    public static final String WEBAPP_COMMON_REG_PATH = RegistryResources.COMPONENTS
            + "org.wso2.carbon.bam.webapp.stat.publisher/common/";

    public static final String WEBAPP_PROPERTIES_REG_PATH = RegistryResources.COMPONENTS
            + "org.wso2.carbon.bam.webapp.stat.publisher/properties";

}
