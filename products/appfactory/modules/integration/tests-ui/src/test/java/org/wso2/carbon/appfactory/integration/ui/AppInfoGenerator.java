/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.appfactory.integration.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppInfoGenerator {
    private static AppInfoGenerator instance = new AppInfoGenerator();
    private ArrayList<ApplicationInfo> arrayList = new ArrayList<ApplicationInfo>();
    private Map<String, ApplicationInfo> applicationInfoMap = new HashMap<String, ApplicationInfo>();
    private static final int numberOfApplications = 5;

    private AppInfoGenerator() {
        for (int i = 1; i <= numberOfApplications; i++) {
            ApplicationInfo applicationInfo =
                    new ApplicationInfo(generateAppKey(), generateAppName(), generateAppDB());
//            arrayList.add(applicationInfo);
            applicationInfoMap.put("appId" + i, applicationInfo);
        }
    }

    public static AppInfoGenerator getInstance() {
        return instance;
    }

    public Map<String, ApplicationInfo> getApplicationInfoMap() {
        return applicationInfoMap;
    }

    public ApplicationInfo getAppDetailsByKey(String key) {
        for (ApplicationInfo anArrayList : arrayList) {
            if (anArrayList.getAppKey().equals(key)) {
                return anArrayList;
            }
        }
        return null;
    }

    public ApplicationInfo getAppDetailsByName(String name) {
        for (ApplicationInfo anArrayList : arrayList) {
            if (anArrayList.getAppKey().equals(name)) {
                return anArrayList;
            }
        }
        return null;
    }

    private String generateAppName() {
        String applicationName = "wso2app";
        int value = (int) (Math.random() * 999);
        String randomNumber = Integer.toString(value);
        return applicationName + randomNumber;
    }

    private String generateAppKey() {
        String applicationKey = "wso2key";
        int value = (int) (Math.random() * 999);
        String randomNumber = Integer.toString(value);
        return applicationKey + randomNumber;
    }

    private String generateAppDB() {
        String databaseName = "db";
        int value = (int) (Math.random() * 999);
        String randomNumber = Integer.toString(value);
        return databaseName + randomNumber;
    }
}
