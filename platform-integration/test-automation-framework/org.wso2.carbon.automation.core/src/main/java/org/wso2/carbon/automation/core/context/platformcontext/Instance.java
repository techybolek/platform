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

package org.wso2.carbon.automation.core.context.platformcontext;

/*
 * Represents the data structure for Instance node in automation.xml
 */
public class Instance {

    private String name;
    private InstanceType type;
    private String host;
    private String httpPort;
    private String httpsPort;
    private String nhttpsPort;
    private String nhttpPort;
    private String webContext;


    public void setName(String name) {

        this.name = name;
    }

    public void setType(InstanceType type) {

        this.type = type;
    }

    public void setHost(String host) {

        this.host = host;
    }

    public void setHttpPort(String port) {

        this.httpPort = port;
    }

    public void setHttpsPort(String port) {

        this.httpsPort = port;
    }


    public void setNhttpsPort(String port) {

        this.nhttpsPort = port;
    }

    public void setNhttpPort(String port) {

        this.nhttpPort = port;
    }
    public void setWebContext(String context) {

        this.webContext = context;
    }

    public String getName() {

        return name;
    }

    public InstanceType getType() {

        return type;
    }

    public String getHost() {

        return host;
    }

    public String getHttpPort() {

        return httpPort;
    }

    public String getHttpsPort() {

        return httpsPort;

    }

    public String getNhttpPort() {

        return nhttpPort;
    }

    public String getNhttpsPort() {

        return nhttpsPort;

    }

    public String getWebContext() {

        return webContext;
    }

}


