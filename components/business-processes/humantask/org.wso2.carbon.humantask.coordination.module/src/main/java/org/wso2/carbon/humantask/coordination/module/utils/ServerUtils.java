/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.humantask.coordination.module.utils;

import java.net.SocketException;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.humantask.coordination.module.HumanTaskCoordinationException;
import org.wso2.carbon.humantask.coordination.module.internal.HTCoordinationModuleContentHolder;
import org.wso2.carbon.humantask.core.configuration.HumanTaskServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


public class ServerUtils {

    private static Log log = LogFactory.getLog(ServerUtils.class);

    /**
     * Returns URL of the of the Task engine's Protocol Handler Admin service.
     * eg: https://localhost:9443/services/HumanTaskProtocolHandler/
     * @return HumanTask protocol handler admin service's url
     */
    public static String getTaskProtocolHandlerURL(ConfigurationContext serverConfigurationContext) throws HumanTaskCoordinationException {
        HumanTaskServerConfiguration serverConfig = HTCoordinationModuleContentHolder.getInstance().getHtServer().getServerConfig();
        String baseURL;
        if (serverConfig.isClusteredTaskEngines()) {
            baseURL = serverConfig.getLoadBalancerURL();
        } else {
            String scheme = CarbonConstants.HTTPS_TRANSPORT;
            String host;
            try {
                host = NetworkUtils.getLocalHostname();
            } catch (SocketException e) {
                log.error(e.getMessage(), e);
                throw new HumanTaskCoordinationException(e.getLocalizedMessage(), e);
            }

            int port = 9443;
            port = CarbonUtils.getTransportProxyPort(serverConfigurationContext, scheme);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(serverConfigurationContext, scheme);
            }
            baseURL =   scheme + "://" + host + ":" + port;
        }

        String webContext = ServerConfiguration.getInstance().getFirstProperty("WebContextRoot");
        if (webContext == null || webContext.equals("/")) {
            webContext = "";
        }

        String tenantDomain = "";
        try {
            tenantDomain = PrivilegedCarbonContext.getCurrentContext().getTenantDomain(true);
        } catch (Throwable e) {
            tenantDomain = null;
        }

        String protocolHandlerURL = baseURL + webContext + ((tenantDomain != null &&
                !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) ?
                "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain : "") +
                Constants.CARBON_ADMIN_SERVICE_CONTEXT_ROOT + "/"
                + Constants.HUMANTASK_ENGINE_COORDINATION_PROTOCOL_HANDLER_SERVICE;
        return protocolHandlerURL;
    }

}
