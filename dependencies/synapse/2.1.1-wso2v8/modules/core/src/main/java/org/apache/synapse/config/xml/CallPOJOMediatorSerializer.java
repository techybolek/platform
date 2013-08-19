/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.ext.CallPOJOMediator;

/**
 * Serializer for {@link org.apache.synapse.mediators.ext.CallPOJOMediator} instances.
 *
 * @see CallPOJOMediatorFactory
 */
public class CallPOJOMediatorSerializer extends AbstractMediatorSerializer {

    public OMElement serializeSpecificMediator(Mediator mediatorInstance) {

        if (!(mediatorInstance instanceof CallPOJOMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + mediatorInstance.getType());
        }

        try {

        CallPOJOMediator mediator;
        mediator = (CallPOJOMediator) mediatorInstance;

        OMElement callpojo = fac.createOMElement("call-pojo", synNS);
        saveTracingState(callpojo, mediator);

        if (mediator.getCommand() != null && mediator.getCommand().getClass().getName() != null) {
            callpojo.addAttribute(fac.createOMAttribute(
                    "name", nullNS, mediator.getCommand().getName()));
        } else {
            handleException("Invalid POJO Command mediator. The command class name is required");
        }

        for (String propName : mediator.getValueSetterProperties().keySet()) {
            Object value = mediator.getValueSetterProperties().get(propName);
            OMElement prop = fac.createOMElement("input", synNS);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));

            if (value instanceof String) {
                prop.addAttribute(fac.createOMAttribute("value", nullNS, (String) value));
            } else if (value instanceof OMElement) {
                prop.addChild((OMElement) value);
            } else {
                handleException("Unable to serialize the command " +
                        "mediator property with the name " + propName + " : Unknown type");
            }

            callpojo.addChild(prop);
        }

        for (String propName : mediator.getExpressionSetterProperties().keySet()) {
            OMElement prop = fac.createOMElement("input", synNS);
            prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
            SynapseXPathSerializer.serializeXPath(
                    mediator.getExpressionSetterProperties().get(propName), prop, "expression");

            callpojo.addChild(prop);
        }

        for (String propName : mediator.getValueGetterProperties().keySet()) {
            if (!isSerialized(propName, mediator)) {
                OMElement prop = fac.createOMElement("output", synNS);
                prop.addAttribute(fac.createOMAttribute("name", nullNS, propName));
                callpojo.addChild(prop);
            }
        }

        return callpojo;
        } catch (ClassCastException ex) {
            handleException("Cannot cast the mediator class to CallPOJOMediator type" + ex);
            return null;
        }
    }

    private boolean isSerialized(String propName, CallPOJOMediator m) {
        return m.getValueSetterProperties().containsKey(propName) ||
                m.getExpressionSetterProperties().containsKey(propName)
                ;
    }

    public String getMediatorClassName() {
        return CallPOJOMediator.class.getName();
    }
}
