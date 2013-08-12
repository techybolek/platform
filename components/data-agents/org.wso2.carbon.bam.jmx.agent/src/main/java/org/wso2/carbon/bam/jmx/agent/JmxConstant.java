/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.bam.jmx.agent;


public interface JmxConstant {

    public static class JmxTaskConstant {

        public static final String JMX_SERVICE_TASK_TYPE = "JMX_SERVICE_TASK";
        public static final char FORWARD_SLASH = '/';
        public static final String STREAM_NAME_PREFIX = "jmx.agent.";
        public static String JMX_PROFILE_NAME = "JMX_PROFILE_NAME";
    }

    public static class JmxConfigurationConstant{

        public static final String JMX_REMOTE_CREDENTIALS_STR = "jmx.remote.credentials";

        public static final String REG_LOCATION =  "repository/components/org.wso2.carbon.publish.jmx.agent/";

        public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    }

    public static class JmxDefaultMBeanConstant {

        public static final String NULL = "NULL";
        public static final String EXTERNAL_EVENT = "externalEvent";
    }
}
