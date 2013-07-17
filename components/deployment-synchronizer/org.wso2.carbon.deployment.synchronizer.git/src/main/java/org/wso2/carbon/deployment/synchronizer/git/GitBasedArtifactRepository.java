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

package org.wso2.carbon.deployment.synchronizer.git;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.synchronizer.ArtifactRepository;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.RepositoryInformation;
import org.wso2.carbon.deployment.synchronizer.RepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSyncronizerConfiguration;
import org.wso2.carbon.deployment.synchronizer.git.repository_creator.GitBlitBasedRepositoryCreator;
import org.wso2.carbon.deployment.synchronizer.git.stratos2.S2GitRepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.util.Utilities;
import org.wso2.carbon.deployment.synchronizer.internal.DeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.internal.util.RepositoryConfigParameter;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Git based artifact repository
 */

public class GitBasedArtifactRepository implements ArtifactRepository {

    private static final Log log = LogFactory.getLog(GitBasedArtifactRepository.class);

    private RepositoryManager repositoryManager;
    private GitDeploymentSyncronizerConfiguration gitDepsyncConfig;

    /*
    * Constructor
    * */
    public GitBasedArtifactRepository () {

        if(!isGitDeploymentSynchronizationEnabled()) {
            return;
        }

        readConfiguration();

        if(gitDepsyncConfig.isStandardDeployment()) {
            repositoryManager = new DefaultGitRepositoryManager(new GitBlitBasedRepositoryCreator());
        }
        else {
            repositoryManager = new S2GitRepositoryManager();
        }
    }

    /**
     * Checks if git based deployment synchronization is enabled
     *
     * @return true if deployment synchronization is enabled, else false
     */
    private boolean isGitDeploymentSynchronizationEnabled() {

        String enableParam = readConfigurationParameter(GitDeploymentSynchronizerConstants.ENABLED);
        if (enableParam == null) {
            return false;
        }

        if (enableParam.equalsIgnoreCase("false")) {
            return  false;
        }

        if (enableParam.equalsIgnoreCase("true")) {
            String repoTypeParam = readConfigurationParameter(GitDeploymentSynchronizerConstants.REPOSITORY_TYPE);
            if(repoTypeParam != null && (repoTypeParam.equalsIgnoreCase(getRepositoryType()))) {
                 return true;
            }
        }

        return false;
    }

    /**
     * Reads the configuration
     */
    private void readConfiguration () {

        gitDepsyncConfig = new GitDeploymentSyncronizerConfiguration();

        String standardDeploymentParam = readConfigurationParameter(GitDeploymentSynchronizerConstants.DEPLOYMENT_METHOD);
        if (standardDeploymentParam != null && (standardDeploymentParam.equalsIgnoreCase("true") || standardDeploymentParam.equalsIgnoreCase("false"))) {
            gitDepsyncConfig.setStandardDeployment(Boolean.parseBoolean(standardDeploymentParam));
        }

    }

    /**
     * Reads the relevant configuration parameter
     *
     * @param parameterKey parameter key
     * @return configuration value
     */
    private String readConfigurationParameter(String parameterKey) {
        return ServerConfiguration.getInstance().getFirstProperty(parameterKey);
    }

    /**
     * Called at tenant load to do initialization related to the tenant
     *
     * @param tenantId id of the tenant
     * @throws DeploymentSynchronizerException in case of an error
     */
    public void init (int tenantId) throws DeploymentSynchronizerException {

        TenantGitRepositoryContext repoCtx = new TenantGitRepositoryContext();

        String gitLocalRepoPath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        repoCtx.setTenantId(tenantId);
        repoCtx.setLocalRepoPath(gitLocalRepoPath);

        FileRepository localRepo = null;
        try {
            localRepo = new FileRepository(new File(gitLocalRepoPath + "/.git"));

        } catch (IOException e) {
            handleError("Error creating git local repository for tenant " + tenantId, e);
        }

        repoCtx.setLocalRepo(localRepo);
        repoCtx.setGit(new Git(localRepo));
        repoCtx.setCloneExists(false);

        TenantGitRepositoryContextCache.getTenantRepositoryContextCache().cacheTenantGitRepoContext(tenantId, repoCtx);

        //provision a repository
        repositoryManager.provisionRepository(tenantId);
        //repositoryManager.addRepository(tenantId, url);

        repositoryManager.getUrlInformation(tenantId);
        repositoryManager.getCredentialsInformation(tenantId);
    }

