/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.utils.dss;


import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.dssutils.SqlDataSourceUtil;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.SQLException;

public class DataServiceBaseTest {
    //    public Log log = LogFactory.getLog(getClass());
    public EnvironmentVariables dssServer;
    public UserInfo userInfo;

    public DataServiceBaseTest() throws Exception {
        userInfo = UserListCsvReader.getUserInfo(2);
        EnvironmentBuilder builder = new EnvironmentBuilder().dss(2);
        dssServer = builder.build().getDss();
    }

    public DataServiceBaseTest(int userId) throws Exception {
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().dss(userId);
        dssServer = builder.build().getDss();
    }

    public void cleanup() {
        userInfo = null;
        dssServer = null;
    }

    public String getServiceUrl(String serviceName) {
        return dssServer.getServiceUrl() + "/" + serviceName;
    }

    public String getServiceUrlHttps(String serviceName) {
        return "https://" + dssServer.getProductVariables().getHostName() + ":"
               + dssServer.getProductVariables().getHttpsPort() + "/services/" + serviceName;
    }

    public void deployService(String serviceName, OMElement dssConfiguration) throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        Assert.assertTrue(dssTest.uploadArtifact(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName,
                                                 new DataHandler(new ByteArrayDataSource(dssConfiguration.toString().getBytes()))),
                          "Service File Uploading failed");
        Assert.assertTrue(dssTest.isServiceDeployed(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName),
                          "Service Not Found, Deployment time out ");
    }

    public void deployService(String serviceName, DataHandler dssConfiguration)
            throws Exception {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        Assert.assertTrue(dssTest.uploadArtifact(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName,
                                                 dssConfiguration),
                          "Service File Uploading failed");
        Assert.assertTrue(dssTest.isServiceDeployed(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName),
                          "Service Not Found, Deployment time out ");
    }

    public void deleteService(String serviceName) throws RemoteException {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        dssTest.deleteService(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName);
        Assert.assertTrue(dssTest.isServiceDeleted(dssServer.getBackEndUrl(), dssServer.getSessionCookie(),
                                                   serviceName), "Service Deletion Failed");
    }

    public DataHandler createArtifact(String path, List<File> sqlFile)
            throws XMLStreamException, IOException, ClassNotFoundException, SQLException {
        SqlDataSourceUtil dataSource = new SqlDataSourceUtil(dssServer.getSessionCookie(), dssServer.getBackEndUrl(),
                                                             FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME),
                                                             Integer.parseInt(userInfo.getUserId()));
        dataSource.createDataSource(sqlFile);
        return dataSource.createArtifact(path);
    }

    public void gracefullyRestartServer() throws Exception {
        ServerAdminClient serverAdminClient = new ServerAdminClient(dssServer.getBackEndUrl(),
                                                                    userInfo.getUserName(),
                                                                    userInfo.getPassword());
        FrameworkProperties frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME);
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }

    public boolean isServiceDeployed(String serviceName) throws RemoteException {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        return dssTest.isServiceDeployed(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName);
    }

    public boolean isServiceFaulty(String serviceName) throws RemoteException {
        DSSTestCaseUtils dssTest = new DSSTestCaseUtils();
        return dssTest.isServiceFaulty(dssServer.getBackEndUrl(), dssServer.getSessionCookie(), serviceName);
    }

    public File selectSqlFile(String fileName) {

        String driver = FrameworkFactory.getFrameworkProperties(ProductConstant.DSS_SERVER_NAME)
                .getDataSource().get_dbDriverName();
        String type = "";
        if (driver.contains("h2")) {
            type = "h2";
        } else if (driver.contains("mysql")) {
            type = "MySql";
        } else if (driver.contains("oracle")) {
            type = "oracle";
        }

        return new File(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts"
                        + File.separator + "DSS" + File.separator + "sql" + File.separator
                        + type + File.separator + fileName);
    }

}