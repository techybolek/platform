/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryCoreServiceComponent;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.extensions.utils.CommonConstants;
import org.wso2.carbon.registry.extensions.utils.CommonUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Iterator;

public class WadlMediaTypeHandler extends Handler {

    private static final Log log = LogFactory.getLog(WadlMediaTypeHandler.class);
    private Registry registry;
    private Registry governanceUserRegistry;

    public void put(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return;
        }
        CommonUtil.acquireUpdateLock();

        Resource resource = requestContext.getResource();
        String resourcePath = requestContext.getResourcePath().getPath();
        registry = requestContext.getRegistry();
        governanceUserRegistry = RegistryCoreServiceComponent.getRegistryService().
                getGovernanceUserRegistry(CurrentSession.getUser(),
                        CurrentSession.getTenantId());
        try {
            OMElement wadlElement;
            String wadlContent;
            Object resourceContent = resource.getContent();
            if (resourceContent instanceof String) {
                wadlContent = (String) resourceContent;
            } else {
                wadlContent = new String((byte[]) resourceContent);
            }

            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().
                        createXMLStreamReader(new StringReader(wadlContent));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                wadlElement = builder.getDocumentElement();
            } catch (XMLStreamException e) {
                String msg = "Error in reading the WADL content of the Process. " +
                        "The requested path to store the Process: " + resourcePath + ".";
                log.error(msg);
                throw new RegistryException(msg, e);
            }

            registry.put(resourcePath, resource);
            CommonUtil.releaseUpdateLock();

            String wadlNamespace = wadlElement.getNamespace().getNamespaceURI();
            String wadlName = RegistryUtils.getResourceName(requestContext.getResourcePath().getPath());

            OMElement grammarsElement = wadlElement.
                    getFirstChildWithName(new QName(wadlNamespace, "grammars"));
            if(grammarsElement != null){
                Iterator<OMElement> grammarElements = grammarsElement.
                        getChildrenWithName(new QName(wadlNamespace, "include"));
                while (grammarElements.hasNext()){
                    String importUrl = grammarElements.next().getAttributeValue(new QName("href"));
                    if(importUrl.endsWith(".xsd")) {
                        String schemaPath = saveSchema(importUrl);
                        addDependency(resource.getPath(),
                                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + schemaPath);
                    }
                }
            }

            String servicePath = saveService(new QName(wadlNamespace, getServiceName(wadlName)));
            addDependency(RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + servicePath, resource.getPath());

            requestContext.setProcessingComplete(true);
        } catch (Exception e) {
            String msg = "Error while parsing the WADL content of " +
                    RegistryUtils.getResourceName(resourcePath);
            log.error(msg);
            throw new RegistryException(msg, e);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private String saveService(QName qName) throws RegistryException {
        try {
            ServiceManager serviceManager = new ServiceManager(governanceUserRegistry);
            Service service = serviceManager.newService(qName);
            serviceManager.addService(service);
            return service.getPath();
        } catch (RegistryException e) {
            String msg = "Adding service for WADL failed :" + qName.getLocalPart();
            log.error(msg);
            throw new RegistryException(msg, e);
        }
    }

    private String saveSchema(String schemaUrl) throws RegistryException {
        try {
            SchemaManager schemaManager = new SchemaManager(governanceUserRegistry);
            Schema schema = schemaManager.newSchema(schemaUrl);
            schemaManager.addSchema(schema);
            return schema.getPath();
        } catch (RegistryException e) {
            String msg = "Schema import failed :" + schemaUrl;
            log.error(msg);
            throw new RegistryException(msg, e);
        }
    }

    private void addDependency(String source, String target) throws RegistryException {
        registry.addAssociation(source, target, CommonConstants.DEPENDS);
        registry.addAssociation(target, source, CommonConstants.USED_BY);
    }

    private String getServiceName(String url){
        String wadlName = url.replaceAll(".*/(.*)/l$","$1");
        return wadlName.substring(0, wadlName.lastIndexOf("."));
    }
}
