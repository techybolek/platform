package org.wso2.carbon.hdfs.mgt.internal;

import java.io.IOException;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.hdfs.dataaccess.DataAccessService;
import org.wso2.carbon.hdfs.mgt.HDFSAdminComponentManager;
import org.wso2.carbon.hdfs.mgt.HDFSServerManagementException;
import org.wso2.carbon.hdfs.mgt.internal.util.HDFSInstanceCache;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

public class HDFSAdminAxis2ConfigContextObserver extends
		AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(HDFSAdminAxis2ConfigContextObserver.class);
    HDFSInstanceCache hdfsInstanceCache = HDFSInstanceCache.getInstance();
    
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        int tid = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tid);
        FileSystem hdfsInstance = getFileSystemInstance();
        try {
        	if(hdfsInstance != null){
        		hdfsInstanceCache.putTenantIdToFSEntry(tid, hdfsInstance);
        	}
        } catch (Exception e) {
            log.error("Error occurred while loading tenant RSS configurations ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
    	int tid = PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();
    	FileSystem hdfsInstance = hdfsInstanceCache.getFileSystemInstanceForTenantId(tid);
    	if(hdfsInstance != null)
    	{
    		try {
	            hdfsInstance.close();
            } catch (IOException e) {
            	 log.error("Error occurred while closinf HDFS file system for tenant "+tid, e);
            }
    	}
    }
    
    private FileSystem getFileSystemInstance()
    {
    	FileSystem hdfsFS = null;
    	 try {
			DataAccessService dataAccessService = HDFSAdminComponentManager
					.getInstance().getDataAccessService();
			hdfsFS = dataAccessService.mountCurrentUserFileSystem();
			
		} catch (HDFSServerManagementException e) {
			 log.error("Error occurred while initializing dataAccessService ", e);
		}catch (IOException e) {
			 log.error("Error occurred while mounting file system ", e);;
		}
    	 return hdfsFS;
    }

}
