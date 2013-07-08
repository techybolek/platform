/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.webapp.list.ui;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.stub.WebappStatPublisherAdminStub;
import org.wso2.carbon.bam.webapp.stat.publisher.stub.conf.ServiceEventingConfigData;

import java.rmi.RemoteException;
import java.util.Locale;
import java.util.ResourceBundle;

public class WebappStatPublisherAdminClient {

    private static final Log log = LogFactory.getLog(WebappStatPublisherAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.webapp.list.ui.i18n.Resources";
    private WebappStatPublisherAdminStub stub;
    private ResourceBundle bundle;

    public WebappStatPublisherAdminClient(String cookie, String backendServerURL,
                                          ConfigurationContext configContext, Locale locale)
            throws AxisFault {
        String serviceURL = backendServerURL + "WebappStatPublisherAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new WebappStatPublisherAdminStub(configContext, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public void setWebappConfigdata(String webappName, String value) {
        int val = 0;
        if (value.trim().contains("1")) {
            val = 1;
        }
        try {
            stub.setWebappConfiguration(webappName, val);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getWebappConfigData(String webappName) {
        try {
            return stub.getWebappConfiguration(webappName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
