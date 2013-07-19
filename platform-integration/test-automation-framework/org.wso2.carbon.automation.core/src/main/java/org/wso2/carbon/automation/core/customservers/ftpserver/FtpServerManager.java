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

package org.wso2.carbon.automation.core.customservers.ftpserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FtpServerManager {
    private final static Log log = LogFactory.getLog(FtpServerManager.class);
    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
    FtpServerFactory serverFactory = new FtpServerFactory();
    ListenerFactory factory = new ListenerFactory();
    boolean isRunning = false;
    int ftpPort;
    FtpServer ftpServer;
    private Thread ftpThread = null;

    public static void main(String[] args) {
        new FtpServerManager().startServer();
    }

    public void startFtpServer() {


        userManagerFactory.setFile(new File("/home/dharshana/wso2source/carbon/platform/trunk/platform-integration/test-automation-framework/org.wso2.carbon.automation.core/src/main/resources/ftp"));//choose any. We're telling the FTP-server where to read it's user list

// set the port of the listener
        factory.setPort(2223);
// replace the default listener
        serverFactory.addListener("default", factory.createListener());
// start the server
        serverFactory.setUserManager(setUser());
        ftpServer = serverFactory.createServer();
        try {
            ftpServer.start();
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }

    public UserManager setUser() {
        BaseUser user = new BaseUser();
        user.setName("test");
        user.setPassword("test");
        user.setHomeDirectory("/home/dharshana/kernal");
        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        user.setAuthorities(authorities);
        UserManager um = userManagerFactory.createUserManager();
        try {
            um.save(user);//Save the user to the user list on the filesystem
        } catch (FtpException e1) {
            //Deal with exception as you need
        }
        return um;
    }

    public synchronized void startServer() {
        if (ftpThread == null) {
            ftpThread = new Thread() {
                public void run() {
                    try {
                        startFtpServer();
                    } catch (Exception e) {
                        // log.error("Tomcat server startup failed ");
                    }
                }
            };
            ftpThread.start();
            isRunning = true;
        }
    }

    public void stop() {
        if (!isRunning) {
            log.info("Ftp server is running at the port " + ftpPort);
            return;
        }
        ftpServer.stop();
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void run() {
        try {
            startFtpServer();
        } catch (Exception e) {
            log.error("Server startup failed :" + e.getMessage());
        }
    }

}
