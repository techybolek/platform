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

package org.wso2.carbon.automation.core.environmentcontext.environmentvariables;

/**
 * Object for instance variables
 **/

public class InstanceVariables implements java.io.Serializable {

    private String hostName;
    private String httpPort;
    private String httpsPort;
    private String webContextRoot;
    private String nhttpPort;
    private String nhttpsPort;
    private String qpidPort;
    private String backendUrl;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public String getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getWebContextRoot() {
        return webContextRoot;
    }

    public void setWebContextRoot(String webContextRoot) {
        this.webContextRoot = webContextRoot;
    }

    public String getNhttpPort() {
        return nhttpPort;
    }

    public void setNhttpPort(String nhttpPort) {
        this.nhttpPort = nhttpPort;
    }

    public String getNhttpsPort() {
        return nhttpsPort;
    }

    public void setNhttpsPort(String nhttpsPort) {
        this.nhttpsPort = nhttpsPort;
    }

    public String getQpidPort() {
        return qpidPort;
    }

    public void setQpidPort(String qpidPort) {
        this.qpidPort = qpidPort;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }
}
