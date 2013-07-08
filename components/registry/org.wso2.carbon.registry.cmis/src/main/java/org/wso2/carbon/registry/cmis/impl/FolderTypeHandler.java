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

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.GregFolder;
import org.wso2.carbon.registry.cmis.GregTypeManager;
import org.wso2.carbon.registry.cmis.PathManager;

/**
 * Type handler that provides cmis:folder.
 */
public class FolderTypeHandler extends AbstractGregTypeHandler {

    public FolderTypeHandler(Registry repository, PathManager pathManager,
			GregTypeManager typeManager) {
		super(repository, pathManager, typeManager);
		// TODO Auto-generated constructor stub
	}

	private static final Logger log = LoggerFactory.getLogger(FolderTypeHandler.class);

    /*private static class FolderIdentifierMap extends DefaultIdentifierMapBase {

        public FolderIdentifierMap() {
            super("nt:folder");
            // xxx not supported: PARENT_ID, ALLOWED_CHILD_OBJECT_TYPE_IDS, PATH
        }
    }*/

    public String getTypeId() {
        return BaseTypeId.CMIS_FOLDER.value();
    }

    public TypeDefinition getTypeDefinition() {
        FolderTypeDefinitionImpl folderType = new FolderTypeDefinitionImpl();
        folderType.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
        folderType.setIsControllableAcl(false);
        folderType.setIsControllablePolicy(false);
        folderType.setIsCreatable(true);
        folderType.setDescription("Folder");
        folderType.setDisplayName("Folder");
        folderType.setIsFileable(true);
        folderType.setIsFulltextIndexed(false);
        folderType.setIsIncludedInSupertypeQuery(true);
        folderType.setLocalName("Folder");
        folderType.setLocalNamespace(GregTypeManager.NAMESPACE);
        folderType.setIsQueryable(true);
        folderType.setQueryName(GregTypeManager.FOLDER_TYPE_ID);
        folderType.setId(GregTypeManager.FOLDER_TYPE_ID);


        GregTypeManager.addBasePropertyDefinitions(folderType);
        GregTypeManager.addFolderPropertyDefinitions(folderType);

        return folderType;
    }
    /*
    public IdentifierMap getIdentifierMap() {
        return new FolderIdentifierMap();
    }*/

    public GregFolder getGregNode(Resource node) {
        return new GregFolder(repository, node, typeManager, pathManager);
    }
    
    /*
    public boolean canHandle(Node node) throws RepositoryException {
        return node.isNodeType(NodeType.NT_FOLDER) || node.getDepth() == 0;
    }
	*/
    
    public GregFolder createFolder(GregFolder parentFolder, String name, Properties properties) {
        try {
        	
        	Collection node = repository.newCollection();
            String destinationPath = getNodeDestPath(parentFolder,name);
        	repository.put(destinationPath, node);
        	Resource resource = repository.get(destinationPath);
        	// compile the properties
            GregFolder.setProperties(repository, resource, getTypeDefinition(), properties);

            return getGregNode(resource);
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

	private String getNodeDestPath(GregFolder parentFolder, String name) {
		
		String parentPath = parentFolder.getNode().getPath();
		if(parentPath.endsWith("/")){
			return parentPath+name;
		}
		else{
			return parentPath+"/"+name;
		}
	}
}
