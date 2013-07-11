/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * @scr.component name="identity.provider.mgt.service.component"" immediate="true"
 *
 */
package org.wso2.carbon.identity.provider.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.provider.mgt.IdPMetadataService;

public class IdentityProviderMgtServiceComponent {

    private static Log log = LogFactory.getLog(IdentityProviderMgtServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bundleCtx = ctxt.getBundleContext();
            bundleCtx.registerService(IdPMetadataService.class.getName(),new IdPMetadataService(),null);
            log.debug("Identity Provider Mgt bundle is activated");
        } catch (Throwable e){
            log.error("Error while activating Identity Provider Mgt bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("Identity Provider Mgt bundle is deactivated");
    }
}
