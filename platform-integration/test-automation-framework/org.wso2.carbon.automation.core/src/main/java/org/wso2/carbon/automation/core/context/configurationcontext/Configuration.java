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
package org.wso2.carbon.automation.core.context.configurationcontext;

/*
 * this class represents the data structure of the configuration node in automation.xml file
 */
public class Configuration {


    private int deploymentDelay;
    private String executionEnvironment;
    private String executionMode;
    private boolean cloudEnabled;
    private boolean clustering;
    private boolean coverage;
    private boolean frameworkDashboard;

    public int getDeploymentDelay() {
        return deploymentDelay;
    }

    public void setDeploymentDelay(int deploymentDelay) {
        this.deploymentDelay = deploymentDelay;
    }

    public String getExecutionEnvironment() {
        return executionEnvironment;
    }

    public void setExecutionEnvironment(String executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }

    public boolean isCloudEnabled() {
        return cloudEnabled;
    }

    public void setCloudEnabled(boolean cloudEnabled) {
        this.cloudEnabled = cloudEnabled;
    }

    public boolean isClustering() {
        return clustering;
    }

    public void setClustering(boolean clustering) {
        this.clustering = clustering;
    }

    public boolean isCoverage() {
        return coverage;
    }

    public void setCoverage(boolean coverage) {
        this.coverage = coverage;
    }

    public boolean isFrameworkDashboard() {
        return frameworkDashboard;
    }

    public void setFrameworkDashboard(boolean frameworkDashboard) {
        this.frameworkDashboard = frameworkDashboard;
    }
}
