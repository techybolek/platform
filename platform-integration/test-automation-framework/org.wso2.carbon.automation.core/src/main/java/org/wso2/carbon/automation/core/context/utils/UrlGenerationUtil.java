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

package org.wso2.carbon.automation.core.context.utils;

import org.wso2.carbon.automation.core.context.AutomationContext;
import org.wso2.carbon.automation.core.context.platformcontext.Instance;

public class UrlGenerationUtil {

    AutomationContext automationContext;
    String nhttpsPort;
    String nhttpPort;
    String httpsPort;
    String httpPort;
    String hostName;
    String webContextRoot;
    String tenantDomain;
    boolean webContextEnabled=true;
    boolean portEnabled = true;
    boolean isRunningOnCloud = false;

    public UrlGenerationUtil
            (AutomationContext context, String instanceGroupName, String instanceName,
             String domain, String tenantUserId) {
        automationContext = new AutomationContext();
        automationContext = context;
        Instance automationInstance = new Instance();
              automationInstance=  automationContext.getPlatformContext()
                .getInstanceGroup(instanceGroupName).getInstance(instanceName);
        nhttpsPort = automationInstance.getNhttpsPort();
        nhttpPort = automationInstance.getNhttpPort();
        httpsPort = automationInstance.getHttpsPort();
        httpPort = automationInstance.getHttpPort();
        hostName = automationInstance.getHost();
        webContextRoot = automationInstance.getWebContext();
        automationContext.getUserManagerContext().getTenant(domain).getTenantUser(tenantUserId);
        if ((nhttpPort == null && nhttpsPort == null) && httpPort == null && httpsPort == null) {
            portEnabled = false;
        }
        if (automationContext.getConfigurationContext().getConfiguration()
                .getExecutionMode().equals("tenant")) {
            isRunningOnCloud = true;
        }
        tenantDomain = domain;
        if(webContextRoot!=null)
        {
            webContextEnabled= false;
        }
    }

    public String getHttpServiceURL() {
        if (isRunningOnCloud) {
            return getHttpServiceURLOfStratos();
        } else {
            return getHttpServiceURLOfProduct();
        }
    }

    public String getHttpsServiceURL() {
        if (isRunningOnCloud) {
            return getHttpsServiceURLOfStratos();
        } else {
            return getHttpsServiceURLOfProduct();
        }
    }