    /**
     * Commits any changes in the local repository to the relevant remote repository
     *
     * @param localRepoPath tenant's local repository path
     * @return true if commit is successful, else false
     * @throws DeploymentSynchronizerException in case of an error
     */
    public boolean commit(int tenantId, String localRepoPath) throws DeploymentSynchronizerException {

        String gitRepoUrl = repositoryManager.getUrlInformation(tenantId).getUrl();
        if(gitRepoUrl == null) { //url not available
            log.warn ("Remote repository URL not available for tenant " + tenantId + ", aborting commit");
            return false;
        }

        TenantGitRepositoryContext gitRepoCtx = TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                retrieveCachedTenantGitContext(tenantId);

        Git git = gitRepoCtx.getGit();
        StatusCommand statusCmd = git.status();
        Status status;
        try {
            status = statusCmd.call();

        } catch (GitAPIException e) {
            log.error("Git status operation for tenant " + gitRepoCtx.getTenantId() + " failed, ", e);
            e.printStackTrace();
            return false;
        }

        if(status.isClean()) {//no changes, nothing to commit
            if(log.isDebugEnabled())
                log.debug("No changes detected in the local repository at " + localRepoPath);
            return false;
        }

        addArtifacts(gitRepoCtx, getNewArtifacts(status));
        addArtifacts(gitRepoCtx, getModifiedArtifacts(status));
        removeArtifacts(gitRepoCtx, getRemovedArtifacts(status));

        commitToLocalRepo(gitRepoCtx);
        pushToRemoteRepo(gitRepoCtx);

        return true;
    }

    /**
     * Returns the newly added artifact set relevant to the current status of the repository
     *
     * @param status git status
     * @return artifact names set
     */
    private Set<String> getNewArtifacts (Status status) {

        return status.getUntracked();
    }

    /**
     * Returns the removed (undeployed) artifact set relevant to the current status of the repository
     *
     * @param status git status
     * @return artifact names set
     */
    private Set<String> getRemovedArtifacts (Status status) {

        return status.getMissing();
    }

    /**
     * Return the modified artifacts set relevant to the current status of the repository
     *
     * @param status git status
     * @return artifact names set
     */
    private Set<String> getModifiedArtifacts (Status status) {

        return status.getModified();
    }

    /**
     * Adds the artifacts to the local staging area
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance
     * @param artifacts set of artifacts
     */
    private void addArtifacts (TenantGitRepositoryContext gitRepoCtx, Set<String> artifacts) {

        if(artifacts.isEmpty())
            return;

        AddCommand addCmd = gitRepoCtx.getGit().add();
        for (String artifact : artifacts) {
            addCmd.addFilepattern(artifact);
        }

        try {
            addCmd.call();

        } catch (GitAPIException e) {
            log.error("Adding artifact to the repository at " + gitRepoCtx.getLocalRepoPath() + "failed", e);
        }
    }

    /**
     * Removes the set of artifacts from local repo
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance
     * @param artifacts Set of artifact names to remove
     */
    private void removeArtifacts (TenantGitRepositoryContext gitRepoCtx, Set<String> artifacts) {

        if(artifacts.isEmpty())
            return;

        RmCommand rmCmd = gitRepoCtx.getGit().rm();
        for (String artifact : artifacts) {
            rmCmd.addFilepattern(artifact);
        }

        try {
            rmCmd.call();

        } catch (GitAPIException e) {
            log.error("Removing artifact from the repository at " + gitRepoCtx.getLocalRepoPath() + "failed", e);
        }
    }

    /**
     * Commits changes for a tenant to relevant the local repository
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for the tenant
     */
    private void commitToLocalRepo (TenantGitRepositoryContext gitRepoCtx) {

        CommitCommand commitCmd = gitRepoCtx.getGit().commit();
        commitCmd.setMessage("tenant " + gitRepoCtx.getTenantId() + "'s artifacts committed to repository at " +
                gitRepoCtx.getLocalRepoPath());

        try {
            commitCmd.call();

        } catch (GitAPIException e) {
            log.error("Committing artifacts to repository failed for tenant " + gitRepoCtx.getTenantId(), e);
        }
    }

    /**
     * Pushes the artifacts of the tenant to relevant remote repository
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for the tenant
     */
    private void pushToRemoteRepo(TenantGitRepositoryContext gitRepoCtx) {

        PushCommand pushCmd = gitRepoCtx.getGit().push();
        UsernamePasswordCredentialsProvider credentialsProvider = createCredentialsProvider(gitRepoCtx.getTenantId());
        if (credentialsProvider == null) {
            log.warn ("Remote repository credentials not available for tenant " + gitRepoCtx.getTenantId() +
                    ", aborting push");
            return;
        }
        pushCmd.setCredentialsProvider(credentialsProvider);

        try {
            pushCmd.call();

        } catch (GitAPIException e) {
            log.error("Pushing artifacts to remote repository failed for tenant " + gitRepoCtx.getTenantId(), e);
            e.printStackTrace();
        }
    }

