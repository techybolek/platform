/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.esb.jms.transport.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class MessageStoreMessageJMSPersistenceTest extends ESBIntegrationTest {

    private static final String logLine0 = "Error processing File URI :.*test.xml";

    private ServerConfigurationManager serverConfigurationManager;
    private String pathToVfsDir;
    private LogViewerClient logViewer;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        // Start the sever
        super.init();

        pathToVfsDir = getClass().getResource(File.separator + "artifacts" + File.separator + "ESB" +
                File.separator + "synapseconfig" + File.separator +
                "vfsTransport" + File.separator).getPath();

        // Change axis2.xml
        serverConfigurationManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        serverConfigurationManager.applyConfiguration(new File(pathToVfsDir + File.separator + "axis2.xml"));
        super.init();

        // create the in folder
        File inFolder = new File(pathToVfsDir + "test" + File.separator + "in" + File.separator);
        assertTrue(inFolder.mkdirs(), "file folder not created");

        logViewer = new LogViewerClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
    }

    @AfterClass(alwaysRun = true)
    public void restoreServerConfiguration() throws Exception {
        try {
            super.cleanup();
        } finally {
            Thread.sleep(3000);
            serverConfigurationManager.restoreToLastConfiguration();
            serverConfigurationManager = null;
        }
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = {"wso2.esb"}, description = "Testing the persist message part of messageStore")
    public void messageStorePersistMessageTest() throws Exception {

        // Set the message store
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/messageStore/MessageStoreMessagePersistTest_config.xml");
        Thread.sleep(2000);

        try {
            // add the vfs config
            addVFSProxy();
            Thread.sleep(2000);
        } catch (Exception e) {e.printStackTrace();}

        File sourceFile = new File(pathToVfsDir + File.separator + "test.xml");
        File targetFile = new File(pathToVfsDir + "test" + File.separator + "in" + File.separator + "test.xml");
        try {
            FileUtils.copyFile(sourceFile, targetFile);
            Thread.sleep(5000);

            LogEvent[] logs = logViewer.getAllSystemLogs();
            for (LogEvent log : logs) {
                Assert.assertFalse(log.getMessage().matches(logLine0));
            }
        } finally {
            deleteFile(targetFile);
        }
    }

    private void addVFSProxy()
            throws Exception {

        addProxyService(AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" " +
                "           name =\"TestProxy\"\n" +
                "           transports=\"http vfs\"\n" +
                "           startOnLoad=\"true\"\n" +
                "           trace=\"disable\">\n" +
                "        <description/>\n" +
                "        <target>\n" +
                "            <inSequence>\n" +
                "                <property name=\"OUT_ONLY\" value=\"true\" scope=\"default\" type=\"STRING\"/>\n" +
                "                <store messageStore=\"JMSMS\"/>\n" +
                "            </inSequence>\n" +
                "        </target>\n" +
                "        <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n" +
                "        <parameter name=\"transport.PollInterval\">1</parameter>\n" +
                "        <parameter name=\"transport.vfs.FileURI\">file://" + pathToVfsDir + "test" + File.separator + "in" + File.separator + "</parameter>\n" +
                "        <parameter name=\"transport.vfs.FileNamePattern\">.*.xml</parameter>\n" +
                "        <parameter name=\"transport.vfs.ContentType\">text/xml</parameter>\n" +
                "        <parameter name=\"transport.vfs.ActionAfterFailure\">MOVE</parameter>\n" +
                "</proxy>"));
    }

    private boolean deleteFile(File file) throws IOException {
        return file.exists() && file.delete();
    }
}
