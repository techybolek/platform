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

package org.wso2.carbon.event.formatter.core;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.formatter.core.config.EventFormatter;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.output.transport.adaptor.core.exception.OutputTransportAdaptorEventProcessingException;

public class EventFormatterListener {

    private EventFormatter eventFormatter;

    public EventFormatterListener(EventFormatter eventFormatter) {
        this.eventFormatter = eventFormatter;
    }

    public void onEvent(Object o) {
        try {
            eventFormatter.sendEvent(o);
        } catch (EventFormatterConfigurationException e) {
            throw new OutputTransportAdaptorEventProcessingException("Cannot send create an event from input:", e);
        }
    }

    public void onEvent(Event o) {
        try {
            eventFormatter.sendEvent(o);
        } catch (EventFormatterConfigurationException e) {
            throw new OutputTransportAdaptorEventProcessingException("Cannot send create an event from input:", e);
        }
    }


}
