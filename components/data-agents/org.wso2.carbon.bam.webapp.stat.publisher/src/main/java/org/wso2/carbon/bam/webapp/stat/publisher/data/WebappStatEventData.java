/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.bam.webapp.stat.publisher.data;

import java.sql.Timestamp;

public class WebappStatEventData {
    private String webappName;
    private String webappOwnerTenant;
    private String webappVersion;
    private String userId;
    private String userTenant;
    private Timestamp timestamp;
    private String resourcePath;  //    httpServletRequest.getPathInfo()
    private String browser;
    private String browserVersion;
    private String operatingSystem;
    private String operatingSystemVersion;

    private String httpMethod; //GET, POST etc.
    private String contentType;
    private String responseContentType;
    private int responseHttpStatusCode;
    private String remoteAddress;
    private String referer;
    private String remoteUser;
    private String authType;
    private String userAgent;

    private long responseTime;

//    private String webappType;

    public String getWebappName() {
        return webappName;
    }

    public void setWebappName(String webappName) {
        this.webappName = webappName;
    }


    public String getWebappVersion() {
        return webappVersion;
    }

    public void setWebappVersion(String webappVersion) {
        this.webappVersion = webappVersion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWebappOwnerTenant() {
        return webappOwnerTenant;
    }

    public void setWebappOwnerTenant(String webappOwnerTenant) {
        this.webappOwnerTenant = webappOwnerTenant;
    }

    public String getUserTenant() {
        return userTenant;
    }

    public void setUserTenant(String userTenant) {
        this.userTenant = userTenant;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public int getResponseHttpStatusCode() {
        return responseHttpStatusCode;
    }

    public void setResponseHttpStatusCode(int responseHttpStatusCode) {
        this.responseHttpStatusCode = responseHttpStatusCode;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

//    public String getWebappType() {
//        return webappType;
//    }
//
//    public void setWebappType(String webappType) {
//        this.webappType = webappType;
//    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOperatingSystemVersion() {
        return operatingSystemVersion;
    }

    public void setOperatingSystemVersion(String operatingSystemVersion) {
        this.operatingSystemVersion = operatingSystemVersion;
    }
}
