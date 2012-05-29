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

package org.wso2.gs.integration.tests;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.dashboard.mgt.theme.stub.ThemeMgtServiceStub;

import java.io.File;


/*
GS- ThemeTestUtils  - Theme
*/

public class ThemeTestUtils {
    private static final Log log = LogFactory.getLog(ThemeTestUtils.class);

    /**
     * To get the file path for the theme.zip file.
     *
     * @param frameworkPath
     * @return filePath
     */
    public static String getGadgetResourcePath(String frameworkPath) {
        return frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".." +
                File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator +
                "resources" + File.separator + "gs-new-theme.zip";
    }

    /**
     * To return the service stub for ThemeMgtService.
     *
     * @param sessionCookie
     * @return ThemeMgtServiceStub
     */
    public static ThemeMgtServiceStub getThemeMgtServiceStub(String sessionCookie) {
        String serviceURL;
        serviceURL = FrameworkSettings.SERVICE_URL + "ThemeMgtService";
        ThemeMgtServiceStub themeMgtServiceStub = null;
        try {
            themeMgtServiceStub = new ThemeMgtServiceStub(serviceURL);
            ServiceClient client = themeMgtServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
            themeMgtServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(600000);
        } catch (AxisFault axisFault) {
            log.error("Unexpected exception thrown");
            axisFault.printStackTrace();
        }
        log.info("ThemeServiceStub created");
        return themeMgtServiceStub;
    }


}



