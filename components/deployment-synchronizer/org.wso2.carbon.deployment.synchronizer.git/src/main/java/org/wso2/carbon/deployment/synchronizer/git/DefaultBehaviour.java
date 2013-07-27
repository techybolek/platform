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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerException;
import org.wso2.carbon.deployment.synchronizer.RepositoryManager;
import org.wso2.carbon.deployment.synchronizer.git.internal.AbstractBehaviour;
import org.wso2.carbon.deployment.synchronizer.git.internal.GitDeploymentSynchronizerConstants;
import org.wso2.carbon.deployment.synchronizer.git.util.GitUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Standard manager - worker specific behaviour
 */

public class DefaultBehaviour extends AbstractBehaviour {

    private static final Log log = LogFactory.getLog(DefaultBehaviour.class);

    /**
     * Constructor
     *
     * @param repositoryManager RepositoryManager instance
     */
    public DefaultBehaviour(RepositoryManager repositoryManager) {
        super(repositoryManager);
    }

    @Override
    public boolean syncInitialLocalArtifacts(TenantGitRepositoryContext gitRepoCtx) {

        boolean initialArtifactsSynched = false;
        File gitRepoDir = new File(gitRepoCtx.getLocalRepoPath());

        if(gitRepoDir.exists()) {
            //if not already a valid git repo
            if(!GitUtils.isValidGitRepo(gitRepoCtx.getLocalRepo())) {
                //check git status
                Status status = getGitStatus(gitRepoCtx);
                //if there are changes
                if(status != null && !status.isClean()) {
                    InitGitRepository(new File(gitRepoCtx.getLocalRepoPath()));
                    //add the remote repository (origin)
                    addRemoteOrigin(gitRepoCtx);
                    //add the new files (untracked files) is available,
                    // and commit to the local repo
                    if(!addArtifacts(gitRepoCtx, getNewArtifacts(status))) {
                        return false; //no new artifacts added
                    }
                    commitToLocalRepo(gitRepoCtx);
                    //push to remote repo
                    pushToRemoteRepo(gitRepoCtx);
                    if(log.isDebugEnabled()) {
                        log.debug("Initial artifacts of tenant " + gitRepoCtx.getTenantId() + " synched to repository "
                                + gitRepoCtx.getRemoteRepoUrl());
                    }
                    initialArtifactsSynched = true;
                }
            }
        }

        return initialArtifactsSynched;
    }

    /**
     * Queries the git status for the repository given by gitRepoCtx
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for the tenant
     * @return Status instance updated with relevant status information,
     *         null in an error in getting the status
     */
    private Status getGitStatus (TenantGitRepositoryContext gitRepoCtx) {

        Git git = gitRepoCtx.getGit();
        StatusCommand statusCmd = git.status();
        Status status;

        try {
            status = statusCmd.call();

        } catch (GitAPIException e) {
            log.error("Git status operation for tenant " + gitRepoCtx.getTenantId() + " failed, ", e);
            status = null;
        }

        return status;
    }

    /**
     * Initialize local git repository
     *
     * @param gitRepoDir directory in the local file system
     */
    private void InitGitRepository (File gitRepoDir) {

        try {
            Git.init().setDirectory(gitRepoDir).setBare(false).call();

        } catch (GitAPIException e) {
            String errorMsg = "Initializing local repo at " + gitRepoDir.getPath() + " failed";
            handleError(errorMsg, e);
        }
    }

    /**
     * Adds the remote repo url (origin) to the local repo
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance of tenant
     * @return true if successfully added the remote origin, else false
     */
    private boolean addRemoteOrigin (TenantGitRepositoryContext gitRepoCtx) {

        boolean remoteAdded;

        StoredConfig config = gitRepoCtx.getGit().getRepository().getConfig();
        config.setString(GitDeploymentSynchronizerConstants.REMOTE,
                GitDeploymentSynchronizerConstants.ORIGIN,
                GitDeploymentSynchronizerConstants.URL,
                gitRepoCtx.getRemoteRepoUrl());

        config.setString(GitDeploymentSynchronizerConstants.REMOTE,
                GitDeploymentSynchronizerConstants.ORIGIN,
                GitDeploymentSynchronizerConstants.FETCH,
                GitDeploymentSynchronizerConstants.FETCH_LOCATION);

        config.setString(GitDeploymentSynchronizerConstants.BRANCH,
                GitDeploymentSynchronizerConstants.MASTER,
                GitDeploymentSynchronizerConstants.REMOTE,
                GitDeploymentSynchronizerConstants.ORIGIN);

        config.setString(GitDeploymentSynchronizerConstants.BRANCH,
                GitDeploymentSynchronizerConstants.MASTER,
                GitDeploymentSynchronizerConstants.MERGE,
                GitDeploymentSynchronizerConstants.GIT_REFS_HEADS_MASTER);

        try {
            config.save();
            remoteAdded = true;

        } catch (IOException e) {
            log.error("Error in adding remote origin for tenant " + gitRepoCtx.getTenantId(), e);
            remoteAdded = false;
        }

        return remoteAdded;
    }

    /**
     * Returns new artifacts set relevant to the current status of the repository
     *
     * @param status Status instance
     * @return artifact names set
     */
    private Set<String> getNewArtifacts (Status status) {

        return status.getUntracked();
    }

    /**
     * Adds the artifacts to the local staging area
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance
     * @param artifacts set of artifacts
     * @return true if new artifacts were added
     */
    private boolean addArtifacts (TenantGitRepositoryContext gitRepoCtx, Set<String> artifacts) {

        if(artifacts.isEmpty()) {
            return false;
        }

        boolean newArtifactsAdded;
        AddCommand addCmd = gitRepoCtx.getGit().add();
        for (String artifact : artifacts) {
            addCmd.addFilepattern(artifact);
        }

        try {
            addCmd.call();
            newArtifactsAdded = true;

        } catch (GitAPIException e) {
            log.error("Adding artifact to the repository at " + gitRepoCtx.getLocalRepoPath() + "failed", e);
            newArtifactsAdded = false;
        }

        return newArtifactsAdded;
    }

    /**
     * Commits the artifacts to the relevant local repository
     *
     * @param gitRepoCtx TenantGitRepositoryContext instance for the tenant
     */
    private void commitToLocalRepo (TenantGitRepositoryContext gitRepoCtx) {

        CommitCommand commitCmd = gitRepoCtx.getGit().commit();
        commitCmd.setMessage("tenant " + gitRepoCtx.getTenantId() + "'s artifacts committed to repository at " +
                gitRepoCtx.getLocalRepoPath() + ", time stamp: " + System.currentTimeMillis());

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
        UsernamePasswordCredentialsProvider credentialsProvider = GitUtils.createCredentialsProvider(repositoryManager,
                gitRepoCtx.getTenantId());

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
        }
    }

    private void handleError (String errorMsg, Exception e) throws DeploymentSynchronizerException {
        log.error(errorMsg, e);
        throw new DeploymentSynchronizerException(errorMsg, e);
    }
}
