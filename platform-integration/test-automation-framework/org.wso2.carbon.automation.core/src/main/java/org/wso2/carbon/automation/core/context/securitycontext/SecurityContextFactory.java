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

package org.wso2.carbon.automation.core.context.securitycontext;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.ContextConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class provied the SecurityContext object related to the autoconfig.xml security node
 */
public class SecurityContextFactory {
    XMLStreamReader xmlStream = null;

    public HashMap<String, Object> getStoreList(OMElement omElement) {

        HashMap<String, Object> map = new HashMap<String, Object>();
        Iterator children = omElement.getChildElements();
        OMNode node;
        while (children.hasNext()) {

            node = (OMNode) children.next();
            String storeName = ((OMElementImpl) node).getLocalName();

            //the store can be a key store and trust store
            //we have to explicitly add these two objects
            if (storeName.equals(ContextConstants.SECURITY_STORES_KETSTORE)) {

                String keyStoreName = ((OMElementImpl) node).getAttribute(QName.valueOf(ContextConstants.SECURITY_KEYSTORE_NAME)).getAttributeValue();
                map.put(keyStoreName, getKeyStore(node));

            } else if (storeName.equals(ContextConstants.SECURITY_STORES_TRUSTSTORE)) {
                String trustStoreName = ((OMElementImpl) node).getAttribute(QName.valueOf(ContextConstants.SECURITY_TRUSTSTORE_NAME)).getAttributeValue();
                map.put(trustStoreName, getTrueStore(node));

            }

        }

        return map;


    }

    /**
     * this method return the securityContext object providing the appropriate OMElement
     */
    public SecurityContext getSecurityContext(OMElement omElement) {

        SecurityContext securityContext = new SecurityContext();
        securityContext.setStoreList(getStoreList(omElement));
        return securityContext;


    }

    /**
     * this method provide the keystore object with its internal structure
     */
    public Keystore getKeyStore(OMNode node) {

        Keystore keystore = new Keystore();
        Iterator keyStoreAttributesIterator = ((OMElementImpl) node).getChildElements();
        while (keyStoreAttributesIterator.hasNext()) {

            OMNode keystoreNode = (OMNode) keyStoreAttributesIterator.next();
            String attribute = ((OMElementImpl) keystoreNode).getLocalName();
            String attributeValue = ((OMElementImpl) keystoreNode).getText();
            if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_FILENAME))
                keystore.setFileName(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_TYPE))
                keystore.setType(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_PASSWORD))
                keystore.setPassword(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_KEYALIAS))
                keystore.setKeyAlias(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_KEYPASSWORD))
                keystore.setKeyPassword(attributeValue);


        }
        return keystore;
    }

    /**
     * this method return the truststore object with its internal structure
     */

    public Truststore getTrueStore(OMNode node) {

        Truststore truststore = new Truststore();
        Iterator trustStoreAttributesIterator = ((OMElementImpl) node).getChildElements();
        while (trustStoreAttributesIterator.hasNext()) {

            OMNode trueStoreNode = (OMNode) trustStoreAttributesIterator.next();
            String attribute = ((OMElementImpl) trueStoreNode).getLocalName();
            String attributeValue = ((OMElementImpl) trueStoreNode).getText();
            if (attribute.equals(ContextConstants.SECURITY_TRUSTSTORE_FILENAME))
                truststore.setFileName(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_TRUSTSTORE_TYPE))
                truststore.setType(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_TRUSTSTORE_PASSWORD))
                truststore.setPassword(attributeValue);


        }
        return truststore;

    }


}
