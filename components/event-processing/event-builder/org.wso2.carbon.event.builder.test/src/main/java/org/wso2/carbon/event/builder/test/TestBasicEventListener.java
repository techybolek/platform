/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.builder.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.builder.core.BasicEventListener;

/**
 * Topic subscription call back handler implementation
 */
public class TestBasicEventListener implements BasicEventListener {
    private static final Log log = LogFactory.getLog(TestBasicEventListener.class);

    @Override
    public void onAddDefinition(StreamDefinition definition) {
        log.info("[TEST-Module] Added definition : " + definition.toString());
    }

    @Override
    public void onRemoveDefinition(StreamDefinition definition) {
        log.info("[TEST-Module] Removed definition : " + definition.toString());
    }

    @Override
    public void onEvent(Object[] event) {
        log.info("[TEST-Module] Received event as Object[]");
        for (Object object : event) {
            log.info(object.toString());
        }
    }
}
