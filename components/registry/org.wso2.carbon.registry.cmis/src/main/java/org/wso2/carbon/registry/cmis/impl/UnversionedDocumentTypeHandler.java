/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.cmis.impl;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.cmis.GregDocument;
import org.wso2.carbon.registry.cmis.GregTypeManager;
import org.wso2.carbon.registry.cmis.GregUnversionedDocument;
import org.wso2.carbon.registry.cmis.PathManager;

/**
 * Type handler that provides cmis:unversioned-document.
 */
public class UnversionedDocumentTypeHandler extends DocumentTypeHandler {

    public UnversionedDocumentTypeHandler(Registry repository,
			PathManager pathManager, GregTypeManager typeManager) {
		super(repository, pathManager, typeManager);
		// TODO Auto-generated constructor stub
	}

	public static final String DOCUMENT_UNVERSIONED_TYPE_ID = "cmis:unversioned-document";

    @Override
    public String getTypeId() {
        return DOCUMENT_UNVERSIONED_TYPE_ID;
    }

    @Override
    public TypeDefinition getTypeDefinition() {

        DocumentTypeDefinitionImpl unversionedDocument = new DocumentTypeDefinitionImpl();
        unversionedDocument.initialize(super.getTypeDefinition());

        unversionedDocument.setDescription("Unversioned document");
        unversionedDocument.setDisplayName("Unversioned document");
        unversionedDocument.setLocalName("Unversioned document");
        unversionedDocument.setIsQueryable(true);
        unversionedDocument.setQueryName(DOCUMENT_UNVERSIONED_TYPE_ID);
        unversionedDocument.setId(DOCUMENT_UNVERSIONED_TYPE_ID);
        unversionedDocument.setParentTypeId(GregTypeManager.DOCUMENT_TYPE_ID);

        unversionedDocument.setIsVersionable(false);
        unversionedDocument.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);

        GregTypeManager.addBasePropertyDefinitions(unversionedDocument);
        GregTypeManager.addDocumentPropertyDefinitions(unversionedDocument);

        return unversionedDocument;
    }

    /*@Override
    public IdentifierMap getIdentifierMap() {
        //return new DefaultDocumentIdentifierMap(false);
    }*/

    /*
    @Override
    public boolean canHandle(Node node) throws RepositoryException {
        return node.isNodeType(NodeType.NT_FILE) && !node.isNodeType(NodeType.MIX_SIMPLE_VERSIONABLE);
    }
	*/
    
    @Override
    public GregDocument getGregNode(Resource node) {
        return new GregUnversionedDocument(repository, node, typeManager, pathManager);
    }
}

