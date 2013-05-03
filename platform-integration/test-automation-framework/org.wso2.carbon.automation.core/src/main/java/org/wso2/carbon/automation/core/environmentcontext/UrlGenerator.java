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

package org.wso2.carbon.automation.core.environmentcontext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.Context;
import org.wso2.carbon.automation.core.globalcontext.frameworkcontextvariables.FrameworkContext;
import org.wso2.carbon.automation.core.globalcontext.GlobalContextInitiator;
import org.wso2.carbon.automation.core.utils.UserInfo;

 /*
 * This module includes URL generation logic for service url backend url and urls with web context
 * */

public class UrlGenerator {

    private static final Log log = LogFactory.getLog(UrlGenerator.class);

    public String getHttpServiceURL(Context context,
                                    FrameworkContext frameworkContext, UserInfo userInfo) {
        if (frameworkContext.getEnvironmentSettings().isRunningOnStratos()) {
            return getHttpServiceURLOfStratos(context, frameworkContext, userInfo);
        } else {
            return getHttpServiceURLOfProduct(context, frameworkContext);
        }
    }

    public String getHttpsServiceURL(Context context,
                                     FrameworkContext frameworkContext, UserInfo userInfo) {
        if (frameworkContext.getEnvironmentSettings().isRunningOnStratos()) {
            return getHttpsServiceURLOfStratos(context, frameworkContext, userInfo);
        } else {
            return getHttpsServiceURLOfProduct(context, frameworkContext);
        }
    }

    public String getHttpServiceURLOfProduct(Context context,
                                             FrameworkContext frameworkContext) {
        String serviceURL;
        String nhttpPort = context.getInstanceVariables().getNhttpPort();
        String httpPort = context.getInstanceVariables().getHttpPort();
        String hostName = context.getInstanceVariables().getHostName();
        String webContextRoot = context.getInstanceVariables().getWebContextRoot();
        boolean webContextEnabled = frameworkContext.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();
        if (nhttpPort != null) {
            httpPort = nhttpPort;
        }

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpPort != null) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + webContextRoot + "/" + "services";
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


    public String getHttpServiceURLOfStratos(Context context,
                                             FrameworkContext frameworkContext,
                                             UserInfo info) {
        String serviceURL;
        String nhttpPort = context.getInstanceVariables().getNhttpPort();
        String httpPort = context.getInstanceVariables().getHttpPort();
        String hostName = context.getInstanceVariables().getHostName();
        String webContextRoot = context.getInstanceVariables().getWebContextRoot();
        boolean webContextEnabled = frameworkContext.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();

        String superTenantID = "0";
        String tenantDomain;

        if (info.getUserId().equals(superTenantID)) { /*skip the domain if user is super admin */
            tenantDomain = null;
        } else {
            tenantDomain = info.getUserName().split("@")[1];
        }
        if (nhttpPort == null) {
            if (portEnabled && webContextEnabled) {
                if (webContextRoot != null && httpPort != null) {
                    serviceURL = "http://" + hostName + ":" + httpPort + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
                } else if (webContextRoot == null && httpPort != null) {
                    serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services/t/" + tenantDomain;
                } else if (webContextRoot == null) {
                    serviceURL = "http://" + hostName + "/" + "services/t/" + tenantDomain;
                } else {
                    serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
                }
            } else if (!portEnabled && webContextEnabled) {
                serviceURL = "http://" + hostName + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
            } else if (portEnabled && !webContextEnabled) {
                serviceURL = "http://" + hostName + ":" + httpPort + "/" + "services/t/" + tenantDomain;
            } else {
                serviceURL = "http://" + hostName + "/" + "services/t/" + tenantDomain;
            }
        } else {
            if (webContextEnabled) {
                if (webContextRoot == null) {
                    serviceURL = "http://" + hostName + ":" + nhttpPort + "/" + "services/t/" + tenantDomain;
                } else {
                    serviceURL = "http://" + hostName + ":" + nhttpPort + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;

                }

            } else {
                serviceURL = "http://" + hostName + ":" + nhttpPort + "/" + "services/t/" + tenantDomain;
            }
        }
        return serviceURL;
    }

