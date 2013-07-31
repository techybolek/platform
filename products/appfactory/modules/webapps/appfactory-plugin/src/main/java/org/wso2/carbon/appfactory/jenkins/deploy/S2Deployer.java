package org.wso2.carbon.appfactory.jenkins.deploy;


import hudson.FilePath;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.deployers.AbstractS2Deployer;
import org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager;
import org.wso2.carbon.appfactory.jenkins.api.JenkinsBuildStatusProvider;

import java.io.File;
import java.io.IOException;

public class S2Deployer extends AbstractS2Deployer {
    private static final Log log = LogFactory.getLog(S2Deployer.class) ;

    protected AppfactoryPluginManager.DescriptorImpl descriptor;

    public S2Deployer() {
        descriptor = new AppfactoryPluginManager.DescriptorImpl();
        super.setAdminUserName(descriptor.getAdminUserName());
        super.setAdminPassword(descriptor.getAdminPassword());
        super.setAppfactoryServerURL(descriptor.getAppfactoryServerURL());
        super.setStoragePath(descriptor.getStoragePath());
        super.setTempPath(descriptor.getTempPath());
        super.buildStatusProvider = new JenkinsBuildStatusProvider();
    }

    public void labelLastSuccessAsPromoted(String jobName, String artifactType)
            throws AppFactoryException, IOException, InterruptedException {
        String lastSucessBuildFilePath = System.getenv("JENKINS_HOME") + File.separator + "jobs" +
                File.separator + jobName + File.separator +
                "lastSuccessful";
        log.debug("Last success build path is :" + lastSucessBuildFilePath);

        String jobPromotedPath = descriptor.getStoragePath() + File.separator + "PROMOTED" + File.separator + jobName;
        String dest = jobPromotedPath + File.separator + "lastSuccessful"; 
        File toBeCleaned = new File(jobPromotedPath);

        if (toBeCleaned.exists()) {
            // since only one artifact can be promoted for a version
            FileUtils.cleanDirectory(toBeCleaned);
        }
        File destDir = new File(dest);
        if (!destDir.mkdirs()) {
            log.error("Unable to create promoted tag for job:" + jobName);
            throw new AppFactoryException("Error occured while creating dir for last successful as PROMOTED:" + jobName);
        }

        File[] lastSucessFiles = getLastBuildArtifact(lastSucessBuildFilePath, artifactType);
		for (File lastSucessFile : lastSucessFiles) {
			FilePath lastSuccessArtifactJenkinsPath = new FilePath(lastSucessFile);
			String promotedDestDirPath =
			                             destDir.getAbsolutePath() +
			                                     File.separator +
			                                     getPromotedDestinationPathForApplication(lastSucessFile.getParent(),
			                                                                              artifactType);

			File promotedDestDir = new File(promotedDestDirPath);
			FilePath promotedDestDirFilePath = new FilePath(promotedDestDir);
			if (!promotedDestDirFilePath.exists()) {
				promotedDestDirFilePath.mkdirs();
			}

			File destFile =
			                new File(promotedDestDir.getAbsolutePath() + File.separator +
			                         lastSuccessArtifactJenkinsPath.getName());

			// given tag is copied to
			// <jenkins-home>/storage/PROMOTED/<job-name>/<tag-name>/
			FilePath destinationFile = new FilePath(destFile);
			if (lastSuccessArtifactJenkinsPath.isDirectory()) {
				lastSuccessArtifactJenkinsPath.copyRecursiveTo(destinationFile);
			} else {
				lastSuccessArtifactJenkinsPath.copyTo(destinationFile);
			}
			log.info("labeled the lastSuccessful as PROMOTED");
		}
    }

   
	@Override
    public void labelAsPromotedArtifact(String jobName, String tagName) {

        try {

            String path = descriptor.getStoragePath() + File.separator + jobName + File.separator + tagName;
            FilePath tagPath = new FilePath(new File(path));

            String jobPromotedPath = descriptor.getStoragePath() + File.separator + "PROMOTED" + File.separator + jobName;
            String dest = jobPromotedPath + File.separator + tagName;

            File toBeCleaned = new File(jobPromotedPath);

            if (toBeCleaned.exists()) {
                // since only one artifact can be promoted for a version
                FileUtils.cleanDirectory(toBeCleaned);
            }

            File destDir = new File(dest);
            if (!destDir.mkdirs()) {
                log.error("Unable to create promoted tag for job:" + jobName + "tag:" + tagName);
            }
            // given tag is copied to <jenkins-home>/storage/PROMOTED/<job-name>/<tag-name>/
            tagPath.copyRecursiveTo(new FilePath(destDir));
            log.info("labeled the tag: " + tagName + " as PROMOTED");

        } catch (Exception e) {
            log.error("Error while labeling the tag: " + tagName + "as PROMOTED", e);
        }
    }
}
