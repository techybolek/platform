package org.wso2.carbon.registry.juddi;

/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.juddi.model.UddiEntityPublisher;
import org.apache.juddi.v3.auth.JUDDIAuthenticator;
import org.apache.juddi.v3.error.ErrorMessage;
import org.uddi.api_v3.DispositionReport;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.uddi.utils.GovernanceUtil;
import org.wso2.carbon.user.core.UserStoreException;

public class JUDDICarbonAuthenticator extends JUDDIAuthenticator {

    private static final Log log = LogFactory.getLog(org.wso2.carbon.registry.juddi.JUDDICarbonAuthenticator.class);

    public JUDDICarbonAuthenticator() {
      super();
    }

    @Override
    public String authenticate(String s, String s1) throws org.apache.juddi.v3.error.AuthenticationException  {
        int tenantId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
        RegistryService registryService = GovernanceUtil.getRegistryService();
        try {
           if(registryService.getUserRealm(tenantId).getUserStoreManager().authenticate(s,s1)) {
              return super.authenticate(s,s1);
           } else {
               throw new org.apache.juddi.v3.error.AuthenticationException(
                       new ErrorMessage("Failed to authenticate the user " + s),new DispositionReport());
           }
        } catch (UserStoreException e) {
             log.error(" Error occurred while Registry UDDI server authenticating the user " + s + " " + e.getMessage());
             throw new org.apache.juddi.v3.error.AuthenticationException(new ErrorMessage(e.getMessage()),new DispositionReport());
        } catch (RegistryException e) {
            log.error(" Error occurred while Registry UDDI server authenticating the user " + s + " " + e.getMessage());
            throw new org.apache.juddi.v3.error.AuthenticationException(new ErrorMessage(e.getMessage()),new DispositionReport());
        }

    }

    @Override
    public UddiEntityPublisher identify(String s, String s1) throws org.apache.juddi.v3.error.AuthenticationException  {
          return super.identify(s,s1);
    }
}