    public String getHttpsServiceURLOfProduct(Context context,
                                              FrameworkContext frameworkContext) {
        String serviceURL;
        String nhttpsPort = context.getInstanceVariables().getNhttpsPort();
        String httpsPort = context.getInstanceVariables().getHttpsPort();
        String hostName = context.getInstanceVariables().getHostName();
        String webContextRoot = context.getInstanceVariables().getWebContextRoot();
        boolean webContextEnabled = frameworkContext.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();

        if (nhttpsPort != null) {
            httpsPort = nhttpsPort;
        }

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                serviceURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "services";
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


    public String getHttpsServiceURLOfStratos(Context context,
                                              FrameworkContext frameworkContext,
                                              UserInfo info) {
        String serviceURL;
        String nhttpsPort = context.getInstanceVariables().getNhttpsPort();
        String httpsPort = context.getInstanceVariables().getHttpsPort();
        String hostName = context.getInstanceVariables().getHostName();
        String webContextRoot = context.getInstanceVariables().getWebContextRoot();
        boolean webContextEnabled = frameworkContext.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();
        String superTenantID = "0";
        String tenantDomain;

        if (info.getUserId().equals(superTenantID)) { /*skip the domain if user is super admin */
            tenantDomain = null;
        } else {
            tenantDomain = info.getUserName().split("@")[1];
        }
        if (nhttpsPort == null) {
            if (portEnabled && webContextEnabled) {
                if (webContextRoot != null && httpsPort != null) {
                    serviceURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
                } else if (webContextRoot == null && httpsPort != null) {
                    serviceURL = "https://" + hostName + ":" + httpsPort + "/" + "services/t/" + tenantDomain;
                } else if (webContextRoot == null) {
                    serviceURL = "https://" + hostName + "/" + "services/t/" + tenantDomain;
                } else {
                    serviceURL = "https://" + hostName + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
                }
            } else if (!portEnabled && webContextEnabled) {
                serviceURL = "https://" + hostName + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;
            } else if (portEnabled && !webContextEnabled) {
                serviceURL = "https://" + hostName + ":" + httpsPort + "/" + "services/t/" + tenantDomain;
            } else {
                serviceURL = "https://" + hostName + "/" + "services/t/" + tenantDomain;
            }
        } else {
            if (webContextEnabled) {
                if (webContextRoot == null) {
                    serviceURL = "https://" + hostName + ":" + nhttpsPort + "/" + "services/t/" + tenantDomain;
                } else {
                    serviceURL = "https://" + hostName + ":" + nhttpsPort + "/" + webContextRoot + "/" + "services/t/" + tenantDomain;

                }

            } else {
                serviceURL = "https://" + hostName + ":" + nhttpsPort + "/" + "services/t/" + tenantDomain;
            }
        }
        return serviceURL;
    }


    public String getBackendUrl(Context context,FrameworkContext frameworkContext) {
        String backendUrl;
        boolean webContextEnabled = frameworkContext.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();
        String httpsPort = context.getInstanceVariables().getHttpsPort();
        String hostName = context.getInstanceVariables().getHostName();
        String webContextRoot = context.getInstanceVariables().getWebContextRoot();
        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                backendUrl = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "services/";
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

    public String getBackendUrl(String httpsPort, String hostName, String webContextRoot) {
        String backendUrl;
        FrameworkContext frameworkContext = new FrameworkContext();
        GlobalContextInitiator globalContextInitiator = new GlobalContextInitiator();
        frameworkContext=globalContextInitiator.getContext().getFrameworkContext();
        boolean webContextEnabled = frameworkContext.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                backendUrl = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "services/";
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

    public String getWebAppURL(Context context,
                               FrameworkContext frameworkContext, UserInfo user) {
        String webAppURL;
        String httpPort = context.getInstanceVariables().getHttpPort();
        String hostName = context.getInstanceVariables().getHostName();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();

        if (frameworkContext.getEnvironmentSettings().isRunningOnStratos()) {
            if (portEnabled && httpPort != null) {
                webAppURL = "http://" + hostName + ":" + httpPort + "/t/" + user.getUserName().split("@")[1] + "/webapps";
            } else {
                webAppURL = "http://" + hostName + "/t/" + user.getUserName().split("@")[1] + "/webapps";
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

    public String getRemoteRegistryURLOfProducts(Context context,FrameworkContext frameworkContext) {
        String remoteRegistryURL;
        String nhttpsPort = context.getInstanceVariables().getNhttpsPort();
        String httpsPort = context.getInstanceVariables().getHttpsPort();
        String hostName = context.getInstanceVariables().getHostName();
        String webContextRoot = context.getInstanceVariables().getWebContextRoot();
        boolean webContextEnabled = frameworkContext.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/" + "registry/";
            } else if (webContextRoot == null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
            } else if (webContextRoot == null) {
                remoteRegistryURL = "https://" + hostName + "/" + "services/";
            } else {
                remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "registry/";
            }
        } else if (!portEnabled && webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "registry/";
        } else if (portEnabled && !webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "registry/";
        } else {
            remoteRegistryURL = "https://" + hostName + "/" + "registry/";
        }
        return remoteRegistryURL;
    }

    public static String getRemoteRegistryURLOfStratos(Context context, FrameworkContext frameworkContext,
                                                       UserInfo info) {
        String remoteRegistryURL;
        String httpsPort = context.getInstanceVariables().getHttpsPort();
        String hostName = context.getInstanceVariables().getHostName();
        String webContextRoot = context.getInstanceVariables().getWebContextRoot();
        boolean webContextEnabled = frameworkContext.getEnvironmentSettings().isEnableCarbonWebContext();
        boolean portEnabled = frameworkContext.getEnvironmentSettings().isEnablePort();

        String superTenantID = "0";
        String tenantDomain;

        if (info.getUserId().equals(superTenantID)) { /*skip the domain if user is super admin */
            tenantDomain = null;
        } else {
            tenantDomain = info.getUserName().split("@")[1];
        }

        if (portEnabled && webContextEnabled) {
            if (webContextRoot != null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + webContextRoot + "/t/" + tenantDomain + "/registry/";
            } else if (webContextRoot == null && httpsPort != null) {
                remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "t/" + tenantDomain + "/registry/";
            } else if (webContextRoot == null) {
                remoteRegistryURL = "https://" + hostName + "/" + "t/" + tenantDomain + "/registry";
            } else {
                remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "t/" + tenantDomain + "/registry/";
            }
        } else if (!portEnabled && webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + "/" + webContextRoot + "/" + "t/" + tenantDomain + "/registry/";
        } else if (portEnabled && !webContextEnabled) {
            remoteRegistryURL = "https://" + hostName + ":" + httpsPort + "/" + "t/" + tenantDomain + "/registry/";
        } else {
            remoteRegistryURL = "https://" + hostName + "/" + "t/" + tenantDomain + "/registry/";
        }
        return remoteRegistryURL;
    }
}

