/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.automation.core.utils.serverutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.coreutils.CodeCoverageUtils;
import org.wso2.carbon.automation.core.utils.coreutils.InputStreamHandler;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.fileutils.ArchiveExtractor;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * A set of utility methods such as starting & stopping a Carbon server.
 */
public class ServerUtils {
    private static final Log log = LogFactory.getLog(ServerUtils.class);

    private Process process;
    private String carbonHome;
    private String originalUserDir = null;
    private InputStreamHandler inputStreamHandler;
    private boolean isCoverageEnable = false;
    private static final String SERVER_SHUTDOWN_MESSAGE = "Halting JVM";
    private static final String SERVER_STARTUP_MESSAGE = "Mgt Console URL";
    private static final long DEFAULT_START_STOP_WAIT_MS = 1000 * 60 * 5;
    private int defaultHttpsPort = 9443;

    public synchronized void startServerUsingCarbonHome(String carbonHome,
                                                        final FrameworkProperties frameworkProperties,
                                                        Map<String, String> commandMap) {
        if (process != null) { // An instance of the server is running
            return;
        }
        Process tempProcess;
        isCoverageEnable = new EnvironmentBuilder().getFrameworkSettings().getCoverageSettings().
                getCoverageEnable();
        try {
            if (isCoverageEnable) {
                CodeCoverageUtils.init();
                CodeCoverageUtils.instrument(carbonHome);
            }
            defaultHttpsPort = Integer.parseInt(frameworkProperties.getProductVariables().getHttpsPort());
            int defaultHttpPort = Integer.parseInt(frameworkProperties.getProductVariables().getHttpPort());
            System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
            originalUserDir = System.getProperty("user.dir");
            System.setProperty("user.dir", carbonHome);
            File commandDir = new File(carbonHome);
            log.info("Starting server............. ");
            String scriptName = ProductConstant.SEVER_STARTUP_SCRIPT_NAME;
            final int portOffset = getPortOffsetFromCommandMap(commandMap);

            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                commandDir = new File(carbonHome + File.separator + "bin");
                String[] cmdArray;
                if (isCoverageEnable) {
                    cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat",
                                            "-Demma.properties=" + System.getProperty("emma.properties"),
                                            "-Demma.rt.control.port=" + (47653 + portOffset),
                                            expandServerStartupCommandList(commandMap)};
                } else {
                    cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat",
                                            expandServerStartupCommandList(commandMap)};
                }

