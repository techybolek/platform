/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.statistics.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.stub.WebappStatPublisherAdminStub;
import org.wso2.carbon.webapp.statistics.data.xsd.StatisticData;


import java.util.Locale;
import java.util.ResourceBundle;

public class WebappStatisticAdminClient {

    private static final Log log = LogFactory.getLog(WebappStatisticAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.statistics.ui.i18n.Resources";
    private WebappStatPublisherAdminStub adminStub;
    private ResourceBundle bundle;
    private StatisticData statisticData = null;

    public WebappStatisticAdminClient(String cookie,
                                      String backendServerURL,
                                      ConfigurationContext configCtx,
                                      Locale locale) throws AxisFault {

        String serviceURL = backendServerURL + "WebappStatPublisherAdmin";

        bundle = ResourceBundle.getBundle(BUNDLE, locale);
        adminStub = new WebappStatPublisherAdminStub(configCtx, serviceURL);

        ServiceClient client = adminStub._getServiceClient();
        Options option = new Options();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

 /*   public StatisticData getWebAppStatistics(){
           try{
               return adminStub.getStatisticData();
           } catch (RemoteException e) {
               e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
               return null;
           }
    }*/

    public void setWebappStatistics(String webApp){
          try{
              statisticData =   adminStub.getWebappRelatedData(webApp);
              if(statisticData==null){
                  statisticData = new StatisticData();
                  statisticData.setRequstCount(0);
                  statisticData.setResponseCount(0);
                  statisticData.setFaultCount(0);
                  statisticData.setMaximumResponseTime(0.0);
                  statisticData.setMinimumresponseTime(0.0);
                  statisticData.setAverageResponseTime(0.0);
              }
          }catch (Exception e){
            e.printStackTrace();
          }
    }

    public int getRequestCount() {
        return statisticData.getRequstCount();
    }

    public int getResponseCount() {
        return statisticData.getResponseCount();
    }

    public int getFaultCount() {
        return statisticData.getFaultCount();
    }

    public double getMaximumResponseTime() {
        return statisticData.getMaximumResponseTime();
    }

    public double getMinimumResponseTime() {
        return statisticData.getMinimumresponseTime();
    }

    public double getAverageResponseTime() {
        return statisticData.getAverageResponseTime();
    }
}
