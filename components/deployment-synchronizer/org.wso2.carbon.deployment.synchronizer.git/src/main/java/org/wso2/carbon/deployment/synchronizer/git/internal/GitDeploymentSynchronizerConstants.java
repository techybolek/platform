package org.wso2.carbon.deployment.synchronizer.git.internal;

import static org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER;

/**
 * Git based DeploymentSynchronizer constansts
 */
public class GitDeploymentSynchronizerConstants {

    //Git repo url related constansts
    //public static final String GITHUB_HTTP_REPO_URL_PREFIX = "http://github.com";
    public static final String GIT_HTTP_REPO_URL_PREFIX = "http://";
    //public static final String GITHUB_HTTPS_REPO_URL_PREFIX = "https://github.com";
    public static final String GIT_HTTPS_REPO_URL_PREFIX = "https://";
    public static final String GITHUB_READ_ONLY_REPO_URL_PREFIX = "git://github.com";
    public static final String GIT_REPO_SSH_URL_PREFIX = "ssh://";
    public static final String GIT_REPO_SSH_URL_SUBSTRING = "@";

    public static final String ENABLED = DEPLOYMENT_SYNCHRONIZER + ".Enabled";

    //EPR for the repository Information Service
    public static final String REPO_INFO_SERVICE_EPR = "RepoInfoServiceEpr";

    //CartridgeAlias property name from carbon.xml
    public static final String CARTRIDGE_ALIAS = "CartridgeAlias";

    //regular expressions for extracting username and password form json string
    public static final String USERNAME_REGEX = "username:(.*?),";
    public static final String PASSWORD_REGEX = "password:(.*?)}";

    //Configuration parameter names read from carbon.xml
    public static final String REPOSITORY_TYPE = DEPLOYMENT_SYNCHRONIZER + ".RepositoryType";
    public static final String DEPLOYMENT_METHOD = DEPLOYMENT_SYNCHRONIZER + ".StandardDeployment";
    public static final String GIT_SERVER = DEPLOYMENT_SYNCHRONIZER + ".GitServer";
    public static final String GIT_REPO_BASE_URL = DEPLOYMENT_SYNCHRONIZER + ".GitBaseUrl";
    public static final String GIT_USERNAME = DEPLOYMENT_SYNCHRONIZER + ".GitUserName";
    public static final String GIT_PASSWORD = DEPLOYMENT_SYNCHRONIZER + ".GitPassword";

    //Git based constants
    public static final String GIT_REFS_HEADS_MASTER = "refs/heads/master";

    //Git server implementations
    public static final String GITBLIT = "gitblit";
    public static final String SCM = "scm";

    //Gitblit based constants
    public static final String GITBLIT_ADMIN_USERNAME = "admin";
    public static final String GITBLIT_ADMIN_PASAWORD = "admin";

}
