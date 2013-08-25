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
    private HashMap<String, KeyStore> keyStoreMap = new HashMap<String, KeyStore>();
    private HashMap<String, TrustStore> trustStoreMap = new HashMap<String, TrustStore>();
    SecurityContext securityContext;

    public void SecurityContextFactory()
    {
        securityContext = new SecurityContext();
    }

    public void getStoreList(OMElement omElement) {


        Iterator children = omElement.getChildElements();
        OMNode node;
        while (children.hasNext()) {

            node = (OMNode) children.next();
            String storeName = ((OMElementImpl) node).getLocalName();

            //the store can be a key store and trust store
            //we have to explicitly add these two objects
            if (storeName.equals(ContextConstants.SECURITY_STORES_KETSTORE)) {

                String keyStoreName = ((OMElementImpl) node).getAttribute(QName.valueOf(ContextConstants
                        .SECURITY_KEYSTORE_NAME)).getAttributeValue();
                // add the key store to the key store list
                keyStoreMap.put(keyStoreName, getKeyStore(node));

            } else if (storeName.equals(ContextConstants.SECURITY_STORES_TRUSTSTORE)) {
                String trustStoreName = ((OMElementImpl) node).getAttribute(QName.valueOf(ContextConstants
                        .SECURITY_TRUSTSTORE_NAME)).getAttributeValue();

                //add trust store to the trust store list
                trustStoreMap.put(trustStoreName, getTrueStore(node));

            }

        }


    }

    /**
     * this method return the securityContext object providing the appropriate OMElement
     */
    public SecurityContext getSecurityContext(OMElement omElement) {

        SecurityContext securityContext = new SecurityContext();
        securityContext.setKeyStoreList(keyStoreMap);
        securityContext.setTrustStoreList(trustStoreMap);

        return securityContext;


    }

    /**
     * this method provide the keyStore object with its internal structure
     */
    public KeyStore getKeyStore(OMNode node) {

        KeyStore keyStore = new KeyStore();
        Iterator keyStoreAttributesIterator = ((OMElementImpl) node).getChildElements();
        while (keyStoreAttributesIterator.hasNext()) {

            OMNode keyStoreNode = (OMNode) keyStoreAttributesIterator.next();
            String attribute = ((OMElementImpl) keyStoreNode).getLocalName();
            String attributeValue = ((OMElementImpl) keyStoreNode).getText();
            if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_NAME)) {
                keyStore.setName(attributeValue);
            } else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_FILENAME))
                keyStore.setFileName(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_TYPE))
                keyStore.setType(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_PASSWORD))
                keyStore.setPassword(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_KEYALIAS))
                keyStore.setKeyAlias(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_KEYSTORE_KEYPASSWORD))
                keyStore.setKeyPassword(attributeValue);


        }
        return keyStore;
    }

    /**
     * this method return the trustStore object with its internal structure
     */

    public TrustStore getTrueStore(OMNode node) {

        TrustStore trustStore = new TrustStore();
        Iterator trustStoreAttributesIterator = ((OMElementImpl) node).getChildElements();
        while (trustStoreAttributesIterator.hasNext()) {

            OMNode trueStoreNode = (OMNode) trustStoreAttributesIterator.next();
            String attribute = ((OMElementImpl) trueStoreNode).getLocalName();
            String attributeValue = ((OMElementImpl) trueStoreNode).getText();
            if (attribute.equals(ContextConstants.SECURITY_TRUSTSTORE_NAME)) {
                trustStore.setName(attributeValue);
            } else if (attribute.equals(ContextConstants.SECURITY_TRUSTSTORE_FILENAME))
                trustStore.setFileName(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_TRUSTSTORE_TYPE))
                trustStore.setType(attributeValue);
            else if (attribute.equals(ContextConstants.SECURITY_TRUSTSTORE_PASSWORD))
                trustStore.setPassword(attributeValue);


        }
        return trustStore;

    }


    public void createSecurityContext(OMElement nodeElement) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public SecurityContext getSecurityContext() {
       return securityContext;
    }
}
