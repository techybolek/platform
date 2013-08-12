package org.wso2.carbon.appfactory.repository.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;

import java.util.Date;

public class TenantRepositoryCreationService {
     
    private RepositoryManager repositoryManager;
    private static final Log log = LogFactory.getLog(TenantRepositoryCreationService.class);
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

    /**
     * Create a repository for an application with type{svn,git}
     *
     * @param applicationKey  Application ID
     * @param type Repository type
     * @param tenantDomain Tenant domain of application
     * @return url for created repository
     * @throws RepositoryMgtException
     */
    public String createRepository(String applicationKey, String type, String tenantDomain)
            throws RepositoryMgtException {
        long s = new Date().getTime();
        String ss = repositoryManager.createRepository(applicationKey, type, tenantDomain);
        log.info("Repo Time : " + ((new Date().getTime()) - s));
        return ss;
    }

}
