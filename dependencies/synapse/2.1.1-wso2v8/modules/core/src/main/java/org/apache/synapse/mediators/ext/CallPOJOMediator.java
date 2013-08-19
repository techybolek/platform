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

package org.apache.synapse.mediators.ext;

import org.apache.synapse.CallPOJO;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.commons.util.PropertyHelper;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SynapseXPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This mediator will use the specified call-pojo object and execute the command after setting
 * the properties specified to it through the configuration. The specified call-pojo object may or
 * may not implement the CallPOJO interface. If the call-pojo object has not implemented the CallPOJO
 * interface then this will use reflection to find a method called execute() and execute it.
 *
 * @see org.apache.synapse.CallPOJO interface
 */
public class CallPOJOMediator extends AbstractMediator {

    /**
     * This will hold the command object to be executed
     */
    private Class command = null;

    /**
     * properties whose values need to be set
     */
    private final Map<String, Object> valueSetterProperties = new HashMap<String, Object>();

    /**
     * properties whose values need to be set
     */
    private final Map<String, SynapseXPath> expressionSetterProperties = new HashMap<String, SynapseXPath>();

    /**
     * properties whose values need to be get
     */
    private final Map<String, String> valueGetterProperties = new HashMap<String, String>();


    /**
     * Implements the mediate method of the Mediator interface. This method will instantiate
     * a new instance of the POJO class, set all specified properties from the current runtime
     * state (and message context) and call the execute method of the Command object.
     *
     * @param synCtx - Synapse MessageContext to be mediated
     * @return boolean true since this will not stop exection chain
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : POJOCommand mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Creating a new instance of POJO class : " + command.getClass());
        }

        Object commandObject = null;
        try {
            // instantiate a new command object each time
            commandObject = command.newInstance();
        } catch (Exception e) {
            handleException("Error creating an instance of the POJO command class : " +
                    command.getClass(), e, synCtx);
        }

        synLog.traceOrDebug("Instance created, setting static and dynamic properties");

        // then set the static/constant properties first
        for (String name : valueSetterProperties.keySet()) {
            PropertyHelper.setInstanceProperty(name, valueSetterProperties.get(name), commandObject);
        }

        // now set the any dynamic properties from the message context properties
        for (String name : expressionSetterProperties.keySet()) {
            String expressionString =  expressionSetterProperties.get(name).stringValueOf(synCtx);
            if(expressionString != null) {
            PropertyHelper.setInstanceProperty(name,expressionString ,
                    commandObject);
            }  else {
                SynapseXPath xpath = expressionSetterProperties.get(name);
                String value = xpath.stringValueOf(synCtx);
                PropertyHelper.setInstanceProperty(name, value, commandObject);
            }
        }

        synLog.traceOrDebug("CallPOJO object initialized successfully, invoking the execute() method");

        // then call the execute method if the Command interface is implemented
        if (commandObject instanceof CallPOJO) {
            try {
                ((CallPOJO) commandObject).execute(synCtx);
            } catch (Exception e) {
                handleException("Error invoking POJO command class : "
                        + command.getClass(), e, synCtx);
            }

        } else {

            try {
                Method exeMethod = command.getMethod("execute");
                exeMethod.invoke(commandObject);
            } catch (NoSuchMethodException e) {
                handleException("Cannot locate an execute() method on POJO class : " +
                        command.getClass(), e, synCtx);
            } catch (Exception e) {
                handleException("Error invoking the execute() method on POJO class : " +
                        command.getClass(), e, synCtx);
            }
        }

        // then set the context properties back to the messageContext from the command
        for (String name : valueGetterProperties.keySet()) {
            synCtx.setProperty(valueGetterProperties.get(name),
                    getInstanceProperty(name, commandObject, synCtx));
        }

        synLog.traceOrDebug("End : POJOCommand mediator");
        return true;
    }

    /**
     * Find and invoke the getter method with the name of form getXXX and returns the value given
     * on the POJO object
     *
     * @param name name of the getter field
     * @param obj POJO instance
     * @param synCtx current message
     * @return object representing the value of the getter method
     */
    private Object getInstanceProperty(String name, Object obj, MessageContext synCtx) {

        String mName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            Method[] methods = obj.getClass().getMethods();

            for (Method method : methods) {
                if (mName.equals(method.getName())) {
                    Object returnResult = method.invoke(obj);
                    synCtx.setProperty(name, returnResult);
                    return   returnResult;
                }
            }
        } catch(InvocationTargetException e) {
            handleException("Unable to get the command property '"
                    + name + "' back to the message", e, synCtx);
        } catch(IllegalAccessException e){
            handleException("Unable to get the command property '"
                    + name + "' back to the message", e, synCtx);
        }

        return null;
    }

    public Class getCommand() {
        return command;
    }

    public void setCommand(Class command) {
        this.command = command;
    }

    public void addValueSetterProperty(String name, Object value) {
        this.valueSetterProperties.put(name, value);
    }

    public void addExpressionSetterProperty(String name, SynapseXPath xpath) {
        this.expressionSetterProperties.put(name, xpath);
    }


    public void addValueGetterProperty(String name, String value) {
        this.valueGetterProperties.put(name, value);
    }


    public Map<String, Object> getValueSetterProperties() {
        return this.valueSetterProperties;
    }

    public Map<String, SynapseXPath> getExpressionSetterProperties() {
        return this.expressionSetterProperties;
    }

    public Map<String, String> getValueGetterProperties() {
        return this.valueGetterProperties;
    }

}
