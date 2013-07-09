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

package org.wso2.carbon.automation.core.customservers.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.File;
import java.io.IOException;

public class TomcatServerManager{
    private final static Log log = LogFactory.getLog(TomcatServerManager.class);
    Tomcat tomcat;
    boolean isRunning = false;
    private Thread tomcatThread = null;
    int tomcatPort;
    String tomcatClass= null;

    public TomcatServerManager(String className,String server, int port)
    {
             this.tomcatPort =port;
        this.tomcatClass = className;

    }

    public void startJaxRsServer() throws Exception {
        final File base = createBaseDirectory();
        log.info("Using base folder: " + base.getAbsolutePath());

      tomcat = new Tomcat();
        tomcat.setPort(tomcatPort);
        tomcat.setBaseDir(base.getAbsolutePath());

        Context context = tomcat.addContext("/", base.getAbsolutePath());
        Tomcat.addServlet(context, "CXFServlet", new CXFServlet());

        context.addServletMapping("/rest/*", "CXFServlet");
        context.addApplicationListener(ContextLoaderListener.class.getName());
        context.setLoader(new WebappLoader(Thread.currentThread().getContextClassLoader()));

        context.addParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());
        context.addParameter("contextConfigLocation", tomcatClass);

        tomcat.start();
        tomcat.getServer().await();
    }

    public synchronized  void startServer()
    {
        if(tomcatThread==null)
        {
            tomcatThread= new Thread(){
                public void run() {
                    try {
                        startJaxRsServer();
                    } catch (Exception e) {
                        log.error("Tomcat server startup failed ");
                    }
                }
            };
            tomcatThread.start();
            isRunning = true;
        }
    }


    private File createBaseDirectory() throws IOException {
        final File base = File.createTempFile("jaxrs-tmp-", "", new File("/home/dharshana/wso2source/carbon/platform/trunk/platform-integration/test-automation-framework/org.wso2.carbon.automation.core/src/main/resources"));

        if (!base.delete()) {
            throw new IOException("Cannot (re)create base folder: " + base.getAbsolutePath());
        }

        if (!base.mkdir()) {
            throw new IOException("Cannot create base folder: " + base.getAbsolutePath());
        }
        return base;
    }

    public void stop() throws LifecycleException {
        if(!isRunning) {
            //LOG.warn("Tomcat server is not running @ port={}", port);
            return;
        }

       // if(isInfo) LOG.info("Stopping the Tomcat server");

        tomcat.stop();
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void run() {
        try {
            startJaxRsServer();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
