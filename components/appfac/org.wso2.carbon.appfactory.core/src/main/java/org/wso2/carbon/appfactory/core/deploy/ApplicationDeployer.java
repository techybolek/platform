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

package org.wso2.carbon.appfactory.core.deploy;

import static org.wso2.carbon.appfactory.core.util.CommonUtil.getAdminUsername;
import static org.wso2.carbon.appfactory.core.util.CommonUtil.getServerAdminPassword;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Storage;
import org.wso2.carbon.appfactory.core.cache.AppVersionCache;
import org.wso2.carbon.appfactory.core.governance.RxtManager;
import org.wso2.carbon.appfactory.core.internal.ServiceHolder;
import org.wso2.carbon.appfactory.core.util.AppFactoryCoreUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import com.google.common.io.Files;

/**
 * This service will deploy an artifact (specified as a combination of
 * application, stage, version and revision) to a set of servers associated with
 * specified stage ( e.g. QA, PROD)
 */
public class ApplicationDeployer {

    private static final Log log = LogFactory.getLog(ApplicationDeployer.class);
    
    /**
	 * Service method to get the latest deployed build information.
	 * 
	 * 
	 * @throws AppFactoryException
	 */
    public String getDeployedArtifactInformation(String applicationId, String version, String stage) throws AppFactoryException{
		String buildNumber = "-1" ;
		
		RxtManager rxtManager  = new RxtManager();
		try {
			buildNumber = rxtManager.getAppVersionRxtValue(applicationId, version,"appversion_lastdeployedid");
		} catch (AppFactoryException e) {
			throw new AppFactoryException(e.getMessage());
		}
		
		return buildNumber ;
	}
    
    
    
    /**
	 * Service method to get the artifact information for the given applicationId.
	 * 
	 * 
	 * @param applicationId
	 * @throws AppFactoryException
	 */
    public List<Artifact> getArtifactInformation(String applicationId) throws AppFactoryException{
		RxtManager rxtManager  = new RxtManager();
		try {
			List<Artifact> artifacts = rxtManager.getAppVersionRxtForApplication(applicationId);			
			return artifacts;
			
		} catch (AppFactoryException e) {
			log.error("Error while retrieving artifat information from rxt");
			throw new AppFactoryException(e.getMessage());
		} catch (RegistryException e) {
			log.error("Error while retrieving artifat information from rxt");
			throw new AppFactoryException(e.getMessage());
        }
	}
    
    /**
	 * Service method to update the latest deployed build information.
	 * This service will be called from Jenkins when the deployment is done.
	 * 
	 * @throws AppFactoryException
	 */
	public void updateDeploymentInformation(String applicationId,String stage ,String version,String buildId) throws AppFactoryException {

		log.info("Deployment information updation service called.");
		RxtManager rxtManager = new RxtManager();
		rxtManager.updateAppVersionRxt(applicationId, version, "appversion_lastdeployedid", buildId);
		AppVersionCache.getAppVersionCache().clearCacheForAppId(applicationId);
		log.info("Deployment information successfuly updated ");
	}

