/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.git.repository.provider;

import com.gitblit.Constants;
import com.gitblit.models.*;
import com.gitblit.utils.RpcUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.repository.provider.common.AbstractRepositoryProvider;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;

/**
 * GITBlit specific repository manager implementation for git
 */
public class GITBlitBasedGITRepositoryProvider extends AbstractRepositoryProvider {
    private static final Log log = LogFactory.getLog(GITBlitBasedGITRepositoryProvider.class);

    public static final String BASE_URL = "RepositoryProviderConfig.git.Property.BaseURL";
    public static final String GITBLIT_ADMIN_USERNAME =
            "RepositoryProviderConfig.git.Property.GitblitAdminUserName";
    public static final String GITBLIT_ADMIN_PASS =
            "RepositoryProviderConfig.git.Property.GitblitAdminPassword";
    public static final String REPO_TYPE = "git";

    private boolean isCreated = true;

    public static final String TYPE = "git";

    /**
     * {@inheritDoc}
     */
    @Override
    public String createRepository(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        CarbonContext ct=CarbonContext.getCurrentContext();
        String repoName=tenantDomain+"/"+applicationKey;
        String repoCreateUrl = config.getFirstProperty(BASE_URL);
        String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
        String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);
        //Create the gftblit repository model
        RepositoryModel model = new RepositoryModel();
        model.name = repoName;
        //authenticated users can clone, push and view the repository
        model.accessRestriction = Constants.AccessRestrictionType.VIEW;
        model.isBare=true; // TODO: temporaryly added for demo purpose, need to fixed with new gitblit
        try {
            isCreated = RpcUtils.createRepository(model, repoCreateUrl, adminUsername,
                                                  adminPassword.toCharArray());           
            if (isCreated) {
                String url = getAppRepositoryURL(applicationKey, tenantDomain);
                return url;
            } else {
                String msg = "Repository is not created for " + applicationKey + " due to remote server error";
                log.error(msg);
                throw new RepositoryMgtException(msg);
            }
        } catch (IOException e) {
            String msg = "Repository is not created for " + applicationKey + " due to " + e.getLocalizedMessage();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }
      
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAppRepositoryURL(String applicationKey, String tenantDomain) throws RepositoryMgtException {
        return config.getFirstProperty(BASE_URL) + REPO_TYPE + "/" + tenantDomain + "/" +applicationKey + ".git";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getType() {
        return TYPE;
    }
    
    public boolean createTenantRepo(String tenantId) throws RepositoryMgtException {
        String defaultTenantRepo=tenantId+"/defApp";
        String repoCreateUrl =
                               config.getFirstProperty(BASE_URL) +
                                       "rpc?req=CREATE_REPOSITORY&name=/" + defaultTenantRepo;
        String repoDeleteUrl =
                config.getFirstProperty(BASE_URL) +
                        "rpc?req=DELETE_REPOSITORY&name=/" + defaultTenantRepo;
        String adminUsername = config.getFirstProperty(GITBLIT_ADMIN_USERNAME);
        String adminPassword = config.getFirstProperty(GITBLIT_ADMIN_PASS);

        // Create the gitblit repository model
        RepositoryModel model = new RepositoryModel();
        model.name = defaultTenantRepo;
        // authenticated users can clone, push and view the repository
        model.accessRestriction = Constants.AccessRestrictionType.VIEW;

        try {
            isCreated =
                        RpcUtils.createRepository(model, repoCreateUrl, adminUsername,
                                                  adminPassword.toCharArray());

            if (isCreated) {
                //String url = getAppRepositoryURL(defaultTenantRepo);
                RpcUtils.deleteRepository(model, repoDeleteUrl, adminUsername,
                                          adminPassword.toCharArray());

                return true;
            } else {
                String msg =
                             "Tenant Repsitory is not created for " + tenantId +
                                     " due to remote server error";
                log.error(msg);
                throw new RepositoryMgtException(msg);
                

            }
        } catch (IOException e) {
            String msg =
                         "Tenant Repsitory is not created for " + tenantId + " due to " +
                                 e.getLocalizedMessage();
            log.error(msg, e);
            throw new RepositoryMgtException(msg, e);
        }

         

    }
}
