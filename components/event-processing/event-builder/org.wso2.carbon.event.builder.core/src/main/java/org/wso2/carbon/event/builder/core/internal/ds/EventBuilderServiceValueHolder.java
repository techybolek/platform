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

package org.wso2.carbon.event.builder.core.internal.ds;

import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.builder.core.config.InputMapperFactory;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.event.builder.core.internal.type.json.JsonInputMapperFactory;
import org.wso2.carbon.event.builder.core.internal.type.map.MapInputMapperFactory;
import org.wso2.carbon.event.builder.core.internal.type.text.TextInputMapperFactory;
import org.wso2.carbon.event.builder.core.internal.type.wso2event.Wso2InputMapperFactory;
import org.wso2.carbon.event.builder.core.internal.type.xml.XMLInputMapperFactory;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.input.transport.adaptor.core.InputTransportAdaptorService;
import org.wso2.carbon.input.transport.adaptor.core.MessageType;
import org.wso2.carbon.input.transport.adaptor.manager.core.InputTransportAdaptorManagerService;

import java.util.concurrent.ConcurrentHashMap;

public class EventBuilderServiceValueHolder {
    private static EventBuilderService eventBuilderService;
    private static InputTransportAdaptorManagerService inputTransportAdaptorManagerService;
    private static InputTransportAdaptorService inputTransportAdaptorService;
    private static CarbonEventBuilderService carbonEventBuilderService;
    private static ConcurrentHashMap<String, InputMapperFactory> mappingFactoryMap;

    static {
        mappingFactoryMap = new ConcurrentHashMap<String, InputMapperFactory>();
        mappingFactoryMap.put(MessageType.MAP, new MapInputMapperFactory());
        mappingFactoryMap.put(MessageType.TEXT, new TextInputMapperFactory());
        mappingFactoryMap.put(MessageType.WSO2EVENT, new Wso2InputMapperFactory());
        mappingFactoryMap.put(MessageType.XML, new XMLInputMapperFactory());
        mappingFactoryMap.put(MessageType.JSON, new JsonInputMapperFactory());
    }

    private static EventStatisticsService eventStatisticsService;

    private EventBuilderServiceValueHolder() {

    }

    public static ConcurrentHashMap<String, InputMapperFactory> getMappingFactoryMap() {
        return mappingFactoryMap;
    }

    public static CarbonEventBuilderService getCarbonEventBuilderService() {
        return carbonEventBuilderService;
    }

    public static void registerEventBuilderService(EventBuilderService eventBuilderService) {
        EventBuilderServiceValueHolder.eventBuilderService = eventBuilderService;
        if (eventBuilderService instanceof CarbonEventBuilderService) {
            EventBuilderServiceValueHolder.carbonEventBuilderService = (CarbonEventBuilderService) eventBuilderService;
        }
    }

    public static EventBuilderService getEventBuilderService() {
        return EventBuilderServiceValueHolder.eventBuilderService;
    }

    public static void registerTransportAdaptorService(
            InputTransportAdaptorService transportAdaptorService) {
        EventBuilderServiceValueHolder.inputTransportAdaptorService = transportAdaptorService;
    }

    public static InputTransportAdaptorService getInputTransportAdaptorService() {
        return EventBuilderServiceValueHolder.inputTransportAdaptorService;
    }

    public static void registerTransportAdaptorManagerService(
            InputTransportAdaptorManagerService transportAdaptorManagerService) {
        EventBuilderServiceValueHolder.inputTransportAdaptorManagerService = transportAdaptorManagerService;
    }

    public static InputTransportAdaptorManagerService getInputTransportAdaptorManagerService() {
        return EventBuilderServiceValueHolder.inputTransportAdaptorManagerService;
    }


    public static void registerEventStatisticsService(EventStatisticsService eventStatisticsService) {
        EventBuilderServiceValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }
}
