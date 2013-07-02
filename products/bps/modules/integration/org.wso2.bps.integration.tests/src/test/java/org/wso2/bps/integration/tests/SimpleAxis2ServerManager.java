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
package org.wso2.bps.integration.tests;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.utils.InputStreamHandler;
import org.wso2.carbon.utils.ServerConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * A simple Axis2 Server for deploying *.aar files for security scenarios
 */
public class SimpleAxis2ServerManager {

    private static Process process;
    private static String originalUserDir = null;
    private static InputStreamHandler inputStreamHandler;

    private final static String SERVER_STARTUP_MESSAGE = "[SimpleAxisServer] Starting";
    private final static String SERVER_SHUTDOWN_MESSAGE = "Shutting down SimpleAxisServer";
    private final static long DEFAULT_START_STOP_WAIT_MS = 1000 * 60 * 4;
    private static final Log log = LogFactory.getLog(SimpleAxis2ServerManager.class);


    public synchronized static void startServer()
            throws ServerConfigurationException {
        if (process != null) { // An instance of the server is running
            return;
        }
        Process tempProcess;
        try {
            String carbonHome = System.getProperty("carbon.home");
            System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
            originalUserDir = System.getProperty("user.dir");
            System.setProperty("user.dir", carbonHome);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            String temp;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                tempProcess = Runtime.getRuntime().exec(new String[]{"bat", "samples/axis2Server/axis2server.bat"},
                                                        null, new File(carbonHome));
            } else {
                tempProcess = Runtime.getRuntime().exec(new String[]{"sh", "samples/axis2Server/axis2server.sh", "test"},
                                                        null, new File(carbonHome));
            }

            InputStreamHandler errorStreamHandler =
                    new InputStreamHandler("errorStream", tempProcess.getErrorStream());
            inputStreamHandler = new InputStreamHandler("inputStream", tempProcess.getInputStream());

            // start the stream readers
            inputStreamHandler.start();
            errorStreamHandler.start();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        shutdown();
                    } catch (Exception ignored) {

                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException("Unable to start server", e);
        }

        ClientConnectionUtil.waitForPort(9000, 60 * 1000, true);

        process = tempProcess;
        log.info("Successfully started Axis2Server server. Returning...");
    }

    public synchronized static void shutdown() throws Exception {

        if (process != null) {
            process.destroy();
            process = null;
            long startTime = System.currentTimeMillis() + 60;
            try {
                while (startTime < System.currentTimeMillis()) { //wait for axis2 server shutdown.
                    boolean value = ClientConnectionUtil.isPortOpen(9000);
                    if (!value) {
                        break;
                    }
                    Thread.sleep(1000);
                }

            } finally {
                System.clearProperty(ServerConstants.CARBON_HOME);
                System.setProperty("user.dir", originalUserDir);

            }
        }
    }
}
