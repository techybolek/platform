/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.entitlement.policy.publisher;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;

import java.util.Properties;

/**
 *
 */
public class CarbonPDPPublisher implements PolicyPublisherModule{

    @Override
    public void init(Properties properties) {

    }

    @Override
    public Properties loadProperties() {
        return new Properties();
    }

    @Override
    public String getModuleName() {
        return "PDP Publisher";
    }

    @Override
    public void publish(PolicyDTO policyDTO, String action) throws IdentityException {

        if(EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(action)){

        } else if(EntitlementConstants.PolicyPublish.ACTION_DELETE.equals(action)){

        } else if(EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(action)){

        } else if(EntitlementConstants.PolicyPublish.ACTION_ENABLE.equals(action)){

        }
    }
}
