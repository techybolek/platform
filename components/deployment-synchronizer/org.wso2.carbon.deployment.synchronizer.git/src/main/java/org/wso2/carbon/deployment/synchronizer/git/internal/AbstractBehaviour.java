/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.synchronizer.git.internal;

import org.wso2.carbon.deployment.synchronizer.RepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.TenantGitRepositoryContext;

/**
 * Defines abstractions for methods that are differ across various deployments
 */

public abstract class AbstractBehaviour {

    protected RepositoryManager repositoryManager;

    /**
     * Constructor
     *
     * @param repositoryManager RepositoryManager instance
     */
    public AbstractBehaviour (RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    /**
     * Synchronize any initial artifacts in the tenant's axis2 repository
     *
     * @param tenantGitRepoCtx TenantGitRepositoryContext instance for the tenant
     * @return true if successfully synched, else false
     */
    public abstract boolean syncInitialLocalArtifacts(TenantGitRepositoryContext tenantGitRepoCtx);

    /**
     * Specify sending/not sending SynchronizeRepositoryRequest from carbon kernel
     *
     * @return true if SynchronizeRepositoryRequest should be sent, else false
     */
    public boolean requireSynchronizeRepositoryRequest () {
        return true;
    }
}
