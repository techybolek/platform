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

package org.wso2.carbon.event.builder.admin.internal.util;

import org.wso2.carbon.event.builder.admin.exception.EventBuilderAdminServiceException;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderConfigurationDto;
import org.wso2.carbon.event.builder.admin.internal.EventBuilderPropertyDto;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.input.transport.adaptor.core.message.MessageDto;

public interface DtoConvertible {
    /**
     *
     * @param messageDto
     * @param eventBuilderConfiguration
     * @return
     */
    public EventBuilderPropertyDto[] getEventBuilderPropertiesFrom(MessageDto messageDto, EventBuilderConfiguration eventBuilderConfiguration);

    /**
     *
     * @param eventBuilderConfigurationDto
     * @param tenantId
     * @return
     * @throws EventBuilderAdminServiceException
     */
    public EventBuilderConfiguration toEventBuilderConfiguration(
            EventBuilderConfigurationDto eventBuilderConfigurationDto, int tenantId) throws
                                                                                     EventBuilderAdminServiceException;

    /**
     *
     * @param eventBuilderConfiguration
     * @return
     * @throws EventBuilderAdminServiceException
     */
    public EventBuilderConfigurationDto fromEventBuilderConfiguration(
            EventBuilderConfiguration eventBuilderConfiguration)
            throws EventBuilderAdminServiceException;

}
