/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class InMemoryPersistenceManager implements DataPersistenceManager{
    
    private String xmlConfig = "<policyEditor>\n" +
            "    <categories>\n" +
            "        <category>\n" +
            "            <name>Subject</name>\n" +
            "            <uri>http://subject</uri>\n" +
            "            <supportedAttributeIds>\n" +
            "                <attributeId>test1</attributeId>\n" +
            "                <attributeId>test2</attributeId>\n" +
            "            </supportedAttributeIds>\n" +
            "            <supportedDataTypes>\n" +
            "                <dataType>data1</dataType>\n" +
            "                <dataType>data2</dataType>\n" +
            "            </supportedDataTypes>\n" +
            "        </category>\n" +
            "        <category>\n" +
            "            <name>Resource</name>\n" +
            "            <uri>http://resource</uri>\n" +
            "            <supportedAttributeIds>\n" +
            "                <attributeId>test1</attributeId>\n" +
            "                <attributeId>test2</attributeId>\n" +
            "            </supportedAttributeIds>\n" +
            "            <supportedDataTypes>\n" +
            "                <dataType>data1</dataType>\n" +
            "                <dataType>data2</dataType>\n" +
            "            </supportedDataTypes>\n" +
            "        </category>\n" +
            "        <category>\n" +
            "            <name>Action</name>\n" +
            "            <uri>http://action</uri>\n" +
            "            <supportedAttributeIds>\n" +
            "                <attributeId>test1</attributeId>\n" +
            "                <attributeId>test2</attributeId>\n" +
            "            </supportedAttributeIds>\n" +
            "            <supportedDataTypes>\n" +
            "                <dataType>data1</dataType>\n" +
            "                <dataType>data2</dataType>\n" +
            "            </supportedDataTypes>\n" +
            "        </category>\n" +
            "    </categories>\n" +
            "</policyEditor>";

    private static Log log = LogFactory.getLog(InMemoryPersistenceManager.class);
    
    @Override
    public PolicyEditorDataHolder buildDataHolder() {

        PolicyEditorDataHolder holder = new PolicyEditorDataHolder();
        ByteArrayInputStream inputStream;
        Element root = null;

        inputStream = new ByteArrayInputStream(this.xmlConfig.getBytes());
        DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
        try {
            Document doc = builder.newDocumentBuilder().parse(inputStream);
            root = doc.getDocumentElement();
        } catch (Exception e) {
            log.error("DOM of request element can not be created from String", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Error in closing input stream of XACML request");
            }
        }
        
        if(root == null){
            return holder;
        }

        NodeList nodeList = root.getChildNodes();
        
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if(node.getNodeName().equals("categories")){
                parseChildElement(node, holder);
            }
        }
        
        return holder;
    }

    @Override
    public void persistData(String xmlConfig) {
        this.xmlConfig = xmlConfig;
    }
    
    private void parseChildElement(Node root, PolicyEditorDataHolder holder){

        NodeList nodeList = root.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if("category".equals(node.getNodeName())){

                String name = null;
                String uri = null;
                Set<String> attributeIds = null;
                Set<String> dataTypes = null;

                NodeList childList = node.getChildNodes();
                for(int j = 0; j < childList.getLength(); j++){
                    Node child = childList.item(j);
                    if("name".equals(child.getNodeName())){
                        name = child.getTextContent();
                    } else if("uri".equals(child.getNodeName())){
                        uri = child.getTextContent();
                    } else if("supportedAttributeIds".equals(child.getNodeName())){
                        attributeIds = new HashSet<String>();
                        NodeList list = child.getChildNodes();                        
                        for(int k = 0; k < list.getLength(); k++){
                            Node nextChild = list.item(k);
                            if("attributeId".equals(nextChild.getNodeName())){
                                attributeIds.add(nextChild.getTextContent());
                            }
                        }
                    } else if("supportedDataTypes".equals(child.getNodeName())){
                        dataTypes = new HashSet<String>();
                        NodeList list = child.getChildNodes();
                        for(int k = 0; k < list.getLength(); k++){
                            Node nextChild = list.item(k);
                            if("dataType".equals(nextChild.getNodeName())){
                                dataTypes.add(nextChild.getTextContent());
                            }
                        }
                    }
                }
                if(name != null){
                    if(uri != null){
                        holder.getCategoryMap().put(name, uri);
                    }
                    if(attributeIds != null){
                        holder.getCategoryAttributeIdMap().put(name, attributeIds);
                    }
                    if(dataTypes != null){
                        holder.getCategoryDataTypeMap().put(name, dataTypes);
                    }
                }
            }
        }               
    }
}