    /**
     * Method inherited from ArtifactRepository for initializing checkout
     *
     * @param localRepoPath local repository path of the tenant
     * @return true if success, else false
     * @throws DeploymentSynchronizerException if an error occurs
     */
    public boolean checkout (int tenantId, String localRepoPath) throws DeploymentSynchronizerException {

        String gitRepoUrl = repositoryManager.getUrlInformation(tenantId).getUrl();
        if(gitRepoUrl == null) { //url not available
            log.warn ("Remote repository URL not available for tenant " + tenantId + ", aborting checkout");
            return false;
        }

        TenantGitRepositoryContext gitRepoCtx = TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                retrieveCachedTenantGitContext(tenantId);

        if(!gitRepoCtx.cloneExists()) {
            cloneRepository(gitRepoCtx);
        }

        return pullArtifacts(gitRepoCtx);
    }

    /**
     * Clones the remote repository to the local repository path
     *
     * @param gitRepoCtx TenantGitRepositoryContext for the tenant
     */
    private void cloneRepository (TenantGitRepositoryContext gitRepoCtx) { //should happen only at the beginning

        File gitRepoDir = new File(gitRepoCtx.getLocalRepoPath());
        if (gitRepoDir.exists()) {
            if(isValidGitRepo(gitRepoCtx)) { //check if a this is a valid git repo
                log.info("Existing git repository detected for tenant " + gitRepoCtx.getTenantId() +
                        ", no clone required");
                gitRepoCtx.setCloneExists(true);
                return;
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Repository for tenant " + gitRepoCtx.getTenantId() + " is not a valid git repo, will try to delete");
                }
                Utilities.deleteFolderStructure(gitRepoDir); //if not a valid git repo but non-empty, delete it (else the clone will not work)
            }
        }

        CloneCommand cloneCmd =  Git.cloneRepository().
                setURI(gitRepoCtx.getRemoteRepoUrl()).
                setDirectory(gitRepoDir).
                setBranch(GitDeploymentSynchronizerConstants.GIT_REFS_HEADS_MASTER);

        UsernamePasswordCredentialsProvider credentialsProvider = createCredentialsProvider(gitRepoCtx.getTenantId());
        if (credentialsProvider == null) {
            log.warn ("Remote repository credentials not available for tenant " + gitRepoCtx.getTenantId() +
                    ", aborting clone");
            return;
        }
        cloneCmd.setCredentialsProvider(credentialsProvider);