    /**
     * Deploys the Artifact to specified stage.
     *
     * @param applicationId The application Id.
     * @param stage         The stage to deploy ( e.g. QA, PROD)
     * @param version       Version of the application
     * @return An array of {@link ArtifactDeploymentStatusBean} indicating the
     *         status of each deployment operation.
     * @throws AppFactoryException
     */
    public ArtifactDeploymentStatusBean[] deployArtifact(String applicationId,
                                                         String stage, String version,
                                                         String tagName, String deployAction)
            throws AppFactoryException {
    	log.info("Deploy artifacti is called...." + applicationId + " ," + stage + " version " + version + " tagname " + tagName + " deployArtifac " + deployAction);
        String key = AppFactoryConstants.DEPLOYMENT_STAGES + "." + stage + "." + AppFactoryConstants.DEPLOYMENT_URL;
        String[] deploymentServerUrls = ServiceHolder.getAppFactoryConfiguration().getProperties(key);

        if (deploymentServerUrls.length == 0) {
            handleException("No deployment paths are configured for stage:" + stage);
        }
        Storage storage = ServiceHolder.getStorage();

        // job name : <applicationId>-<version>-default
        String jobName = applicationId + '-' + version + '-' + "default";
        String applicationType = null;
        try {
            applicationType = getApplicationType(applicationId);
        } catch (RegistryException e) {
            String errorMsg = "Unable to find the application type for application id : " + applicationId;
            handleException(errorMsg,e);
        }

        log.info("Trying to deploy artifact with job name " + jobName);
        if("deploy".equals(deployAction)) {
            if (tagName == null || tagName.trim().isEmpty()) {
            	log.info("Calling deployLatestSuccessArtifact with jobName -" + jobName + " applicationType -" + applicationType + " stage" + stage);
                storage.deployLatestSuccessArtifact(jobName, applicationType, stage);
            } else {
            	log.info("Calling deployTaggedArtifact with jobName -" + jobName + " applicationType -" + applicationType + " stage" + stage + " tagname-" + tagName + " deplyAction -" + deployAction );
                storage.deployTaggedArtifact(jobName, applicationType, tagName, stage, deployAction);
            }
        } else if ("promote".equals(deployAction)) {
        	log.info("Calling deployTaggedArtifact with PROMOTE jobName -" + jobName + " applicationType -" + applicationType + " stage" + stage + " tagname-" + tagName + " deplyAction -" + deployAction );
            storage.deployTaggedArtifact(jobName, applicationType, tagName, stage, deployAction);
        } else if("rePromote".equals(deployAction)) {
            ServiceHolder.getStorage().deployPromotedArtifact(jobName, applicationType, stage);
        }
        return null;
    }

