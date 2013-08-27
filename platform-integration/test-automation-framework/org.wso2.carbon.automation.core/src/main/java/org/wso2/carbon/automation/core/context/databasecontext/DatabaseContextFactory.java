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

package org.wso2.carbon.automation.core.context.databasecontext;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.ContextConstants;

import javax.xml.namespace.QName;
import java.util.*;

/*
 * this class return the database context object
 */
public class DatabaseContextFactory {

    DatabaseContext databaseContext;

    public void DatabaseContextFactory() {
        databaseContext = new DatabaseContext();
    }

    /*
     * this method returns the list of databases ojects in the provided OMElement
     */
    public void createDatabaseContext(OMElement dataBaseElement) {


        HashMap<String, Database> databaseMap = new HashMap<String, Database>();
        OMNode node;
        Iterator children = dataBaseElement.getChildElements();
        while (children.hasNext()) {
            Database database = new Database();
            node = (OMNode) children.next();
            String databaseName = (((OMElementImpl) node)
                    .getAttribute(QName.valueOf(ContextConstants
                            .DATABASE_CONTEXT_NAME))).getAttributeValue();
            database.setName(databaseName);
            Iterator configPropertiesIterator = ((OMElementImpl) node).getChildElements();
            while (configPropertiesIterator.hasNext()) {

                OMNode databaseNode = (OMNode) configPropertiesIterator.next();
                String attribute = ((OMElementImpl) databaseNode).getLocalName();
                String attributeValue = ((OMElementImpl) databaseNode).getText();
                if (attribute.equals(ContextConstants.DATABASE_CONTEXT_URL))
                    database.setUrl(attributeValue);
                else if (attribute.equals(ContextConstants.DATABASE_CONTEXT_USERNAME))
                    database.setUsername(attributeValue);
                else if (attribute.equals(ContextConstants.DATABASE_CONTEXT_PASSWORD))
                    database.setPassword(attributeValue);
                else if (attribute.equals(ContextConstants.DATABASE_CONTEXT_DRIVERCLASSNAME))
                    database.setDriverClassName(attributeValue);

            }
            databaseMap.put(databaseName, database);


        }
        databaseContext.setDatabaseConfigurations(databaseMap);
    }

    /*
     * this method return the databaseContext object providing the appropriate database node in autoconfig.xml
     */
    public DatabaseContext getDatabaseContext() {
        return databaseContext;


    }


}

