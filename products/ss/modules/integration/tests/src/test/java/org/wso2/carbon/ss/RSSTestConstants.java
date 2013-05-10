/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.ss;

import java.io.File;

public class RSSTestConstants {

    public static final class EnvironmentConfigurations {
        private EnvironmentConfigurations() {
            throw new AssertionError();
        }

        public static final String RSS_CONFIG_PATH = "repository" + File.separator + "conf" +
                File.separator + "etc" + File.separator + "rss-config.xml";
        public static final String RSS_ADMIN_SERVICE = "RSSAdmin";
        public static final String NDATASOURCE_ADMIN_SERVICE = "NDataSourceAdminService";
        public static final String RSS_MANAGEMENT_MODE_SYSTEM = "WSO2_RSS";
        public static final String RSS_MANAGEMENT_MODE_USER = "User";
        public static final String DATABASE_TYPE_MYSQL = "mysql";
        public static final String RSS_INSTANCE_SERVER_CATEGORY_LOCAL = "LOCAL";
        public static final String RSS_INSTANCE_SERVER_CATEGORY_RDS = "RDS";
    }

    public static final class SampleConfigurations {
        private SampleConfigurations() {
            throw new AssertionError();
        }

        public static final String SAMPLE_DATABASE_NAME = "test_db2";
        public static final String SAMPLE_DATABASE_USERNAME = "db_usr1";
        public static final String SAMPLE_DATABASE_USER_PASSWORD = "password";
        public static final String SAMPLE_PRIVILEGE_TEMPLATE_NAME = "testTemplate";

        public static final String SAMPLE_RSS_INSTANCE_NAME = "testRssInstance";
        public static final String SAMPLE_RSS_INSTANCE_URL = "jdbc:mysql://localhost:3306";
        public static final String SAMPLE_RSS_INSTANCE_ADMIN_USERNAME = "root";
        public static final String SAMPLE_RSS_INSTANCE_ADMIN_PASSWORD = "root";

        public static final String SAMPLE_RSS_INSTANCE_MODIFIED_URL = "jdbc:mysql://localhost:3306";
        public static final String SAMPLE_RSS_INSTANCE_ADMIN_MODIFIED_USERNAME = "rootmod";
        public static final String SAMPLE_RSS_INSTANCE_ADMIN_MODIFIED_PASSWORD = "rootmod";
    }

}
