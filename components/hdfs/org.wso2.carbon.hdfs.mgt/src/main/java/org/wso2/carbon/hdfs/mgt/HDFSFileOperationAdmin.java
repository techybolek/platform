package org.wso2.carbon.hdfs.mgt;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.wso2.carbon.hdfs.dataaccess.DataAccessService;

public class HDFSFileOperationAdmin extends HDFSAdmin {
	

	public boolean createFile(String filePath, byte[] fileContent)
			throws HDFSServerManagementException {
		DataAccessService dataAccessService = HDFSAdminComponentManager
				.getInstance().getDataAccessService();
		FileSystem hdfsFS = null;
		Path path = new Path(filePath);
		boolean fileExists = true;
		try {
			hdfsFS = dataAccessService.mountCurrentUserFileSystem();
			if (!hdfsFS.exists(path)) {
				FSDataOutputStream outputStream = hdfsFS.create(path);
				outputStream.write(fileContent);
				outputStream.close();
				return true;
			} else {
				fileExists = true;
			}
		} catch (IOException e) {
			handleException("Exception occured when creating file", e);
		}
		handleItemExistState(fileExists, true, false);
		return false;
	}

	public HDFSFileContent downloadFile(String filePath)
			throws HDFSServerManagementException {

		DataAccessService dataAccessService = HDFSAdminComponentManager
				.getInstance().getDataAccessService();
		FileSystem hdfsFS = null;
		FSDataInputStream inputStream = null;
		HDFSFileContent hdfsFileContent = new HDFSFileContent();
		DataHandler dataHandler = null;
		String mimeType = "application/octet-stream";
		try {
			hdfsFS = dataAccessService.mountCurrentUserFileSystem();
			if (hdfsFS.exists(new Path(filePath))) {
				inputStream = hdfsFS.open(new Path(filePath));
				ByteArrayDataSource ds = new ByteArrayDataSource(inputStream,
						mimeType);
				dataHandler = new DataHandler(ds);
				hdfsFileContent.setDataHandler(dataHandler);
			}
		} catch (IOException e) {
			e.printStackTrace(); 
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					handleException("Exception occured when closing input stream", e);
				}
			}
			if (hdfsFS != null) {
				try {
					hdfsFS.close();
				} catch (IOException e) {
					handleException("Exception occured when closing file system", e);
				}
			}
		}
		return hdfsFileContent;
	}
}