        try {
            cloneCmd.call();
            log.info("Git clone operation for tenant " + gitRepoCtx.getTenantId() + " successful");
            gitRepoCtx.setCloneExists(true);

        } catch (TransportException e) {
            log.error("Accessing remote git repository failed for tenant " + gitRepoCtx.getTenantId(), e);

        } catch (GitAPIException e) {
            log.error("Git clone operation for tenant " + gitRepoCtx.getTenantId() + " failed", e);
        }
    }

    /**
     * Pulling if any updates are available in the remote git repository. If basic authentication is required,
     * will call 'RepositoryInformationService' for credentials.
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for tenant
     * @return true if success, else false
     */
    private boolean pullArtifacts (TenantGitRepositoryContext gitRepoCtx) {

        PullCommand pullCmd = gitRepoCtx.getGit().pull();

        UsernamePasswordCredentialsProvider credentialsProvider = createCredentialsProvider(gitRepoCtx.getTenantId());
        if (credentialsProvider == null) {
            log.warn ("Remote repository credentials not available for tenant " + gitRepoCtx.getTenantId() +
                    ", aborting pull");
            return false;
        }
        pullCmd.setCredentialsProvider(credentialsProvider);

        try {
            pullCmd.call();

        } catch (InvalidConfigurationException e) {
            log.warn("Git pull unsuccessful for tenant " + gitRepoCtx.getTenantId() + ", " + e.getMessage());
            Utilities.deleteFolderStructure(new File(gitRepoCtx.getLocalRepoPath()));
            cloneRepository(gitRepoCtx);
            return true;

        } catch (JGitInternalException e) {
            log.warn("Git pull unsuccessful for tenant " + gitRepoCtx.getTenantId() + ", " + e.getMessage());
            return false;

        } catch (TransportException e) {
            log.error("Accessing remote git repository " + gitRepoCtx.getRemoteRepoUrl() + " failed for tenant " + gitRepoCtx.getTenantId(), e);
            return false;

        } catch (CheckoutConflictException e) { //TODO: handle conflict efficiently. Currently the whole directory is deleted and re-cloned
            log.warn("Git pull for the path " + e.getConflictingPaths().toString() + " failed due to conflicts");
            Utilities.deleteFolderStructure(new File(gitRepoCtx.getLocalRepoPath()));
            cloneRepository(gitRepoCtx);
            return true;

        } catch (GitAPIException e) {
            log.error("Git pull operation for tenant " + gitRepoCtx.getTenantId() + " failed", e);
            return false;
        }

        return true;
    }

    /**
     * Creates and return a UsernamePasswordCredentialsProvider instance for a tenant
     *
     * @param tenantId tenant Id
     * @return UsernamePasswordCredentialsProvider instance or null if username/password is not valid
     */
    private UsernamePasswordCredentialsProvider createCredentialsProvider (int tenantId) {

        RepositoryInformation repoInfo = repositoryManager.getCredentialsInformation(tenantId);
        if(repoInfo == null) {
            return null;
        }

        String userName = repoInfo.getUserName();
		String password = repoInfo.getPassword();

		if (userName!= null && password != null) {
			return new UsernamePasswordCredentialsProvider(userName, password);

        } else {
            return new UsernamePasswordCredentialsProvider("", "");
        }

    }

    /**
     * Checks if an existing local repository is a valid git repository
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance
     * @return true if a valid git repo, else false
     */
    private boolean isValidGitRepo (TenantGitRepositoryContext gitRepoCtx) {

        for (Ref ref : gitRepoCtx.getLocalRepo().getAllRefs().values()) { //check if has been previously cloned successfully, not empty
            if (ref.getObjectId() == null)
                continue;
            return true;
        }

        return false;
    }

    /**
     * Calls a utility method to extract the username from a json string
     *
     * @param repoInfoJsonString json format string
     *
     * @return username if exists, else an empty String
     */
    private String getUserName (String repoInfoJsonString) {
        return Utilities.getMatch(repoInfoJsonString,
                GitDeploymentSynchronizerConstants.USERNAME_REGEX, 1);
    }

    /**
     * Calls a utility method to extract the password from a json string
     *
     * @param repoInfoJsonString json format string
     *
     * @return password if exists, else an empty String
     */
    private String getPassword (String repoInfoJsonString) {
         return Utilities.getMatch(repoInfoJsonString,
                 GitDeploymentSynchronizerConstants.PASSWORD_REGEX, 1);
    }

    public void initAutoCheckout(boolean b) throws DeploymentSynchronizerException {
        //no implementation
    }

    public void cleanupAutoCheckout() {
        //no implementation
    }

    /**
     * Return the repository type
     *
     * @return repository type, i.e. git
     */
    public String getRepositoryType() {

        return DeploymentSynchronizerConstants.REPOSITORY_TYPE_GIT;
    }

    public List<RepositoryConfigParameter> getParameters() {

        return null;
    }

    /**
     * Partial checkout with defined depth. Currently not supported in GIT.
     *
     * @param tenantId tenant id
     * @param filePath local repository path
     * @param depth depth to checkout (0 - 3)
     * @return if success true, else false
     * @throws DeploymentSynchronizerException if an error occurs
     */
    public boolean checkout(int tenantId, String filePath, int depth) throws DeploymentSynchronizerException {

        return checkout(tenantId, filePath); //normal checkout is done
    }

    /**
     * Partial update with defined depth.Currently not supported in GIT.
     *
     * @param tenantId tenant Id
     * @param rootPath root path to the local repository
     * @param filePath path to sub directory to update
     * @param depth depth to update (0 - 3)
     * @return if success true, else false
     * @throws DeploymentSynchronizerException if an error occurs
     */
    public boolean update(int tenantId, String rootPath, String filePath, int depth) throws DeploymentSynchronizerException {

        return checkout(tenantId, rootPath); //normal checkout is done
    }

    /**
     * removed tenant's information from the cache
     *
     * @param tenantId tenant Id
     */
    public void cleanupTenantContext(int tenantId) {
        TenantGitRepositoryContextCache.getTenantRepositoryContextCache().
                removeCachedTenantGitContext(tenantId);
    }

    private void handleError (String errorMsg, Exception e) throws DeploymentSynchronizerException {
        log.error(errorMsg, e);
        throw new DeploymentSynchronizerException(errorMsg, e);
    }

}
