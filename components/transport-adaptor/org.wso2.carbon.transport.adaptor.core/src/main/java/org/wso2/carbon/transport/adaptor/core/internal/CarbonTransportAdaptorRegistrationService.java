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

package org.wso2.carbon.transport.adaptor.core.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorFactory;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorRegistrationService;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.transport.adaptor.core.exception.TransportAdaptorConfigException;
import org.wso2.carbon.transport.adaptor.core.internal.ds.TransportAdaptorServiceValueHolder;

public class CarbonTransportAdaptorRegistrationService
        implements TransportAdaptorRegistrationService {

    private static final Log log = LogFactory.getLog(TransportAdaptorService.class);

    @Override
    public void registerTransportAdaptor(String className) throws TransportAdaptorConfigException {

        try {
            Class transportTypeFactoryClass = Class.forName(className);
            TransportAdaptorFactory adaptorFactory =
                    (TransportAdaptorFactory) transportTypeFactoryClass.newInstance();
            ((CarbonTransportAdaptorService) (TransportAdaptorServiceValueHolder.getCarbonTransportAdaptorService())).registerTransportAdaptor(adaptorFactory.getTransportAdaptor());
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new TransportAdaptorConfigException("TransportAdaptor class " + className + " can not be found", e);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new TransportAdaptorConfigException("Can not access the class " + className, e);
        } catch (InstantiationException e) {
            log.error(e.getMessage(), e);
            throw new TransportAdaptorConfigException("Can not instantiate the class " + className, e);
        }
    }

    @Override
    public void unRegisterTransportAdaptor(String className) {
        // No unregister
    }
}
