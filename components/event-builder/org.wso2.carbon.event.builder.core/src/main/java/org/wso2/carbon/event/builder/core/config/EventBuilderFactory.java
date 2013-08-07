
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

package org.wso2.carbon.event.builder.core.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.engine.AxisConfiguration;


public interface EventBuilderFactory {

    /**
     * Construct an {@link InputMapping} from the give omElement and return it.
     *
     * @param omElement the {@link OMElement} that will be used to construct input mapping
     * @return the constructed {@link InputMapping}
     */
    InputMapping constructInputMappingFromOM(OMElement omElement);

    /**
     * Construct an OMElement from a given input mapping
     *
     * @param inputMapping the {@link InputMapping} that will be used to create the OMElement
     * @param factory      the {@link OMFactory} that will be used in this construction
     * @return the constructed {@link OMElement}
     */
    OMElement constructOMFromInputMapping(InputMapping inputMapping, OMFactory factory);

    /**
     * Constructs an returns an appropriate EventBuilder depending on the Factory Implementation
     *
     * @param axisConfiguration         axisConfiguration of the calling context
     * @param eventBuilderConfiguration the {@link EventBuilderConfiguration} to be used
     * @return the {@link EventBuilder} instance populated with the supplied configuration
     */
    EventBuilder constructEventBuilder(AxisConfiguration axisConfiguration, EventBuilderConfiguration eventBuilderConfiguration);
}