    public String getHttpServiceURLOfProduct() {
        String serviceURL;
        if (nhttpPort != null) {
            httpPort = nhttpPort;
        }

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpPort != null) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + webContextRoot
                        + "/" + "services";
            } else if (webContextRoot == null && httpPort != null) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services";
            } else if (webContextRoot == null) {
                serviceURL = "http://" + hostName + "/" + "services/";
            } else {
                serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services";
            }
        } else if (!portEnabled && webContextEnabled) {
            serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services";
        } else if (portEnabled && !webContextEnabled) {
            serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services";
        } else {
            serviceURL = "http://" + hostName + "/" + "services";
        }
        return serviceURL;

    }


    public String getHttpServiceURLOfStratos() {
        String serviceURL;


        if (nhttpPort == null) {
            if (portEnabled && webContextEnabled) {
                if (webContextRoot != null && httpPort != null) {
                    serviceURL = "http://" + hostName + ":" + httpPort + "/" + webContextRoot
                            + "/" + "services/t/" + tenantDomain;
                } else if (webContextRoot == null && httpPort != null) {
                    serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services/t/"
                            + tenantDomain;
                } else if (webContextRoot == null) {
                    serviceURL = "http://" + hostName + "/" + "services/t/" + tenantDomain;
                } else {
                    serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services/t/"
                            + tenantDomain;
                }
            } else if (!portEnabled && webContextEnabled) {
                serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services/t/"
                        + tenantDomain;
            } else if (portEnabled && !webContextEnabled) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services/t/"
                        + tenantDomain;
            } else {
                serviceURL = "http://" + hostName + "/" + "services/t/" + tenantDomain;
            }
        } else {
            if (webContextEnabled) {
                if (webContextRoot == null) {
                    serviceURL = "http://" + hostName + ":" + nhttpPort + "/" + "services/t/"
                            + tenantDomain;
                } else {
                    serviceURL = "http://" + hostName + ":" + nhttpPort + "/" + webContextRoot
                            + "/" + "services/t/" + tenantDomain;

                }

            } else {
                serviceURL = "http://" + hostName + ":" + nhttpPort + "/" + "services/t/"
                        + tenantDomain;
            }
        }
        return serviceURL;
    }

    public String getHttpsServiceURLOfProduct() {
        String serviceURL;

        if (nhttpsPort != null) {
            httpsPort = nhttpsPort;
        }

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                serviceURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot
                        + "/" + "services";
            } else if (webContextRoot == null && httpsPort != null) {
                serviceURL = "https://" + hostName + ":" + httpsPort + "/" + "services";
            } else if (webContextRoot == null) {
                serviceURL = "https://" + hostName + "/" + "services/";
            } else {
                serviceURL = "https://" + hostName + "/" + webContextRoot + "/" + "services";
            }
        } else if (!portEnabled && webContextEnabled) {
            serviceURL = "https://" + hostName + "/" + webContextRoot + "/" + "services";
        } else if (portEnabled && !webContextEnabled) {
            serviceURL = "https://" + hostName + ":" + httpsPort + "/" + "services";
        } else {
            serviceURL = "https://" + hostName + "/" + "services";
        }
        return serviceURL;

    }


    public String getHttpsServiceURLOfStratos() {
        String serviceURL;

        if (nhttpsPort == null) {
            if (portEnabled && webContextEnabled) {
                if (webContextRoot != null && httpsPort != null) {
                    serviceURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot
                            + "/" + "services/t/" + tenantDomain;
                } else if (webContextRoot == null && httpsPort != null) {
                    serviceURL = "https://" + hostName + ":" + httpsPort + "/" + "services/t/"
                            + tenantDomain;
                } else if (webContextRoot == null) {
                    serviceURL = "https://" + hostName + "/" + "services/t/" + tenantDomain;
                } else {
                    serviceURL = "https://" + hostName + "/" + webContextRoot + "/" + "services/t/"
                            + tenantDomain;
                }
            } else if (!portEnabled && webContextEnabled) {
                serviceURL = "https://" + hostName + "/" + webContextRoot + "/" + "services/t/"
                        + tenantDomain;
            } else if (portEnabled && !webContextEnabled) {
                serviceURL = "https://" + hostName + ":" + httpsPort + "/" + "services/t/"
                        + tenantDomain;
            } else {
                serviceURL = "https://" + hostName + "/" + "services/t/" + tenantDomain;
            }
        } else {
            if (webContextEnabled) {
                if (webContextRoot == null) {
                    serviceURL = "https://" + hostName + ":" + nhttpsPort + "/" + "services/t/"
                            + tenantDomain;
                } else {
                    serviceURL = "https://" + hostName + ":" + nhttpsPort + "/" + webContextRoot
                            + "/" + "services/t/" + tenantDomain;

                }

            } else {
                serviceURL = "https://" + hostName + ":" + nhttpsPort + "/" + "services/t/"
                        + tenantDomain;
            }
        }
        return serviceURL;
    }


    public String getBackendUrl() {
        String backendUrl;
        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                backendUrl = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot
                        + "/" + "services/";
            } else if (webContextRoot == null && httpsPort != null) {
                backendUrl = "https://" + hostName + ":" + httpsPort + "/" + "services/";
            } else if (webContextRoot == null) {
                backendUrl = "https://" + hostName + "/" + "services/";
            } else {
                backendUrl = "https://" + hostName + "/" + webContextRoot + "/" + "services/";
            }
        } else if (!portEnabled && webContextEnabled) {
            backendUrl = "https://" + hostName + "/" + webContextRoot + "/" + "services/";
        } else if (portEnabled && !webContextEnabled) {
            backendUrl = "https://" + hostName + ":" + httpsPort + "/" + "services/";
        } else {
            backendUrl = "https://" + hostName + "/" + "services/";
        }
        return backendUrl;
    }

    public String getWebAppURL() {
        String webAppURL;
        if (isRunningOnCloud) {
            if (portEnabled && httpPort != null) {
                webAppURL = "http://" + hostName + ":" + httpPort + "/t/" + tenantDomain + "/webapps";
            } else {
                webAppURL = "http://" + hostName + "/t/" + tenantDomain + "/webapps";
            }
        } else {
            if (portEnabled && httpPort != null) {
                webAppURL = "http://" + hostName + ":" + httpPort;
            } else {
                webAppURL = "http://" + hostName;
            }
        }
        return webAppURL;
    }
}
