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

package org.wso2.carbon.event.formatter.core.internal.type.xml;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.carbon.event.formatter.core.config.EventFormatter;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterFactory;
import org.wso2.carbon.event.formatter.core.config.OutputMapping;

public class XMLEventFormatterFactory implements EventFormatterFactory {


    @Override
    public OutputMapping constructOutputMapping(OMElement omElement) {
        return XMLFormatterConfigurationBuilder.fromOM(omElement);
    }

    @Override
    public OMElement constructOutputMappingOM(
            OutputMapping outputMapping, OMFactory factory) {
        return XMLFormatterConfigurationBuilder.outputMappingToOM(outputMapping, factory);
    }

    @Override
    public EventFormatter constructEventFormatter(
            EventFormatterConfiguration eventFormatterConfiguration, int tenantId) {
        return new XMLOutputEventFormatter(eventFormatterConfiguration, tenantId);
    }
}
