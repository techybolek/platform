/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.webapp.stat.publisher.util;

import org.wso2.carbon.bam.webapp.stat.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.context.CarbonContext;

import java.util.HashMap;
import java.util.Map;

public class WebappConfig {

    public volatile static Map<Integer, Map<String, Integer>> webappConfiguration = new HashMap<Integer, Map<String, Integer>>();


    public static boolean  getWebappConfigData(String webappName) {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        if (webappConfiguration.containsKey(tenantId)) {
            Map<String, Integer> map = webappConfiguration.get(tenantId);
            if (map.containsKey(webappName)) {
                return map.get(webappName) == 1 ? true : false;
            }else{
             return readFromRegistry(webappName,tenantId);
            }
        }
        else{
            return readFromRegistry(webappName,tenantId);
        }
    }

    public void setWebappConfigData(String webappName, int value) {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        insertValueToMap(webappName, value, tenantId);

        RegistryPersistenceManager registryPersistenceManager = new RegistryPersistenceManager();

        registryPersistenceManager.setWebappConfigProperty(tenantId,webappName,value);

    }

    public static void  insertValueToMap(String webappName, int value, int tenantId){

        if (webappConfiguration.containsKey(tenantId))   {
            Map<String, Integer> map = webappConfiguration.get(tenantId);
            map.put(webappName,value);
            webappConfiguration.put(tenantId,map);
        } else {
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put(webappName,value);
            webappConfiguration.put(tenantId,map);
        }

    }

    private static boolean readFromRegistry(String webappName, int tenantId){
        RegistryPersistenceManager registryPersistenceManager = new RegistryPersistenceManager();
        if(registryPersistenceManager.getWebappConfigProperty(tenantId,webappName) != null){
            String value = registryPersistenceManager.getWebappConfigProperty(tenantId,webappName);
            insertValueToMap(webappName, Integer.parseInt(value), tenantId);
            return Integer.parseInt(value) == 0 ? false : true;
        }  else{
            insertValueToMap(webappName, 0, tenantId);
            return false;
        }
    }
}
