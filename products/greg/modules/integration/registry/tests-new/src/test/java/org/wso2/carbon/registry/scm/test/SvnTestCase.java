package org.wso2.carbon.registry.scm.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.util.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.server.admin.ServerAdminClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.ServerGroupManager;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.server.mgt.test.RegistryConfiguratorTestCase;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;

/**
 * Registry SCM mount test for SVN
 *
 *
 */
public class SvnTestCase {

    private static final Log log = LogFactory.getLog(SvnTestCase.class);
    private FrameworkProperties frameworkProperties;
    private ServerAdminClient serverAdminClient;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void init() throws Exception {
        int userId = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

        serverAdminClient =
                new ServerAdminClient(environment.getGreg().getBackEndUrl(),
                        userInfo.getUserName(), userInfo.getPassword());

        frameworkProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getProductVariables().getBackendUrl(),
                        userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = "wso2.greg")
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_user})
    public void testSetupServerEnvironment() throws Exception {
        addScmConfiguration();
        ServerGroupManager.getServerUtils().restartGracefully(serverAdminClient, frameworkProperties);
    }

    /**
     *  Get SCM mounted resource test
     *
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     */
    @Test(groups = "wso2.greg")
    public void testResourceExists() throws RemoteException, ResourceAdminServiceExceptionException {
        Assert.notNull(resourceAdminServiceClient.getTextContent("/_system/governance/policy/policy.xml"),
                "Mounted resource content should not be null");
    }


    private void addScmConfiguration() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        try {
            OMElement regConfig = RegistryConfiguratorTestCase.getRegistryXmlOmElement();
            String scmConfig = "<scm>" +
                    "        <connection checkOutURL=\"scm:svn:https://svn.wso2.org/repos/wso2/carbon/" +
                    "platform/trunk/platform-integration/platform-automated-test-suite/" +
                    "org.wso2.carbon.automation.test.repo/src/main/resources/artifacts/GREG/" +
                    "policy\" workingDir=\"" + getTempLocation() + "\" mountPoint=\"/_system/" +
                    "governance/policy\" checkInURL=\"\" readOnly=\"\" updateFrequency=\"\">" +
                    "                <username>anonymoususer</username>" +
                    "                <password>anonymoususer123</password>" +
                    "        </connection>" +
                    "    </scm>";

            OMElement scmConfigOMElement = AXIOMUtil.stringToOM(scmConfig);
            regConfig.addChild(scmConfigOMElement);

            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            scmConfigOMElement.serialize(writer);
            scmConfigOMElement.build();
            Thread.sleep(2000);
        } catch (Exception e) {
            log.error("registry.xml edit fails" + e.getMessage());
            throw new Exception("registry.xml edit fails" + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            fileOutputStream.close();
            assert writer != null;
            writer.flush();
        }
    }


    private String getRegistryXMLPath() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "registry.xml";
    }

    private String getTempLocation() {
        return CarbonUtils.getCarbonHome() + File.separator + "tmp";
    }
}
