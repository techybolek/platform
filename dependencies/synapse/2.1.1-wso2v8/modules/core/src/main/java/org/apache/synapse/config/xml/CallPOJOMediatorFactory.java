package org.apache.synapse.config.xml;

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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.mediators.ext.CallPOJOMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;


/**
 * Factory for {@link org.apache.synapse.mediators.ext.CallPOJOMediator} instances.
 * <p/>
 * Configuration syntax:
 * <pre>
 * &lt;call-pojo name=&quot;class-name&quot;&gt;
 *   &lt;input name=&quot;string&quot; value=&quot;literal&quot; | expression=&quot;xpath&quot;
 *   /&gt;
 *   &lt;input name=&quot;string&quot; expression=&quot;XPATH expression&quot;?
 *                 /&gt;
 *   &lt;output name=&quot;string&quot; ? /&gt;
 * &lt;/call-pojo&gt;
 * </pre>
 */
public class CallPOJOMediatorFactory extends AbstractMediatorFactory {

    private static final QName CALL_POJO_Q
            = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "call-pojo");


    public Mediator createSpecificMediator(OMElement elem, Properties properties) {

        CallPOJOMediator pojoMediator = new CallPOJOMediator();

        // Class name of the CallPOJO object should be present
        OMAttribute name = elem.getAttribute(ATT_NAME);
        if (name == null) {
            String msg = "The name of the actual Call POJO implementation class" +
                    " is a required attribute";
            log.error(msg);
            throw new SynapseException(msg);
        }

        Class classMediatorClass;

        try {
            ClassLoader classMediatorLoader = null;
            if(properties != null){
                classMediatorLoader = (ClassLoader) properties.get(SynapseConstants.SYNAPSE_LIB_LOADER);
            }
            classMediatorLoader = classMediatorLoader != null ? classMediatorLoader :
                    getClass().getClassLoader();
            classMediatorClass = classMediatorLoader.loadClass(
                    name.getAttributeValue());

        } catch (Exception e) {
            String msg = "Error : " + name.getAttributeValue();
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }


        // setting the properties to the command. these properties will be instantiated
        // at the mediation time
        for (Iterator it = elem.getChildElements(); it.hasNext(); ) {
            OMElement child = (OMElement) it.next();
            if ("input".equals(child.getLocalName()) || "output".equals(child.getLocalName())) {

                OMAttribute nameAttr = child.getAttribute(ATT_NAME);
                if (nameAttr != null && nameAttr.getAttributeValue() != null
                        && !"".equals(nameAttr.getAttributeValue())) {
                    if ("input".equals(child.getLocalName())) {
                        handleInputPropertyAction(nameAttr.getAttributeValue(), child, pojoMediator);
                    } else {
                        handleOutputPropertyAction(nameAttr.getAttributeValue(), child, pojoMediator);
                    }
                } else {
                    handleException("A POJO command mediator " +
                            "property must specify the name attribute");
                }
            }
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc
        pojoMediator.setCommand(classMediatorClass);
        processAuditStatus(pojoMediator, elem);

        return pojoMediator;
    }

    private void handleInputPropertyAction(String name, OMElement propElem, CallPOJOMediator m) {

        OMAttribute valueAttr = propElem.getAttribute(ATT_VALUE);
        OMAttribute exprAttr = propElem.getAttribute(ATT_EXPRN);

        SynapseXPath xpath = null;
        try {
            if (exprAttr != null) {
                xpath = SynapseXPathFactory.getSynapseXPath(propElem, ATT_EXPRN);
            }
        } catch (JaxenException e) {
            handleException("Error in building the expression as an SynapseXPath" + e);
        }

        // if there is a value attribute there is expression
        if (valueAttr != null) {
            String value = valueAttr.getAttributeValue();
            m.addValueSetterProperty(name,value);

        } else {
            // if both context-name and expression is there
            if (exprAttr != null) {
                m.addExpressionSetterProperty(name,xpath);
                } else {
                    handleException("Action attribute " +
                            "is required for the command property with name " + name);
                }
            }

    }


    private void handleOutputPropertyAction(String name, OMElement propElem, CallPOJOMediator m) {

        OMAttribute valueAttr = propElem.getAttribute(ATT_VALUE);

        // if there is a value attribute there is no action (action is implied as read value)
        if (valueAttr != null) {
            String value = valueAttr.getAttributeValue();
            m.addValueGetterProperty(name,value);
        } else {
            m.addValueGetterProperty(name, propElem.getAttributeValue(ATT_NAME));
        }
    }

    public QName getTagQName() {
        return CALL_POJO_Q;
    }

}


