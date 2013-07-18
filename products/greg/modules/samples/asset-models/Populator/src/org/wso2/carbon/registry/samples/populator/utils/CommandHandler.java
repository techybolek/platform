/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.samples.populator.utils;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private static final Map<String, String> inputs = new HashMap<String, String>();
    private static final Map<String, String> argumentMap = new HashMap<String, String>();

    static {
        argumentMap.put("-cr", "Context Root of the Service");
        argumentMap.put("-l", "Location of Model");
        argumentMap.put("-pw", "Password of the Admin");
        argumentMap.put("-u", "Username of the Admin");
        argumentMap.put("-p", "Port of the registry");
        argumentMap.put("-h", "Hostname of the registry");
    }

    public static boolean setInputs(String[] arguments) {

        if (arguments.length == 0 || (arguments.length == 1 && arguments[0].equals("--help"))) {
            printMessage();
            return false;
        }

        // now loop through the arguments list to capture the options
        for (int i = 0; i < arguments.length; i++) {
            String val = argumentMap.get(arguments[i]);
            if (val != null) {
                if (arguments.length - 1 == i) {
                    throw new RuntimeException(val + " is missing");
                }
                inputs.put(arguments[i], arguments[++i]);
            }
        }

        return true;
    }


    private static void printMessage() {
        System.out.println("Usage: migration-client <options>");
        System.out.println("Valid options are:");
        System.out.println("\t-h :\t(Required) The hostname/ip of the registry to login.");
        System.out.println("\t-p :\t(Required) The port of the registry to login.");
        System.out.println("\t-u :\t(Required) The user name of the registry login.");
        System.out.println("\t-pw:\t(Required) The password of the registry login.");
        System.out.println();
        System.out.println("Example to migrate a registry running on localhost on default values");
        System.out.println("\te.g: migration-client -h localhost -p 9443 -u admin -pw admin");
    }

    public static String getRegistryURL() {
        String contextRoot = inputs.get("-cr");
        if (contextRoot == null) {
            if(getDomain() != null){
                return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/t/" + getDomain() + "/registry/";
            } else {
            	return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/registry/";
	    }
        } else {
	    if(getDomain() != null){
                return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/t/" + getDomain() + "/" + contextRoot + "/registry/";
            } else {
            	return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/" + contextRoot + "/registry/";
	    }
        }
    }

    public static String getHost() {
 	return inputs.get("-h");	
    }

    public static String getServiceURL() {
        String contextRoot = inputs.get("-cr");
	if (contextRoot == null) {
            if(getDomain() != null){
                return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/t/" + getDomain() + "/services/";
            } else {
                return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/services/";
            }
        } else {
            if(getDomain() != null){
                return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/t/" + getDomain() + "/" + contextRoot + "/services/";
            } else {
                return "https://" + inputs.get("-h") + ":" + inputs.get("-p") + "/" + contextRoot + "/services/";
            }
        }
    }

    public static String getHandlerJarLocation() {
        return inputs.get("-l")+"/target";
    }

    public static String getHandlerDef() {
        return inputs.get("-l")+"/handler-def/handlers.xml";
    }

    public static String getLifecycleConfigLocation() {
        return inputs.get("-l")+"/lifecycles";
    }

    public static String getLifecycleOperationsLocation() {
        return inputs.get("-l")+"/lifecycle-operations";
    }

    public static String getUsersAndRolesLocation() {
        return inputs.get("-l") + "/users-and-roles";
    }

    public static String getSubscriptionsLocation() {
        return inputs.get("-l") + "/subscriptions";
    }

    public static String getDataLocation() {
        return inputs.get("-l") + "/data";
    }

    public static String getRxtFileLocation() {
        return inputs.get("-l")+"/registry-extensions";
    }

    public static String getUsername() {
        return inputs.get("-u");
    }

    public static String getDomain(){
	String[] s = inputs.get("-u").split("@");
	if(s.length == 2){
	    return s[1];
	} else {
	    return null;
	}

    }

    public static String getPassword() {
        return inputs.get("-pw");
    }

    public static String getJRTemplateLocation(){
        return inputs.get("-l")+"/reporting-templates";
    }

    public static String getReportsLocation(){
        return inputs.get("-l")+"/reports";
    }

    public static String getModelName(){
        return inputs.get("-l").split("/")[1];
    }
}