                tempProcess = Runtime.getRuntime().exec(cmdArray, null, commandDir);
            } else {
                String[] cmdArray;
                if (isCoverageEnable) {
                    cmdArray = new String[]{"sh", "bin/" + scriptName + ".sh",
                                            "-Demma.properties=" + System.getProperty("emma.properties"),
                                            "-Demma.rt.control.port=" + (47653 + portOffset),
                                            expandServerStartupCommandList(commandMap)};
                } else {
                    cmdArray = new String[]{"sh", "bin/" + scriptName + ".sh",
                                            expandServerStartupCommandList(commandMap)};
                }

                tempProcess = Runtime.getRuntime().exec(cmdArray, null, commandDir);
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
                        log.info("Shutting down server..");
                        shutdown(portOffset, frameworkProperties);
                    } catch (Exception e) {
                        log.error("Cannot shutdown server ..", e);
                    }
                }
            });

            ClientConnectionUtil.waitForPort(defaultHttpPort + portOffset,
                                             DEFAULT_START_STOP_WAIT_MS, false,
                                             frameworkProperties.getProductVariables().getHostName());
            //wait until Mgt console url printed.
            long time = System.currentTimeMillis() + 60 * 1000;
            while (!inputStreamHandler.getOutput().contains(SERVER_STARTUP_MESSAGE) &&
                   System.currentTimeMillis() < time) {
                // wait until server startup is completed
            }

            ClientConnectionUtil.waitForLogin(defaultHttpsPort + portOffset,
                                              frameworkProperties.getProductVariables().getHostName(),
                                              frameworkProperties.getProductVariables().getBackendUrl());

            log.info("Server started successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Unable to start server", e);
        }
        process = tempProcess;
    }

    public synchronized String setUpCarbonHome(String carbonServerZipFile)
            throws IOException {
        if (process != null) { // An instance of the server is running
            return carbonHome;
        }
        if (isCoverageEnable) {
            CodeCoverageUtils.init();
        }
        int indexOfZip = carbonServerZipFile.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(carbonServerZipFile + " is not a zip file");
        }
        String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";
        if (fileSeparator.equals("\\")) {
            carbonServerZipFile = carbonServerZipFile.replace("/", "\\");
        }
        String extractedCarbonDir =
                carbonServerZipFile.substring(carbonServerZipFile.lastIndexOf(fileSeparator) + 1,
                                              indexOfZip);
        FileManipulator.deleteDir(extractedCarbonDir);
        String extractDir = "carbontmp" + System.currentTimeMillis();

        String baseDir = (System.getProperty("basedir", ".")) + File.separator + "target";
        new ArchiveExtractor().extractFile(carbonServerZipFile, baseDir + File.separator + extractDir);
        return carbonHome =
                new File(baseDir).getAbsolutePath() + File.separator + extractDir + File.separator +
                extractedCarbonDir;
    }

    public synchronized void shutdown(int portOffset, FrameworkProperties properties)
            throws Exception {
        if (process != null) {
            if (ClientConnectionUtil.isPortOpen(defaultHttpsPort + portOffset,
                                                properties.getProductVariables().getHostName())) {

                shutdownServer(portOffset, properties);
                long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
                while (!inputStreamHandler.getOutput().contains(SERVER_SHUTDOWN_MESSAGE) &&
                       System.currentTimeMillis() < time) {
                    // wait until server shutdown is completed
                }

                log.info("Server stopped successfully...");
            }
            process.destroy();
            process = null;
            if (isCoverageEnable) {
                CodeCoverageUtils.generateReports(carbonHome);
            }

            if (portOffset == 0) {
                System.clearProperty(ServerConstants.CARBON_HOME);
            }
            System.setProperty("user.dir", originalUserDir);
        }
    }

    private void shutdownServer(int portOffset, FrameworkProperties properties) throws Exception {
        int httpsPort = defaultHttpsPort + portOffset;
        String url = properties.getProductVariables().getBackendUrl();
        String backendURL = url.replaceAll("(:\\d+)", ":" + httpsPort);

        log.info("Shutting down the server running on : " + backendURL);
        UserInfo userInfo = UserListCsvReader.getUserInfo(ProductConstant.SUPER_ADMIN_USER_ID);
        try {

            ServerAdminClient serverAdminClient =
                    new ServerAdminClient(backendURL, userInfo.getUserName(), userInfo.getPassword());
            serverAdminClient.shutdown();
        } catch (Exception e) {
            log.error("Error when shutting down the server.", e);
            throw new Exception("Error when shutting down the server.", e);
        }
    }

    public synchronized void restartGracefully(ServerAdminClient serverAdminClient,
                                               final FrameworkProperties frameworkProperties)
            throws Exception {

        serverAdminClient.restartGracefully();

        long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
        while (!inputStreamHandler.getOutput().contains(SERVER_SHUTDOWN_MESSAGE) &&
               System.currentTimeMillis() < time) {
            // wait until server shutdown is completed
        }

        Thread.sleep(5000);//wait for port to close

        if (isCoverageEnable) {
            CodeCoverageUtils.renameCoverageDataFile(carbonHome);
        }

        ClientConnectionUtil.waitForPort(Integer.parseInt(frameworkProperties.getProductVariables().getHttpsPort()),
                                         frameworkProperties.getProductVariables().getHostName());

        ClientConnectionUtil.waitForLogin(Integer.parseInt(frameworkProperties.getProductVariables().getHttpsPort()),
                                          frameworkProperties.getProductVariables().getHostName(),
                                          frameworkProperties.getProductVariables().getBackendUrl());

    }

    private String expandServerStartupCommandList(Map<String, String> commandMap) {
        Set<Map.Entry<String, String>> entries = commandMap.entrySet();
        String keyValueArray = "";
        for (Map.Entry<String, String> entry : entries) {

            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.isEmpty()) {
                keyValueArray = keyValueArray + key + ",";
            } else {
                keyValueArray = keyValueArray + key + "=" + value + ",";
            }
        }
        return keyValueArray.substring(0, keyValueArray.length() - 1);

    }

    private int getPortOffsetFromCommandMap(Map<String, String> commandMap) {
        if (commandMap.get(ProductConstant.PORT_OFFSET_COMMAND) != null) {
            return Integer.parseInt(commandMap.get(
                    ProductConstant.PORT_OFFSET_COMMAND));
        } else {
            throw new IllegalArgumentException("portOffset value must be set in command " +
                                               "list");
        }
    }
}