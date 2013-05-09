/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.transport.adaptor.manager.core.internal.build;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.manager.core.TransportManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.exception.TMConfigurationException;
import org.wso2.carbon.transport.adaptor.manager.core.internal.CarbonTransportManagerService;
import org.wso2.carbon.transport.adaptor.manager.core.internal.util.TransportManagerValueHolder;

/**
 * this class creates the transport manager service by reading the values from the transport-manager-config.xml file
 */
public class TransportManagerServiceBuilder {

    private static final Log log = LogFactory.getLog(TransportManagerServiceBuilder.class);
    private static CarbonTransportManagerService carbonTransportManagerService;

    /**
     * creates the service and register it
     *
     * @return - transport configuration service.
     * @throws TMConfigurationException
     */
    public static TransportManagerService createTransportManagerService() throws TMConfigurationException {

        carbonTransportManagerService = new CarbonTransportManagerService();
        TransportManagerValueHolder.registerCarbonTransportManagerService(carbonTransportManagerService);
        return carbonTransportManagerService;
    }


}
