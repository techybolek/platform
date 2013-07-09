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

package org.wso2.carbon.identity.entitlement.policy.version;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;

import java.util.Properties;

/**
 * This manages the policy versions
 */
public interface PolicyVersionManager {

    /**
     * init policy version handler
     *
     * @param properties
     */
    public void init(Properties properties);

    /**
     *
     * @param policyId
     * @param version
     * @return
     * @throws IdentityException
     */
    public PolicyDTO getPolicy(String policyId, int version) throws IdentityException;

    /**
     *
     * @param policyDTO
     * @return
     * @throws IdentityException
     */
    public int createVersion(PolicyDTO policyDTO) throws IdentityException;

    /**
     *
     * @param policyId
     * @throws IdentityException
     */
    public void deletePolicy(String policyId) throws IdentityException;
}