    public String getArtifactDetails(File file) throws AppFactoryException{
        String artifactDetails = null;
        String fileName;

        if (file == null) {
            return "Not Found";
        }

        fileName = file.getName();
        if (fileName.endsWith(".war")) {
            String artifactVersion = fileName.substring(fileName.indexOf('-') + 1,
                    fileName.indexOf(".war"));

            String artifactName = fileName.substring(0, (fileName.indexOf('-')));
            artifactDetails = artifactName + '-' + artifactVersion;
            return artifactDetails;

        } else if (fileName.endsWith(".car")) {
            fileName = file.getAbsolutePath();
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                String msg = "Unable to find file : " + fileName;
                log.error(msg,e);
                throw new AppFactoryException(msg,e);
            }

            ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry;

            try {
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String entryName = zipEntry.getName();
                    if (!(entryName.equals("artifacts.xml"))) {
                        //  byte[] buf = new byte[1024];
                        log.info("Name of  Zip Entry : " + entryName);
                        String artifactVersion = entryName.substring(entryName.indexOf('_') + 1);
                        String artifactName = entryName.substring(0, (entryName.indexOf('_')));
                        zipInputStream.close();
                        fileInputStream.close();

                        artifactDetails = artifactName + '-' + artifactVersion;
                        return artifactDetails;
                    }

                }
            } catch (IOException e) {
                String msg = "Unable to complete operation";
                log.error(msg,e);
                throw new AppFactoryException(msg,e);
            }
        }
        return artifactDetails;

    }

    public String getStage(String applicationId, String version) throws AppFactoryException {
        return new RxtManager().getStage(applicationId, version);
    }

    private String getApplicationType(String applicationId) throws RegistryException {
        try {
            String applicationType;
            RegistryService registryService = ServiceHolder.getRegistryService();
            UserRegistry userRegistry = registryService.getGovernanceSystemRegistry();
            Resource resource = userRegistry.get(AppFactoryConstants.REGISTRY_APPLICATION_PATH +
                    RegistryConstants.PATH_SEPARATOR + applicationId + RegistryConstants.PATH_SEPARATOR
                            + "appinfo");
            GovernanceUtils.loadGovernanceArtifacts(userRegistry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(
                    userRegistry, "application");
            GenericArtifact artifact = artifactManager
                    .getGenericArtifact(resource.getUUID());
            applicationType = artifact.getAttribute("application_type");
            return applicationType;
        } catch (RegistryException e) {
            log.error(e);
            throw e;
        }
    }

//    Is called from the jaggery app
    public String[] getTagNamesOfPersistedArtifacts(String applicationId, String version) throws AppFactoryException {
        // job name : <applicationId>-<version>-default
        String jobName = applicationId + '-' + version + '-' + "default";
        return ServiceHolder.getStorage().getTagNamesOfPersistedArtifacts(jobName);
    }

    private void handleException(String msg) throws AppFactoryException {
        log.error(msg);
        throw new AppFactoryException(msg);
    }

    private void handleException(String msg, Throwable throwable)
            throws AppFactoryException {
        log.error(msg, throwable);
        throw new AppFactoryException(msg, throwable);
    }

    private String getDeploymentHostFromUrl(String url) throws AppFactoryException {
        String hostName = null;
        try {
            URL deploymentURL = new URL(url);
            hostName = deploymentURL.getHost();
        } catch (MalformedURLException e) {
            handleException("Deployment url is malformed.", e);
        }

        return hostName;
    }

    /**
     * Deleting an application from given environment
     *
     * @param stage         Stage to identify the environment
     * @param applicationId Application ID which needs to delete
     * @return boolean
     * @throws AppFactoryException An error
     */
    public boolean unDeployArtifact(String stage, String applicationId, String version)
            throws AppFactoryException {
    
        String event = "Deleting application " + applicationId + " in version for " + version + ", from " + stage + " stage" ;
        log.info(event);
        
        
        /*String key = AppFactoryConstants.DEPLOYMENT_STAGES + "." + stage + "." + AppFactoryConstants.DEPLOYMENT_URL;

        String[] deploymentServerUrls = ServiceHolder.getAppFactoryConfiguration().getProperties(key);

        if (deploymentServerUrls.length == 0) {
            handleException("No deployment paths are configured for stage:" + stage);
        }
        */
        
        String applicationType;
        try {
            applicationType = getApplicationType(applicationId);
        } catch (RegistryException e) {
            String errorMsg = String.format("Unable to find the application type for application id: %s",
                    applicationId);
            log.error(errorMsg, e);
            throw new AppFactoryException(errorMsg, e);
        }

        /*
        if (AppFactoryConstants.FILE_TYPE_WAR.equals(applicationType)|| AppFactoryConstants.FILE_TYPE_JAXWS.equals(applicationType)|| AppFactoryConstants.FILE_TYPE_JAXRS.equals(applicationType) ) {
            // undeploy the webapp(war/jaxws/jaxrs file)
            deleteWebApp(applicationId, deploymentServerUrls,AppFactoryConstants.FILE_TYPE_WAR,version);
        } else if (AppFactoryConstants.FILE_TYPE_CAR.equals(applicationType)) {
            // un-deploy the cApp (car file)
            deleteCApp(applicationId, deploymentServerUrls,version);
        }else if(AppFactoryConstants.FILE_TYPE_JAGGERY.equals(applicationType)){
            deleteWebApp(applicationId, deploymentServerUrls,"",version);
        }else if(AppFactoryConstants.FILE_TYPE_ESB.equals(applicationType)){
            //deleteWebApp(applicationId, deploymentServerUrls,"");
        }else if(AppFactoryConstants.FILE_TYPE_DBS.equals(applicationType)){
        	deleteService(applicationId, deploymentServerUrls,"",version);
        }else if(AppFactoryConstants.FILE_TYPE_PHP.equals(applicationType)){
            //deleteWebApp(applicationId, deploymentServerUrls,"");
        }else if(AppFactoryConstants.FILE_TYPE_BPEL.equals(applicationType)){
            //deleteWebApp(applicationId, deploymentServerUrls,"");
        }else {
            handleException("Can not detect application type to delete the application");
        }
        */
        
        deleteFromDepSyncGitRepo(applicationId, version, applicationType, stage);
        
        return true;
    }

    protected String getParameterValue(Map metadata, String key) {
        if (metadata.get(key) == null) {
            return null;
        }
        if (metadata.get(key) instanceof String[]) {
            String[] values = (String[]) metadata.get(key);
            if (values.length > 0) {
                return values[0];
            }
            return null;
        } else if (metadata.get(key) instanceof String) {
            return metadata.get(key).toString();
        }

        return null;
    }

    protected String[] getParameterValues(Map metadata, String key) {
        if (metadata.get(key) == null) {
            return null;
        }
        if (metadata.get(key) instanceof String[]) {
            return (String[]) metadata.get(key);
        } else if (metadata.get(key) instanceof String) {
            return new String[]{metadata.get(key).toString()};
        }

        return null;
    }

    /**
     * Generate the repository URL (to commit the application artifact)
     * @param applicationId application Id
     * @param applicationType type of the application
     * @param stage the stage 
     * @return the repository URL
     */
    private String generateRepoUrl(String applicationId, String applicationType, String stage) {
        String baseUrl = getBaseUrl(applicationType, stage);
        String template = getUrlPattern(applicationType, stage);
        String gitRepoUrl = baseUrl + "git/" + template;
        return gitRepoUrl.replace("{@application_key}", applicationId).replace("{@stage}", stage);
    }


    
    /**
     * Reads {@link AppFactoryConfiguration} and returns the repository provider
     * URL pattern (of the repository).
     * if a configuration doesn't exists for a particular application type
     * default value will be returned ( - which is configured using '*' )
     * 
     * @param applicationType type of the application.
     * @param stage the stage/environment
     * @return the pattern
     */
    
    private String getUrlPattern(String applicationType, String stage) {
        String template = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."  + 
                stage + ".Deployer.ApplicationType." + applicationType + "RepositoryProvider.Property.URLPattern");
        
        if (StringUtils.isBlank(template)){
            //default to "*"
            template = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."  + stage + 
                    ".Deployer.ApplicationType.*.RepositoryProvider.Property.URLPattern" );
        }
        return template;
    }


    
    /**
     * Reads {@link AppFactoryConfiguration} and returns the repository provider
     * URL.
     * if a configuration doesn't exists for a particular application type
     * default value will be returned ( - which is configured using '*' )
     * 
     * @param applicationType type of the application.
     * @param stage the stage/environment
     * @return the URL
     */
    private String getBaseUrl(String applicationType, String stage) {
        String baseUrl =  ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."  + 
                                     stage + ".Deployer.ApplicationType." + applicationType + "RepositoryProvider.Property.BaseURL");
        if ( StringUtils.isBlank(baseUrl)){
            //default to "*"
            baseUrl = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."  + stage + 
                                                         ".Deployer.ApplicationType.*.RepositoryProvider.Property.BaseURL" );
        }
        return baseUrl;
    }
    
    /**
     * Reads {@link AppFactoryConfiguration} and returns the repository provider
     * user name.
     * if a configuration doesn't exists for a particular application type
     * default value will be returned ( - which is configured using '*' )
     * 
     * @param applicationType type of the application.
     * @param stage the stage/environment
     * @return the user name
     */
    private String getRepositoryProviderAdminUser(String applicationType, String stage){
        
        
        String adminUser =  ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."  + 
                stage + ".Deployer.ApplicationType." + applicationType + "RepositoryProvider.Property.AdminUserName");
        if ( StringUtils.isBlank(adminUser)){
            //default to "*"
            adminUser = ServiceHolder.getAppFactoryConfiguration().getFirstProperty("ApplicationDeployment.DeploymentStage."  + stage + 
                                    ".Deployer.ApplicationType.*.RepositoryProvider.Property.AdminUserName" );
        }
        return adminUser;
    }
    
    /**
     * Reads {@link AppFactoryConfiguration} and returns the repository provider
     * password.
     * if a configuration doesn't exists for a particular application type
     * default value will be returned ( - which is configured using '*' )
     * 
     * @param applicationType type of the application.
     * @param stage the stage/environment
     * @return the password
     */
    private String getRepositoryProviderAdminPassword(String applicationType, String stage){
       
        AppFactoryConfiguration appFactoryConfiguration = ServiceHolder.getAppFactoryConfiguration();
        
        String adminUser = appFactoryConfiguration.getFirstProperty("ApplicationDeployment.DeploymentStage."  + 
                stage + ".Deployer.ApplicationType." + 
                applicationType + "RepositoryProvider.Property.AdminPassword");
        
        if ( StringUtils.isBlank(adminUser)){
            //default to "*"
            adminUser = appFactoryConfiguration.getFirstProperty("ApplicationDeployment.DeploymentStage." 
                        + stage + ".Deployer.ApplicationType.*.RepositoryProvider.Property.AdminPassword" );
        }
        return adminUser;
    }
    
    /**
     * Deletes a artifact corresponding to specified application id, version, stage 
     * @param applicationId application Id
     * @param version version of the application that needs be undeployed.
     * @param applicationType type of the application (war, jaxrs etc)
     * @param stage currently deployed stage 
     * @throws AppFactoryException if an error occurs.
     */
    private void deleteFromDepSyncGitRepo(String applicationId, String version,
                                          String applicationType, String stage)
                                                                               throws AppFactoryException {

        String repoProviderAdminName = getRepositoryProviderAdminUser(applicationType, stage);
        String repoProviderAdminPassword =
                                           getRepositoryProviderAdminPassword(applicationType,
                                                                              stage);

        File applicationTempLocation = Files.createTempDir(); // new
                                                              // File(applicationTempPath);//
                                                              // <appid>/developement

        try {
            AppfactoryRepositoryClient repositoryClient = new AppfactoryRepositoryClient("git");
            String gitRepoUrl = generateRepoUrl(applicationId, applicationType, stage);
            repositoryClient.init(repoProviderAdminName, repoProviderAdminPassword);
            repositoryClient.checkOut(gitRepoUrl, applicationTempLocation);

            // dbs files are copied to multiple server locations

            String[] deployedServerPaths = getServerDeploymentPaths(applicationType);

            for (String serverPath : deployedServerPaths) {

                File applicationRootLocation = new File(applicationTempLocation, serverPath);
                log.info("applicationRootLocation : " + applicationRootLocation.getAbsolutePath());
                if (applicationRootLocation.isDirectory()) {
                    String fileExtension = getFileExtension(applicationType);
                    log.debug("search for a file corresponding to : " + applicationId +
                              " version :" + version + " extension" + fileExtension);
                    Collection<File> filesToDelete =
                                                     getFilesToDelete(applicationId, version, applicationRootLocation, fileExtension, applicationType);

                    for (File f : filesToDelete) {
                        log.debug("git removing the file : " + f.getAbsolutePath());
                        if (!repositoryClient.remove(gitRepoUrl, f,
                                                     "Undeploying the file : " + f.getName())) {
                            log.debug("unable to remove the file from git repository" +
                                      f.getAbsolutePath());
                        }
                    }
                } else {
                    log.error("unable to find correct directory structure in git repository : " +
                              applicationRootLocation.getAbsolutePath());
                }
            }

            log.debug("checking in git at : " + applicationTempLocation);
            repositoryClient.checkIn(gitRepoUrl, applicationTempLocation, "Undelpoying artifacts");
        } catch (AppFactoryException e) {

            String msg =
                         "Undeploying application failed: Unable to delete files from git repository application id: " +
                                 applicationId + " version :" + version + " stage : " + stage;
            handleException(msg, e);
        } finally {

            try {
                FileUtils.deleteDirectory(applicationTempLocation);
            } catch (IOException ioe) {
                // we ignore error of not being able to delete temporary
                // directory.
                log.error("Unable to delete the temporary directory after"
                          + " application demote operation, error will be ignored", ioe);
            }
        }
    }



	@SuppressWarnings("unchecked")
    private Collection<File> getFilesToDelete(String applicationId, String version,
	                                    File applicationRootLocation, String fileExtension,
	                                    String applicationType) {

		if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(applicationType)) {

			return FileUtils.listFiles(applicationRootLocation, new String[] { AppFactoryConstants.APPLICATION_TYPE_XML },
			                           true);

		} else {
			return FileUtils.listFiles(applicationRootLocation,
			                           new ArtifactFileFilter(applicationId, version, fileExtension),
			                           null);
		}
	}
    /**
     * Delete CApp from given deployment servers
     *
     * @param applicationId        application ID
     * @param deploymentServerUrls deployment servers
     * @throws AppFactoryException an error
     */
    private void deleteCApp(String applicationId, String[] deploymentServerUrls,String version) throws AppFactoryException {
        for (String deploymentServerUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = getDeploymentHostFromUrl(deploymentServerUrl);
                ApplicationDeleteClient applicationDeleteClient = new ApplicationDeleteClient(deploymentServerUrl);

                if (applicationDeleteClient.authenticate(getAdminUsername(applicationId), getServerAdminPassword(),
                        deploymentServerIp)) {
                    applicationDeleteClient.deleteCarbonApp(applicationId);
                    log.debug(applicationId + " is successfully undeployed.");
                } else {
                    handleException("Failed to login to " + deploymentServerIp + " to undeploy the artifact:" +
                            applicationId);
                }

            } catch (Exception e) {
                handleException("Error occurred when un-deploying car file for application ID : " + applicationId, e);

            }
        }
    }

    /**
     * Delete web application from given deployment servers
     *
     * @param applicationId        application ID
     * @param deploymentServerUrls deployment servers
     * @throws AppFactoryException an error
     */
    private void deleteWebApp(String applicationId, String[] deploymentServerUrls,String type,String version) throws AppFactoryException {
        for (String deploymentServerUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = getDeploymentHostFromUrl(deploymentServerUrl);
                ApplicationDeleteClient applicationDeleteClient = new ApplicationDeleteClient(deploymentServerUrl);

                if (applicationDeleteClient.authenticate(getAdminUsername(applicationId), getServerAdminPassword(),
                        deploymentServerIp)) {
                    applicationDeleteClient.deleteWebApp(applicationId,type,version);
                    log.info(applicationId + " is successfully undeployed.");
                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to undeploy the artifact:" + applicationId);
                }
            } catch (Exception e) {
                handleException("Error occurred when un-deploying war file for application ID : " + applicationId, e);

            }
        }
    }
    
    /**
     * Delete service from given deployment servers
     *
     * @param applicationId        application ID
     * @param deploymentServerUrls deployment servers
     * @throws AppFactoryException an error
     */
    private void deleteService(String applicationId, String[] deploymentServerUrls,String type,String version) throws AppFactoryException {
        for (String deploymentServerUrl : deploymentServerUrls) {
            try {
                String deploymentServerIp = getDeploymentHostFromUrl(deploymentServerUrl);
                ApplicationDeleteClient applicationDeleteClient = new ApplicationDeleteClient(deploymentServerUrl);

                if (applicationDeleteClient.authenticate(getAdminUsername(applicationId), getServerAdminPassword(),
                        deploymentServerIp)) {
                    applicationDeleteClient.deleteService(applicationId,type,version);
                    log.info(applicationId + " is successfully undeployed.");
                } else {
                    handleException("Failed to login to " + deploymentServerIp +
                            " to undeploy the artifact:" + applicationId);
                }
            } catch (Exception e) {
                handleException("Error occurred when un-deploying war file for application ID : " + applicationId, e);

            }
        }
    }

    /**
     * Reads {@link AppFactoryConfiguration} and returns a list of locations (or
     * most of the time one location) a specified application type is deployed
     * in a server
     * 
     * @param applicationType
     *            type of the application
     * @return an array of String
     */
    private String[] getServerDeploymentPaths(String applicationType) {
        String paths =
                       ServiceHolder.getAppFactoryConfiguration()
                                    .getFirstProperty("ApplicationType." + applicationType +
                                                              ".Property.ServerDeploymentPaths");
        return StringUtils.isNotBlank(paths) ? paths.trim().split(",") : null;
    }

    /**
     * Reads {@link AppFactoryConfiguration} and returns file extension of a particular application type 
     * @param applicationType type of the application ( war, jaxrs etc).
     * @return file extension.
     */
    private String getFileExtension(String applicationType) {
        String extension =
                           ServiceHolder.getAppFactoryConfiguration()
                                        .getFirstProperty("ApplicationType." + applicationType +
                                                                  ".Property.Extension");
        return StringUtils.isBlank(extension) || extension.equals("none") ? null : extension.trim();

    }

    /**
     * Used to filter artifact(s)/ corresponding to specified application id, version and file extension 
     * 
     */
    class ArtifactFileFilter implements IOFileFilter {
        /**
         * Name of the file.
         */
        private String fileName;

        /**
         * Constructor of the class.
         * @param applicationId application Id
         * @param version version
         * @param extension file extension
         */
        public ArtifactFileFilter(String applicationId, String version, String extension) {

            if (!version.equals("trunk")) { 
                fileName = applicationId + "-" + version;
            } else { //file naming convension for trunk is different.
                fileName = applicationId + "-SNAPSHOT";
            }

            fileName = fileName + "." + extension;
        }

        /**
         * Only files are accepted (not directories). they should match the expected file name.
         */
        @Override
        public boolean accept(File file) {
            return file.isFile() && file.getName().equals(fileName);
        }

        /**
         * No directories are accepted.
         */
        @Override
        public boolean accept(File dir, String name) {
            return false;
        }

    }
    
}
