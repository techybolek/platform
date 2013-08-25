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



package org.wso2.carbon.automation.core.context.toolcontext;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.ContextConstants;
import org.wso2.carbon.automation.core.utils.frameworkutils.productvariables.Selenium;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;

public class ToolContextFactory {

    private Selenium selenium;

    public ToolContextFactory(Selenium selenium) {
        this.selenium = selenium;
    }

    public ToolContextFactory() {
        //To change body of created methods use File | Settings | File Templates.
    }

    public ToolContext getToolContext(OMNode omNode) {
        ToolContext toolContext = new ToolContext();
        toolContext.setSelenium(selenium);

        return toolContext;


    }

    public void createConfiguration(OMElement nodeElement) {
    }
/*
*//**
     * create the tool list
     *//*

    public void getToolList(OMNode omNode) {

        Iterator children = ((OMElementImpl) omNode).getChildElements();
        OMNode element;
        while (children.hasNext()) {

            element = (OMNode) children.next();
            String toolName = ((OMElementImpl) element).getLocalName();

            //here check for the each tool

            if (toolName.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM)) {

                selenium = createSelenium(element);


            }


        }


    }

*//*
*
     * Create selenium tool object

*//*

    public Selenium createSelenium(OMNode seleniumNode) {

        Iterator seleniumProperties = ((OMElementImpl) seleniumNode).getChildElements();
        OMNode property;
        Selenium seleniumTool = new Selenium();

        //this map contains the list of the browsers                  s
        HashMap<String, Browser> browserList = new HashMap<String, Browser>();
        while (seleniumProperties.hasNext()) {

            property = (OMNode) seleniumProperties.next();
            String attribute = ((OMElementImpl) property).getLocalName();
            String attributeValue = ((OMElementImpl) property).getText();

            if (attribute.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_REMOTE_DRIVE_URL)) {

                seleniumTool.setRemoteDriverURL(attributeValue);
                seleniumTool.setRemoteDriverEnable(Boolean.parseBoolean(((OMElementImpl) property).getAttribute(QName.valueOf(ContextConstant.TOOL_CONTEXT_TOOL_SELENIUM_REMOTE_DRIVE_URL_ENABLE)).getAttributeValue()));
            } else if (attribute.equals(ContextConstant.TOOL_CONTEXT_TOOL_SELENIUM_BROWSER)) {
                Browser browser = createSeleniumBrowser(property);
                browserList.put(browser.getBrowserType(), browser);


            }


        }
        seleniumTool.setBrowserList(browserList);
        return seleniumTool;
    }


*//*
*
     * create browser object for the selenium tool

*//*


    public Browser createSeleniumBrowser(OMNode omNode) {

        Browser browser = new Browser();
        Iterator browserProperties = ((OMElementImpl) omNode).getChildElements();
        OMNode property;
        while (browserProperties.hasNext()) {

            property = (OMNode) browserProperties.next();
            String attribute = ((OMElementImpl) property).getLocalName();
            String attributeValue = ((OMElementImpl) property).getText();

            //set the browser properties
            if (attribute.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_BROWSER_TYPE)) {
                browser.setBrowserType(attributeValue);

            } else if (attribute.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_BROWSER_WEB_DRIVE_PATH)) {

                browser.setWebDriverPath(attributeValue);

                Boolean webDriverPathEnable = Boolean.parseBoolean(((OMElementImpl) property).getAttribute(QName.valueOf(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_BROWSER_WEB_DRIVER_PATH_ENABLE)).getAttributeValue());
                browser.setWebDriverEnabled(webDriverPathEnable);

            }
        }
        return browser;

    }


    public Selenium getSelenium() {
        return selenium;
    }*/
}
