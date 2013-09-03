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


import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;

public class ToolContextFactory {


    ToolContext toolContext;



    public ToolContextFactory() {
        toolContext = new ToolContext();

    }

    public ToolContext getToolContext() {

        return toolContext;
    }


    /**
     * create the tool context
     *
     * @param nodeElement OMElement input from the xml reader
     */
    public void createToolContext(OMElement nodeElement) {

        Iterator children = nodeElement.getChildElements();
        OMElement element;
        while (children.hasNext()) {

            element = (OMElement) children.next();
            String toolName = element.getLocalName();

            //here check for the each tool
            if (toolName.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM)) {
                Selenium selenium = new Selenium();
                selenium = createSelenium(element);
                toolContext.setSelenium(selenium);

            }


        }


    }

    /*
     * Create selenium tool object
     */
    protected Selenium createSelenium(OMElement seleniumNode) {

        Iterator seleniumProperties = seleniumNode.getChildElements();
        OMElement property;
        Selenium seleniumTool = new Selenium();

        //this map contains the list of the browsers
        HashMap<String, Browser> browserList = new HashMap<String, Browser>();
        while (seleniumProperties.hasNext()) {

            property = (OMElement) seleniumProperties.next();
            String attribute = property.getLocalName();
            String attributeValue = property.getText();

            if (attribute.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_REMOTE_DRIVE_URL)) {

                seleniumTool.setRemoteDriverURL(attributeValue);
                seleniumTool.setRemoteDriverEnable(Boolean.parseBoolean(property.getAttribute(QName.valueOf(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_REMOTE_DRIVE_URL_ENABLE)).getAttributeValue()));
            } else if (attribute.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_BROWSER)) {
                Browser browser = createSeleniumBrowser(property);
                browserList.put(browser.getBrowserType(), browser);


            }


        }
        seleniumTool.setBrowserList(browserList);
        return seleniumTool;
    }


    /*
     * create browser object for the selenium tool
     */

    public Browser createSeleniumBrowser(OMElement element) {

        Browser browser = new Browser();
        Iterator browserProperties = element.getChildElements();
        OMElement property;
        while (browserProperties.hasNext()) {

            property = (OMElement) browserProperties.next();
            String attribute = property.getLocalName();
            String attributeValue = property.getText();

            //set the browser properties
            if (attribute.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_BROWSER_TYPE)) {
                browser.setBrowserType(attributeValue);

            } else if (attribute.equals(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_BROWSER_WEB_DRIVE_PATH)) {

                browser.setWebDriverPath(attributeValue);

                Boolean webDriverPathEnable = Boolean.parseBoolean(property.getAttribute(QName.valueOf(ContextConstants.TOOL_CONTEXT_TOOL_SELENIUM_BROWSER_WEB_DRIVER_PATH_ENABLE)).getAttributeValue());
                browser.setWebDriverEnabled(webDriverPathEnable);

            }


        }
        return browser;

    }


}
