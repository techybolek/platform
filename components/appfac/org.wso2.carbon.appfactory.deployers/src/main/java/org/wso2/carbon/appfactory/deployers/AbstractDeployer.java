package org.wso2.carbon.appfactory.deployers;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.Deployer;
import org.wso2.carbon.appfactory.deployers.build.api.BuildStatusProvider;
import org.wso2.carbon.appfactory.deployers.notify.DeployNotifier;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class AbstractDeployer implements Deployer {

    private static final Log log = LogFactory.getLog(AbstractDeployer.class);
    protected String adminUserName;
    protected String adminPassword;
    protected String appfactoryServerURL;
    protected String storagePath;
    protected String tempPath;
    protected BuildStatusProvider buildStatusProvider;


    @SuppressWarnings("unused")
    public AbstractDeployer() {
    }

    /**
     * This will deploy the artifact in the given job with the specified tag name
     *
     * @param parameters request
     */
    public void deployTaggedArtifact(Map<String, String[]> parameters) throws Exception {
        String jobName = getParameter(parameters,AppFactoryConstants.JOB_NAME);
        String tagName = getParameter(parameters,AppFactoryConstants.TAG_NAME);
        String artifactType = getParameter(parameters,AppFactoryConstants.ARTIFACT_TYPE);
        String deployAction = getParameter(parameters,AppFactoryConstants.DEPLOY_ACTION);
        
        log.info("Deploying taggged artifact with jobName - " + jobName);
        //TODO: MOve this to a separate call from the client. : Promote last successful build.
        if (deployAction.equals("promote") || tagName.trim().isEmpty()) {
        	
            log.info("Since no tag name is specified latest successful build will be deployed.");
            deployLatestSuccessArtifact(parameters);
            log.info("Initial deployement was successfull");
            return;
        }

        try {
            deployTaggedArtifact(parameters, jobName, tagName, artifactType);
            if (deployAction.equals("promote")) {
                labelAsPromotedArtifact(jobName, tagName);
            }
        } catch (AppFactoryException e) {
            String msg="deployment of tagged artifact " + tagName + " failed for " + jobName;
            handleException(msg,e);
        }
    }

    private void deployTaggedArtifact(Map<String,String[]> parameters, String jobName, String tagName,
                                      String artifactType) throws AppFactoryException {
        String path = storagePath + File.separator + jobName + File.separator + tagName;
        File[] artifactToDeploy = getArtifact(path, artifactType);
        deploy(artifactType, artifactToDeploy, parameters,true);
    }

    /**
     * This method can be used to deploy a promoted artifact to a stage
     * We have used this method after first promote action of an application, so the artifact
     * deployed in first promote action will be deployed in the next promote action
     */
    public void deployPromotedArtifact(Map<String, String[]> parameters) throws Exception {
        String jobName = getParameter(parameters,AppFactoryConstants.JOB_NAME);
        String artifactType = getParameter(parameters, AppFactoryConstants.ARTIFACT_TYPE);
        String pathToPromotedArtifact = storagePath + File.separator + "PROMOTED" + File.separator + jobName;
//        File promotedArtifact = new File(pathToPromotedArtifact);
        log.info("Deploying Promoted artifact with jobName - " + jobName + " path to promoted artifacti is " + pathToPromotedArtifact);
        File[] fileToDeploy = getArtifact(pathToPromotedArtifact, artifactType);
        deploy(artifactType, fileToDeploy, parameters,false);
    }

    /**
     * This will deploy the latest successfully built artifact of the given job
     *
     * @param parameters request
     */
    public void deployLatestSuccessArtifact(Map<String, String[]> parameters) throws Exception {
        String jobName = getParameter(parameters,AppFactoryConstants.JOB_NAME);
        String artifactType = getParameter(parameters,AppFactoryConstants.ARTIFACT_TYPE);
        String stageName = getParameter(parameters,AppFactoryConstants.DEPLOY_STAGE);
        String deployAction = getParameter(parameters,AppFactoryConstants.DEPLOY_ACTION);

        log.info("Deplying Last Sucessful Artifact with job name - " +  jobName + " stageName -" + stageName + " deployAction -" + deployAction);
        if (deployAction == null || deployAction.isEmpty()) {
            deployAction = "deploy";
        }
//        We don't need to deployment server URL since this is read from the appfactory configurations
//        String[] deploymentServerUrls = req.getParameterValues(DEPLOYMENT_SERVER_URLS);

        try {
            String path = System.getenv("JENKINS_HOME") + File.separator + "jobs" + File.separator +
                    jobName + File.separator + "lastSuccessful";
            File lastSuccess = new File(path);

            // if no successful builds are there, we trigger a build first in order to deploy the
            // latest success artifact
            if (!lastSuccess.exists()) {
                log.info("No builds have been triggered for " + jobName + ". Building " + jobName +
                        " first to deploy the latest built artifact");
                String jenkinsUrl = parameters.get("rootPath")[0];
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new NameValuePair("isAutomatic", "false"));
                nameValuePairs.add(new NameValuePair("doDeploy",Boolean.toString(true)));
                nameValuePairs.add(new NameValuePair("deployAction", deployAction));
                nameValuePairs.add(new NameValuePair("deployStage", stageName));
                nameValuePairs.add(new NameValuePair("persistArtifact", String.valueOf(false)));

                String buildUrl = jenkinsUrl + "/job/" + jobName + "/buildWithParameters";
                triggerBuild(jobName, buildUrl, nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]));
                // since automatic build deploy the latest artifact of successful builds to the
                // server, return after triggering the build
                return;
            }
            File[] artifactToDeploy = getLastBuildArtifact(path, artifactType);
            if (deployAction.equalsIgnoreCase("promote")) {
                deploy(artifactType, artifactToDeploy, parameters,false);
                log.debug("Making last successful build as PROMOTED");
                labelLastSuccessAsPromoted(jobName, artifactType);
            } else {
                deploy(artifactType, artifactToDeploy, parameters,true);
            }
        } catch (AppFactoryException e) {
            String msg="deployment of latest success artifact failed for " + jobName;
            handleException(msg,e);
        }
    }

    /**
     * This method is used to build the specified job
     * build parameters are set in such a way that it does not execute any post build actions
     *
     * @param jobName  job that we need to build
     * @param buildUrl url used to trigger the build
     * @throws AppFactoryException
     */
    protected void triggerBuild(String jobName, String buildUrl, NameValuePair[] queryParameters) throws AppFactoryException {
        PostMethod buildMethod = new PostMethod(buildUrl);
        buildMethod.setDoAuthentication(true);
        if (queryParameters != null) {
            buildMethod.setQueryString(queryParameters);
        }
        HttpClient httpClient = new HttpClient();
        httpClient.getState().setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(getAdminUsername(),
                        getServerAdminPassword()));
        httpClient.getParams().setAuthenticationPreemptive(true);
        int httpStatusCode = -1;
        try {
            httpStatusCode = httpClient.executeMethod(buildMethod);

        } catch (Exception ex) {
            String errorMsg = String.format("Unable to start the build on job : %s",
                    jobName);
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg, ex);
        } finally {
            buildMethod.releaseConnection();
        }

        if (HttpStatus.SC_FORBIDDEN == httpStatusCode) {
            final String errorMsg = "Unable to start a build for job [".concat(jobName)
                    .concat("] due to invalid credentials.")
                    .concat("Jenkins returned, http status : [")
                    .concat(String.valueOf(httpStatusCode))
                    .concat("]");
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }

        if (HttpStatus.SC_NOT_FOUND == httpStatusCode) {
            final String errorMsg = "Unable to find the job [" + jobName + "Jenkins returned, " +
                    "http status : [" + httpStatusCode + "]";
            log.error(errorMsg);
            throw new AppFactoryException(errorMsg);
        }
    }

    protected void handleException(String msg) throws AppFactoryException {
        log.error(msg);
        throw new AppFactoryException(msg);
    }

    protected void handleException(String msg, Exception e) throws AppFactoryException {
        log.error(msg, e);
        throw new AppFactoryException(msg, e);
    }

    /**
     * This method will be used to retrieve the artifact in the given path
     *
     * @param path         path were artifact has been stored
     * @param artifactType artifact type (car/war)
     * @return the artifact
     * @throws AppFactoryException
     */
    protected File[] getArtifact(String path, String artifactType) throws AppFactoryException {
        String[] fileExtension = new String[0];

        List<File> fileList = new ArrayList<File>();
        if (AppFactoryConstants.APPLICATION_TYPE_JAXWS.equals(artifactType) ||
                AppFactoryConstants.APPLICATION_TYPE_JAXRS.equals(artifactType)) {
            fileExtension = new String[]{AppFactoryConstants.APPLICATION_TYPE_WAR};
        } else if (AppFactoryConstants.APPLICATION_TYPE_JAGGERY.equals(artifactType)) {
            fileExtension = new String[]{AppFactoryConstants.APPLICATION_TYPE_ZIP};
        } else if (AppFactoryConstants.APPLICATION_TYPE_DBS.equals(artifactType)) {
            fileExtension = new String[]{AppFactoryConstants.APPLICATION_TYPE_DBS,
                    AppFactoryConstants.APPLICATION_TYPE_XML};
        } else if (AppFactoryConstants.APPLICATION_TYPE_BPEL.equals(artifactType)) {
            fileExtension = new String[]{AppFactoryConstants.APPLICATION_TYPE_ZIP};
        } else if (AppFactoryConstants.APPLICATION_TYPE_PHP.equals(artifactType)) {
            File phpAppParentDirectory = new File(path + File.separator + "archive");
            for (File phpAppDir : phpAppParentDirectory.listFiles()) {
                if (phpAppDir.isDirectory() && phpAppDir.getName().contains("-")) {
                    fileList.add(phpAppDir.getAbsoluteFile());
                }
            }
        } else if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(artifactType)) {
            fileExtension = new String[]{AppFactoryConstants.APPLICATION_TYPE_XML};
        } else {
            fileExtension = new String[]{artifactType};
        }

        fileList.addAll((List<File>) FileUtils.listFiles(new File(path), fileExtension, true));

        if (!(fileList.size() > 0)) {
            log.error("No built artifact found");
            throw new AppFactoryException("No built artifact found");
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    /**
     * Returns the artifacts related to last build location.
     * @param path
     * @param artifactType
     * @return
     * @throws AppFactoryException
     */
	protected File[] getLastBuildArtifact(String path, String artifactType)
	                                                                       throws AppFactoryException {
		// Archive folder is considered for freestyle projects.
		if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(artifactType)) {
			path = path + File.separator + "archive";
		}
		return getArtifact(path, artifactType);
	}
	
    /**
     * Deploy the given artifact to the given server URLs
     *
     * @param artifactType      artifact type
     * @param artifactsToDeploy artifacts that needs to be deployed
     */

    protected void deploy(String artifactType, File[] artifactsToDeploy, Map<String, String[]> parameters,Boolean notify)
            throws AppFactoryException {
        DeployNotifier notifier = new DeployNotifier();

        String jobName = getParameter(parameters, AppFactoryConstants.JOB_NAME);
        String deployStage = getParameter(parameters, AppFactoryConstants.DEPLOY_STAGE);
        try{
        if (AppFactoryConstants.APPLICATION_TYPE_CAR.equals(artifactType)) {
//            We expect only one artifact here.
            File artifactToDeploy = artifactsToDeploy[0];
            DataHandler dataHandler = new DataHandler(new FileDataSource(artifactToDeploy));

            UploadedFileItem uploadedFileItem = new UploadedFileItem();
            uploadedFileItem.setDataHandler(dataHandler);
            uploadedFileItem.setFileName(artifactToDeploy.getName());
            uploadedFileItem.setFileType("jar");

            UploadedFileItem[] uploadedFileItems = {uploadedFileItem};

            deployCarbonApp(uploadedFileItems, parameters);

        } else if (AppFactoryConstants.APPLICATION_TYPE_WAR.equals(artifactType)) {
//            We expect only one artifact here.
            File artifactToDeploy = artifactsToDeploy[0];
            DataHandler dataHandler = new DataHandler(new FileDataSource(artifactToDeploy));

            WebappUploadData webappUploadData = new WebappUploadData();
            webappUploadData.setDataHandler(dataHandler);
            webappUploadData.setFileName(artifactToDeploy.getName());

            WebappUploadData[] webappUploadDataItems = {webappUploadData};
            uploadWebApp(webappUploadDataItems, parameters);

        } else if (AppFactoryConstants.APPLICATION_TYPE_JAXWS.equals(artifactType) ||
                AppFactoryConstants.APPLICATION_TYPE_JAXRS.equals(artifactType)) {
//            We expect only one artifact here.
            File artifactToDeploy = artifactsToDeploy[0];
            DataHandler dataHandler = new DataHandler(new FileDataSource(artifactToDeploy));

            WebappUploadData webappUploadData = new WebappUploadData();
            webappUploadData.setDataHandler(dataHandler);
            webappUploadData.setFileName(artifactToDeploy.getName());

            WebappUploadData[] webappUploadDataItems = {webappUploadData};
            uploadJaxWebApp(webappUploadDataItems, parameters);

        } else if (AppFactoryConstants.APPLICATION_TYPE_JAGGERY.equals(artifactType)) {
//            We expect only one artifact here.
            File artifactToDeploy = artifactsToDeploy[0];
            DataHandler dataHandler = new DataHandler(new FileDataSource(artifactToDeploy));

            org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData webappUploadData =
                    new org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData();
            webappUploadData.setDataHandler(dataHandler);
            webappUploadData.setFileName(artifactToDeploy.getName());

            org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData[] webappUploadDataItems = {webappUploadData};
            uploadJaggeryApp(webappUploadDataItems, parameters);


        } else if (AppFactoryConstants.APPLICATION_TYPE_DBS.equals(artifactType)) {
//            We expect 2 artifact artifact here.
            List<UploadItem> dataItems = new ArrayList<UploadItem>();
            for (File artifactToDeploy : artifactsToDeploy) {
                DataHandler dataHandler = new DataHandler(new FileDataSource(artifactToDeploy));

                UploadItem dataItem = new UploadItem();
                dataItem.setFileName(artifactToDeploy.getName());
                dataItem.setDataHandler(dataHandler);

                dataItems.add(dataItem);
            }
            uploadDBSApp(dataItems.toArray(new UploadItem[dataItems.size()]), parameters);

        } else if (AppFactoryConstants.APPLICATION_TYPE_BPEL.equals(artifactType)) {
//            We expect only one artifact here.
            File artifactToDeploy = artifactsToDeploy[0];
            DataHandler dataHandler = new DataHandler(new FileDataSource(artifactToDeploy));

            org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem uploadedData =
                    new org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem();
            uploadedData.setDataHandler(dataHandler);
            uploadedData.setFileName(artifactToDeploy.getName());
            uploadedData.setFileType(AppFactoryConstants.APPLICATION_TYPE_ZIP);

            org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem[] uploadedDataItems = {uploadedData};
            uploadBPEL(uploadedDataItems, parameters);

        } else if (AppFactoryConstants.APPLICATION_TYPE_PHP.equals(artifactType)) {
//            We expect only one artifact here.
            File artifactToDeploy = artifactsToDeploy[0];
            DataHandler dataHandler = new DataHandler(new FileDataSource(artifactToDeploy));

            UploadItem dataItem = new UploadItem();
            dataItem.setFileName(artifactToDeploy.getName());
            dataItem.setDataHandler(dataHandler);

            UploadItem[] uploadItems = {dataItem};
            uploadPHP(uploadItems, parameters);

        } else if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(artifactType)) {
////            We expect only one artifact here.
        	
        	ExtendedUploadItem[] uploadItems = new ExtendedUploadItem[artifactsToDeploy.length];
			for (int i = 0; i < uploadItems.length; i++) {
				File artifact = artifactsToDeploy[i];
				DataHandler dataHandler = new DataHandler(new FileDataSource(artifact));
				
				ExtendedUploadItem dataItem = new ExtendedUploadItem(dataHandler,artifact);
				dataItem.setFileName(artifact.getName());
				uploadItems[i] = dataItem;
			}  	
        	
        	uploadESBApp(uploadItems, parameters);            
            
        }
            log.info("Application Deployed Successfully. Job Name :" + jobName);
            if (notify){
                notifier.deployed(jobName,deployStage, adminUserName, adminPassword, appfactoryServerURL, buildStatusProvider);
            }
        }catch (AppFactoryException e){
           String msg="Application is not Deployed Successfully. Job Name :" + jobName;
           handleException(msg,e);

        }
    }

    protected String getAdminUsername() {
        return adminUserName;
    }

    protected String getAdminUsername(String applicationId) {
        return adminUserName + "@" + applicationId;
    }

    protected String getServerAdminPassword() {
        return adminPassword;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setAppfactoryServerURL(String appfactoryServerURL) {
        this.appfactoryServerURL = appfactoryServerURL;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
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

    public abstract void deployCarbonApp(UploadedFileItem[] uploadedFileItems, Map metadata)
            throws AppFactoryException;

    public abstract void uploadWebApp(WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException;

    public abstract void uploadJaxWebApp(WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException;

    public abstract void uploadJaggeryApp(
            org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException;

    public abstract void uploadDBSApp(UploadItem[] uploadData, Map metadata)
            throws AppFactoryException;

    public abstract void uploadPHP(UploadItem[] uploadData, Map metadata)
            throws AppFactoryException;

    public abstract void uploadBPEL(
            org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem uploadedFileItem[], Map metadata)
            throws AppFactoryException;

    public abstract void uploadESBApp(ExtendedUploadItem[] uploadData, Map metadata)
            throws AppFactoryException;


    /**
     * Method labels the last successful build as PROMOTED by copiting last sucessful build into PROMOTED location.
     *
     * @param jobName
     * @param artifactType
     * @throws AppFactoryException
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract void labelLastSuccessAsPromoted(String jobName, String artifactType)
            throws AppFactoryException, IOException, InterruptedException ;


    /**
     * Used to store the promoted artifact. This will store the artifact in jenkins storage
     *
     * @param jobName
     * @param tagName
     */
    public abstract void labelAsPromotedArtifact(String jobName, String tagName) ;

    private String getParameter(Map<String,String[]> parameters, String parameterName){
        if(!parameters.containsKey(parameterName)){
            return null;
        }else{
            String[] values = parameters.get(parameterName);
            if(values.length == 0){
                return null;
            }else{
                return values[0];
            }
        }
    }
}
