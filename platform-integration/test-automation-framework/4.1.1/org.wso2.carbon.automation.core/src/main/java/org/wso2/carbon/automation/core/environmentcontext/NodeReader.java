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

package org.wso2.carbon.automation.core.environmentcontext;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.environmentcontext.environmentenum.ProductProperties;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.Context;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.InstanceProperties;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.InstanceVariables;
import org.wso2.carbon.automation.core.environmentcontext.environmentvariables.NodeProperties;
import org.wso2.carbon.automation.core.ProductConstant;
import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/*
* Reader module for the XML files which containing the node details
* */
public class NodeReader {
    private static final Log log = LogFactory.getLog(NodeReader.class);

    private static final String NODE_ID = "id";
    private static final String NODE_GROUP = "name";
    private static final String NODE_ENVIRONMENT = "environment";
    XMLStreamReader xmlStream = null;

    public NodeReader() {
        DataHandler handler;
        try {
            String nodeFile = ProductConstant.NODE_FILE_NAME;

            URL clusterXmlURL = new File(String.format("%s%s", ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION, nodeFile)).toURL();
            handler = new DataHandler(clusterXmlURL);
            xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(handler.getInputStream());
        } catch (XMLStreamException e) {
            log.error(String.format("Cannot create Stream :-%s", e.getMessage()));
        } catch (IOException e) {
            log.error(String.format("File Input Error :-%s", e.getMessage()));
        }
    }

    /**
     * List all available platform nodes under a given platform
     * <p/>
     * *
     */
    public HashMap<String, Context> getNodeContext() {

        HashMap<String, Context> nodeMap = new HashMap<String, Context>();

        for (OMNode platform : listPlatforms()) {
            for (OMNode group : listGroups(platform)) {
                for (OMNode node : listNodes(group)) {
                    for (Context instance : getInstance(node).values()) {
                        nodeMap.put(instance.getNodeId(), instance);
                    }
                }
            }
        }
      return nodeMap;
    }

    /*
     * Lists all Platforms in the node.XML file
     *
     *  */
    public List<OMNode> listPlatforms() {
        StAXOMBuilder builder = new StAXOMBuilder(xmlStream);
        OMElement endPointElem = builder.getDocumentElement();
        List<OMNode> platformList = new ArrayList<OMNode>();

        OMNode node;
        Iterator children = endPointElem.getChildElements();
        while (children.hasNext()) {
            node = (OMNode) children.next();
            platformList.add(node);
        }
        return platformList;
    }

    /**
     * List all available group nodes under a given platform
     * <p/>
     * *
     */
    public List<OMNode> listGroups(OMNode node) {
        List<OMNode> groupList = new ArrayList<OMNode>();
        Iterator environmentNodeItr = ((OMElementImpl) node).getChildElements();
        while (environmentNodeItr.hasNext()) {
            groupList.add((OMNode) environmentNodeItr.next());
        }
        return groupList;
    }

    /**
     * List all available sub nodes nodes under a given platform
     * <p/>
     * *
     */
    public List<OMNode> listNodes(OMNode node) {
        List<OMNode> nodeList = new ArrayList<OMNode>();
        Iterator environmentNodeItr = ((OMElementImpl) node).getChildElements();
        while (environmentNodeItr.hasNext()) {
            nodeList.add((OMNode) environmentNodeItr.next());
        }
        return nodeList;
    }

    public HashMap<String, Context> getInstance(OMNode node) {
        String host = null;
        String httpPort = null;
        String httpsPort = null;
        String nhttpPort = null;
        String webcontextRoot = null;
        String nhttpsPort = null;
        String qpidPort = null;
        InstanceVariables nodeVariables;
        Iterator instanceIterator = ((OMElementImpl) node).getChildElements();
        String type = ((OMElementImpl) node).getLocalName();
        String nodeGroup = ((OMElementImpl) node.getParent()).getAttribute(new QName(NODE_GROUP)).getAttributeValue();
        String nodeEnvironment = ((OMElementImpl) ((OMElementImpl) node.getParent()).getParent()).getAttribute(new QName(NODE_ENVIRONMENT)).getAttributeValue();
        Context contextMap;
        HashMap<String, Context> nodeMap = new HashMap<String, Context>();
        while (instanceIterator.hasNext()) {
            NodeProperties platform = new NodeProperties();
            InstanceProperties instanceProperties = new InstanceProperties();
            OMNode node2 = (OMNode) instanceIterator.next();
            String nodeId = ((OMElementImpl) node2).getAttribute(new QName(NODE_ID)).getAttributeValue();
            String nodeContent = ((OMElementImpl) node2).getLocalName();
            Iterator param = ((OMElementImpl) node2).getChildElements();
            while (param.hasNext()) {
                OMNode node3 = (OMNode) param.next();
                String attrib = ((OMElementImpl) node3).getLocalName();
                if (attrib.equals(ProductProperties.host.name())) {
                    host = ((OMElementImpl) node3).getText();
                } else if (attrib.equals(ProductProperties.httpport.name())) {
                    httpPort = ((OMElementImpl) node3).getText();
                } else if (attrib.equals(ProductProperties.httpsport.name())) {
                    httpsPort = ((OMElementImpl) node3).getText();
                } else if (attrib.equals(ProductProperties.nhttpport.name())) {
                    nhttpPort = ((OMElementImpl) node3).getText();
                } else if (attrib.equals(ProductProperties.nhttpsport.name())) {
                    nhttpsPort = ((OMElementImpl) node3).getText();
                } else if (attrib.equals(ProductProperties.qpidport.name())) {
                    qpidPort = ((OMElementImpl) node3).getText();
                }
            }
            platform.setPlatform(nodeGroup, nodeEnvironment);
            instanceProperties.setNodeId(nodeId);
            instanceProperties.setGroupId(nodeGroup);
            instanceProperties.setNodeContent(nodeContent);
            instanceProperties.setNodeType(type);
            instanceProperties.setPlatform(platform);
            nodeVariables = setProductVariables(host, httpPort, httpsPort,
                    webcontextRoot, nhttpPort, nhttpsPort, qpidPort);
            contextMap = new Context();
            contextMap.setInstanceProperties(instanceProperties);
            contextMap.setInstanceVariables(nodeVariables);
            contextMap.setNodeId(nodeId);
            nodeMap.put(nodeId, contextMap);
        }
        return nodeMap;
    }

    private InstanceVariables setProductVariables(String host, String httpPort, String httpsPort,
                                                  String webContextRoot, String nhttpPort,
                                                  String nhttpsPort, String qpidPort) {
        InstanceVariables productVariable = new InstanceVariables();
        UrlGenerator urlGeneratorUtil = new UrlGenerator();
        String backendUrl = urlGeneratorUtil.getBackendUrl(httpsPort, host, webContextRoot);
        productVariable.setBackendUrl(backendUrl);
        productVariable.setHostName(host);
        productVariable.setHttpPort(httpPort);
        productVariable.setHttpsPort(httpsPort);
        productVariable.setNhttpPort(nhttpPort);
        productVariable.setNhttpsPort(nhttpsPort);
        productVariable.setQpidPort(qpidPort);
        productVariable.setWebContextRoot(webContextRoot);
        return productVariable;
    }
}
