/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.pap;

import org.wso2.carbon.identity.entitlement.dto.AttributeTreeNodeDTO;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * When creating XACML policies from WSO2 Identity server's policy editor, We can define set of
 * pre-defined attribute values, attribute ids, categories, function and so on.
 * These data can be retrieved from external sources such as
 * databases,  LDAPs,  or file systems. This interface provides the flexibility for this.
 */
public interface PolicyEditorDataFinderModule {

    /**
     * Initializes data retriever module
     *
	 * @param properties properties, that need to initialize the module. These properties can be
     * defined in entitlement.properties file
     * @throws Exception throws when initialization is failed
     */
    public void init(Properties properties) throws Exception;

    /**
     * Gets name of this module
     *
     * @return name as String
     */
    public String getModuleName();

    /**
     * Gets all supported category types
     *
     * @return Map of supported categories with category display name  <--> category Uri
     */
    public Map<String, String> getSupportedCategories();

    /**
     * Finds attribute values for given category type
     *
     * @param category category of the attribute
     * @return Set of attribute values that has been encapsulated in to <code>AttributeTreeNodeDTO</code>
     * @throws Exception throws, if fails
     */
    public AttributeTreeNodeDTO getAttributeValueData(String category) throws Exception;

    /**
     * Gets supported attribute ids from this module for given category type
     *
     * @param category category of the attribute
     * @return support attribute ids as String Map of display name and attribute id uri
     * @throws Exception throws, if fails
     */
    public Map<String, String> getSupportedAttributeIds(String category) throws Exception;

    /**
     * Gets data types of the attribute values for given category type
     *
     * @param category category of the attribute
     * @return support data types as String Map of display name and data type uri
     * @throws Exception throws, if fails
     */
    public Set<String> getAttributeDataTypes(String category) throws Exception;

    /**
     * Defines whether node <code>AttributeTreeNodeDTO</code> is defined by child node name
     * or by full path name with parent node names
     * 
     * @return true or false
     */
    public boolean isFullPathSupported();

    /**
     * Defines whether tree nodes of <code>AttributeValueTreeNodeDTO</code> elements are shown
     * in UI by as a tree or flat structure
     * 
     * @return  if as a tree -> true or else -> false
     */
    public boolean isHierarchicalTree();

    /**
     * Gets all supported rule functions
     *
     * please note you need to defined the rule function following pool
     *
     * @return  Map of supported functions with function display name <--> function Id uri
     */
    public Map<String, String> getSupportedRuleFunctions();

    /**
     * Gets map of attribute id and corresponding data type
     *
     * This is used to build required data type based on the attribute id.
     *
     * @return  Map of attribute id with data type attribute id  <--> data type
     */
    public Map<String, String> getAttributeIdDataTypes();

    /**
     * Gets all supported target functions
     *
     * please note you need to target function following pool
     *
     * @return  Map of supported functions with function display name  <--> function Id
     */
    public Map<String, String> getSupportedTargetFunctions();

    /**
     * Gets default attribute id for given category
     *
     * @param category  category of the attribute
     * @return Uri value of attribute id
     */
    public String getDefaultAttributeId(String category);

    /**
     * Gets default attribute data type of the given category
     *
     * @param category  category of the attribute
     * @return Uri value of data type
     */
    public String getDefaultAttributeDataType(String category);

}
