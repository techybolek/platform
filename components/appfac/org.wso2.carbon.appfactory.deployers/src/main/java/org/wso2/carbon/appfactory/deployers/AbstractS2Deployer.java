package org.wso2.carbon.appfactory.deployers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.FileUtils;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.clients.AppfactoryRepositoryClient;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;

public abstract class AbstractS2Deployer extends AbstractDeployer {
	
	private static final Log log = LogFactory.getLog(AbstractS2Deployer.class);

    @Override
    public void deployCarbonApp(UploadedFileItem[] uploadedFileItems, Map metadata)
            throws AppFactoryException {
        addToGitRepo(uploadedFileItems[0].getFileName(), uploadedFileItems[0].getDataHandler(), metadata, "");

    }

    @Override
    public void uploadWebApp(WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException {
        addToGitRepo(webappUploadDatas[0].getFileName(), webappUploadDatas[0].getDataHandler(), metadata, "webapps");
    }

    @Override
    public void uploadJaxWebApp(WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException {
        addToGitRepo(webappUploadDatas[0].getFileName(), webappUploadDatas[0].getDataHandler(), metadata, "jaxwebapps");

    }

    @Override
    public void uploadJaggeryApp(org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData[] webappUploadDatas, Map metadata)
            throws AppFactoryException {
        addToGitRepo(webappUploadDatas[0].getFileName(), webappUploadDatas[0].getDataHandler(), metadata, "jaggeryapps");

    }

    private void addToGitRepo(String fileName, DataHandler dataHandler, Map metadata, String rootFolder)
            throws AppFactoryException {
        String applicationId = getParameterValue(metadata, AppFactoryConstants.APPLICATION_ID);
        String gitRepoUrl = generateRepoUrl(applicationId, metadata);
        String stageName = getParameterValue(metadata, AppFactoryConstants.DEPLOY_STAGE);

//        String defaultUser = descriptor.getAdminUserName();
//        String defaultPassword = descriptor.getAdminPassword();

//        This is a temporary code. the above commented should be used
        String applicationAdmin = getParameterValue(metadata, "Deployer.RepositoryProvider.Property.AdminUserName");
        String defaultPassword = getParameterValue(metadata, "Deployer.RepositoryProvider.Property.AdminPassword");

//        String applicationAdmin = defaultUser + "@" + applicationId;

        File tempLocation = new File(tempPath);
        if (!tempLocation.exists()) {
            if (!tempLocation.mkdir()) {
                String msg="Unable to create temp directory : " + tempPath ;
                handleException(msg);
            }
        }

        String applicationParentTempPath = tempPath + File.separator + applicationId;
        File applicationParentTempLocation = new File(applicationParentTempPath);
        if (!applicationParentTempLocation.exists()) {
            if (!applicationParentTempLocation.mkdir()) {
                String msg="Unable to create application temp directory: " + applicationParentTempLocation.getAbsolutePath();
                handleException(msg);
            }
        }

        String applicationTempPath = applicationParentTempPath + File.separator + stageName;
        File applicationTempLocation = new File(applicationTempPath);

        if (!applicationTempLocation.exists()) {
            log.debug("applicationTempLocation doesn't exists: " + applicationTempLocation.getAbsolutePath());
            if (!applicationTempLocation.mkdir()) {
                String msg="Unable to create application temp directory: " +
                        applicationTempLocation.getAbsolutePath();
                handleException(msg);
            }
        }

        AppfactoryRepositoryClient repositoryClient = new AppfactoryRepositoryClient("git");
        try {
            repositoryClient.init(applicationAdmin, defaultPassword);
            repositoryClient.checkOut(gitRepoUrl, applicationTempLocation);

            String applicationRootPath = applicationTempPath + File.separator + rootFolder;
            
            File applicationRootFile = new File(applicationRootPath);
            if (!applicationRootFile.exists()) {
                log.debug("applicationRootFile doesn't exists: " + applicationRootFile.getAbsolutePath());
                if (!applicationRootFile.mkdirs()) {
                    String msg="Unable to create application root path";
                    handleException(msg);
                }
            }

            String targetFilePath = applicationRootPath + File.separator + fileName;
            File targetFile = new File(targetFilePath);

            //If there is a file in repo, we delete it first
            if (targetFile.exists()) {
                repositoryClient.remove(gitRepoUrl, targetFile, "Removing the old file to add the new one");
                //repositoryClient.checkIn(gitRepoUrl, applicationTempLocation, "Removing the old file to add the new one");
                
                try {
                    targetFile = new File(targetFilePath);
                    // check weather directory exists.
                    if (!targetFile.getParentFile().isDirectory()) { 
                        log.debug("parent directory : " +
                                  targetFile.getParentFile().getAbsolutePath() +
                                  " doesn't exits creating again");
                        if (!targetFile.getParentFile().mkdirs()) {
                            throw new IOException("Unable to re-create " +
                                                  targetFile.getParentFile().getAbsolutePath());
                        }

                    }
                    if (!targetFile.createNewFile()) {
                        throw new IOException("unable re-create the target file : " +
                                              targetFile.getAbsolutePath());
                    }

                    if (targetFile.canWrite()) {
                        log.debug("Successfully re-created a writable file : " + targetFilePath);
                    } else {
                        String errorMsg = "re-created file is not writable: " + targetFilePath;
                        log.error(errorMsg);
                        throw new IOException(errorMsg);
                    }

                } catch (IOException e) {
                    log.error("Unable to create the new file after deleting the old: " + targetFile.getAbsolutePath(), e);
                    throw new AppFactoryException(e);
                }
            }
           
            copyFilesToGit(dataHandler, targetFile);
            
            repositoryClient.add(gitRepoUrl, new File(targetFilePath));
            repositoryClient.checkIn(gitRepoUrl, applicationTempLocation, "Adding the artifact to the repo");
        } catch (AppFactoryException e) {
            String msg = "Unable to copy files to git location";
            handleException(msg,e);
        }
    }

    private void copyFilesToGit(DataHandler datahandler, File destinationFile)
            throws AppFactoryException {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(destinationFile);
            
            datahandler.writeTo(fileOutputStream);
        } catch (FileNotFoundException e) {
            log.error(e);
            throw new AppFactoryException(e);
        } catch (IOException e) {
            log.error(e);
            throw new AppFactoryException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private String generateRepoUrl(String applicationId, Map metadata) {
        String baseUrl = getParameterValue(metadata, "Deployer.RepositoryProvider.Property.BaseURL");
        String template = getParameterValue(metadata, "Deployer.RepositoryProvider.Property.URLPattern");

        String gitRepoUrl = baseUrl + "git/" + template;
        return gitRepoUrl.replace("{@application_key}", applicationId).replace("{@stage}", getParameterValue(metadata, "deployStage"));
    }

    @Override
    public void uploadBPEL(org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem[] uploadedFileItem, Map metadata)
            throws AppFactoryException {
        addToGitRepo(uploadedFileItem[0].getFileName(), uploadedFileItem[0].getDataHandler(), metadata, "bpel");
    }

    @Override
    public void uploadDBSApp(UploadItem[] uploadData, Map metadata) throws AppFactoryException {
        String dbsFileName = null;
        for (UploadItem uploadItem : uploadData) {
            log.info("uploaded item: " + uploadItem.getFileName());
            if (uploadItem.getFileName().endsWith("dbs")) {
                addToGitRepo(uploadItem.getFileName(), uploadItem.getDataHandler(), metadata, "dataservices");
                dbsFileName = FileUtils.filename(uploadItem.getFileName());
                break;
//            }else if(uploadItem.getFileName().endsWith("xml")){
//                addToGitRepo(uploadItem.getFileName(), uploadItem.getDataHandler(), metadata, "servicemetafiles");
            }
        }
        if (dbsFileName != null) {
            for (UploadItem uploadItem : uploadData) {
                if (uploadItem.getFileName().startsWith(dbsFileName) &&
                        uploadItem.getFileName().endsWith(AppFactoryConstants.APPLICATION_TYPE_XML)) {
//                    This is the service xml file. It has the same name as the dbs file. So we are committing it here.
                    addToGitRepo(uploadItem.getFileName(), uploadItem.getDataHandler(), metadata, "servicemetafiles");
                    break;

                }
            }
        }
    }

    @Override
    public void uploadPHP(UploadItem[] uploadData, Map metadata) {
//        TODO: we have to implement this for S2
    }

	@Override
	public void uploadESBApp(ExtendedUploadItem[] uploadData, Map metadata)
            throws AppFactoryException {
		log.info("Extended Uploaded Data length - " + uploadData.length);
		for (ExtendedUploadItem uploadItem : uploadData) {
			log.info("Upload item " + uploadItem.getFileName());
			String filePath = uploadItem.getFile().getAbsolutePath().toLowerCase();
			log.info("Upload file item path is " + filePath);

			if (filePath.contains(AppFactoryConstants.ESB_ARTIFACT_PREFIX)) {
				String fileDir = uploadItem.getFile().getParent();
				String fileRepoLocation = fileDir.split(AppFactoryConstants.ESB_ARTIFACT_PREFIX)[1];
				log.info("repo file location is - " + fileRepoLocation);
				addToGitRepo(uploadItem.getFileName(), uploadItem.getDataHandler(), metadata,
				             AppFactoryConstants.ESB_ARTIFACT_DEPLOYMENT_PATH + fileRepoLocation);
			}
		}
	}

    @Override
    public void unDeployArtifact(Map<String, String[]> requestParameters) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
    
	protected String getPromotedDestinationPathForApplication(String filepath, String artifactType) {

		if (AppFactoryConstants.APPLICATION_TYPE_ESB.equals(artifactType)) {
			if (filepath.contains(AppFactoryConstants.ESB_ARTIFACT_PREFIX)) {
				String esbRelativeFilePath =
				                             filepath.split(AppFactoryConstants.ESB_ARTIFACT_PREFIX)[1];
				return AppFactoryConstants.ESB_ARTIFACT_PREFIX + File.separator + esbRelativeFilePath;
			}
		}
		return "";
	}
}

