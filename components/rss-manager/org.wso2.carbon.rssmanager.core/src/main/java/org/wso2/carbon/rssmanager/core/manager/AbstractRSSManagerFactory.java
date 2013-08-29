/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.rssmanager.core.manager;

import org.wso2.carbon.rssmanager.core.config.RSSConfiguration;
import org.wso2.carbon.rssmanager.core.exception.RSSManagerException;

import java.lang.reflect.Constructor;

public final class AbstractRSSManagerFactory {

    public static RSSManagerFactory getRSSManagerFactory(
            RSSConfiguration config) throws RSSManagerException {
        if (config == null) {
            throw new RSSManagerException("RSS Configuration is not initialized properly, " +
                    "thus is null");
        }
        String implClassName = config.getRSSProvider();
        if (implClassName == null) {
            throw new IllegalArgumentException("RSS Manager Factory implementation passed is " +
                    "null and thus, cannot be instantiated");
        }
        try {
            Class implClass = Class.forName(implClassName);
            Class[] typeParams = {RSSConfiguration.class};
            Constructor constructor = implClass.getConstructor(typeParams);
            Object[] typeParamValues = {config};
            return (RSSManagerFactory) constructor.newInstance(typeParamValues);
        } catch (Exception e) {
            throw new RSSManagerException("Error occurred while creating RSSManagerFactory: " +
                    e.getMessage(), e);
        }
    }

}
