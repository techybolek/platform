/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appfactory.tenant.build.integration.uploder;

import java.io.File;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.webapp.mgt.stub.WebappAdminStub;
import org.wso2.carbon.webapp.mgt.stub.types.carbon.WebappUploadData;

/**
 * Uploder using the web service interface provided by App Server
 */
public class WebBasedBuildServerUploader implements BuildServerUploader{

	private String authCookie;
	
    private String backendServerURL;
	
    
    
    public WebBasedBuildServerUploader(String username, String password, String backendServerURL) throws Exception {
	    super();
	    this.backendServerURL = backendServerURL;
	    authenticate(username, password, new URL(backendServerURL).getHost());
	   // authenticate(username, password, backendServerURL);
	}

	/**
     * Authenticates the session using specified credentials
     * 
     * @param userName
     *            The user name
     * @param password
     *            The password
     * @param host
     *            the Staging server's hostname/ip
     * @return
     * @throws Exception
     */
    private void authenticate(String userName, String password, String host) throws Exception{
        String serviceURL = backendServerURL + "/AuthenticationAdmin";
        AuthenticationAdminStub authStub = new AuthenticationAdminStub(serviceURL);
        
        authStub._getServiceClient().getOptions().setManageSession(true);
        boolean authenticate = authStub.login(userName, password, host);
        
        if(authenticate){
        	authCookie =
                    (String) authStub._getServiceClient().getServiceContext()
                                     .getProperty(HTTPConstants.COOKIE_STRING);
        }else{
        	throw new IllegalArgumentException("Invalid Credentials provided");
        }        
    }
    
	@Override
    public void uploadBuildServerApp(File serverApp) throws Exception {
	    
	    String serviceURL = backendServerURL + "/WebappAdmin";;
        WebappAdminStub webappAdminStub = new WebappAdminStub(serviceURL);
        ServiceClient client = webappAdminStub._getServiceClient();
        
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                            authCookie);
        webappAdminStub.uploadWebapp(new WebappUploadData[]{getWebAppUploadDataItem(serverApp)});
    }

	private WebappUploadData getWebAppUploadDataItem(File fileToDeploy) {
	    
        DataHandler dataHandler = new DataHandler(new FileDataSource(fileToDeploy));
		
		WebappUploadData webappUploadData = new WebappUploadData();
        webappUploadData.setDataHandler(dataHandler);
        webappUploadData.setFileName(fileToDeploy.getName());
		return webappUploadData;
    }	

}
