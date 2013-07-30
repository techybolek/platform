package org.wso2.carbon.appfactory.repository.mgt.service;

import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;

public class TenantRepositoryCreationService {
     
    private RepositoryManager repositoryManager;

    public TenantRepositoryCreationService() {
        this.repositoryManager = new RepositoryManager();
    }
    /**
     * Create the tenant repo in the repository
     * @param tenantId specify the tenant that the repo should be created for
     * @return true based on the success of the operation
     * @throws RepositoryMgtException 
     */
    public Boolean createTenantRepo(String tenantId,String type) throws RepositoryMgtException {
        
        Boolean result=repositoryManager.createTenantRepo(tenantId, type); 
        return result;
        
    }

}
