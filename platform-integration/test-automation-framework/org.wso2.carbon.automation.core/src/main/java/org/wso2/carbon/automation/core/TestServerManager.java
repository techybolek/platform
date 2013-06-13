/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.core;

import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.automation.core.utils.serverutils.ServerUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A TestServerManager is responsible for preparing a Carbon server for test executions,
 * and shuts down the server after test executions.
 * <p/>
 * All test suites/classes which require starting of a server should extend this class
 */
public abstract class TestServerManager {

    private ServerUtils serverUtils = new ServerUtils();
    private String carbonZip;
    private int portOffset;
    private FrameworkProperties frameworkProperties;
    private Map<String, String> commandMap = new HashMap<String, String>();
    private String carbonHome;

    protected TestServerManager() {
    }

    protected TestServerManager(String carbonZip) {
        this.carbonZip = carbonZip;
    }

    protected TestServerManager(int portOffset) {
        this.portOffset = portOffset;
    }

    protected TestServerManager(String carbonZip, Map<String, String> commandMap) {
        this.carbonZip = carbonZip;
        if (commandMap.get(ProductConstant.PORT_OFFSET_COMMAND) != null) {
            this.portOffset = Integer.parseInt(commandMap.get(ProductConstant.PORT_OFFSET_COMMAND));
        } else {
            throw new IllegalArgumentException("portOffset value must be set in command " +
                                               "list");
        }
//        this.portOffset = portOffset;
        this.commandMap = commandMap;
    }

    public String getCarbonZip() {
        return carbonZip;
    }

    public String getCarbonHome() {
        return carbonHome;
    }

    public Map<String, String> getCommands() {
        return commandMap;
    }

    /**
     * This method is called for starting a Carbon server in preparation for execution of a
     * TestSuite
     * <p/>
     * Add the @BeforeSuite TestNG annotation in the method overriding this method
     *
     * @return The CARBON_HOME
     * @throws IOException If an error occurs while copying the deployment artifacts into the
     *                     Carbon server
     */
    protected String startServer() throws IOException {
        if (carbonZip == null) {
            carbonZip = System.getProperty("carbon.zip");
        }
        if (carbonZip == null) {
            throw new IllegalArgumentException("carbon zip file is null");
        }
        carbonHome = serverUtils.setUpCarbonHome(carbonZip);
        frameworkProperties =
                FrameworkFactory.getFrameworkProperties(getServerList().get(0));
        serverUtils.startServerUsingCarbonHome(carbonHome, frameworkProperties, commandMap);
        return carbonHome;
    }

    /**
     * This method is called for stopping a Carbon server
     * <p/>
     * Add the @AfterSuite annotation in the method overriding this method
     *
     * @throws Exception If an error occurs while shutting down the server
     */
    protected void stopServer() throws Exception {
        serverUtils.shutdown(portOffset, frameworkProperties);
    }

    /**
     * Copy all the artifacts necessary for running a TestSuite
     *
     * @param carbonHome The CARBON_HOME of the relevant server
     * @throws IOException If an error occurs while copying artifacts
     */
    protected abstract void copyArtifacts(String carbonHome) throws IOException;

    private List<String> getServerList() {
        if (System.getProperty("server.list") != null) {
            return Arrays.asList(System.getProperty("server.list").split(","));
        }

        return null;
    }
}
