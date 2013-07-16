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

package org.wso2.carbon.registry.cmis;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.impl.DocumentTypeHandler;
import org.wso2.carbon.registry.cmis.impl.FolderTypeHandler;
import org.wso2.carbon.registry.cmis.impl.GregProperty;

import java.util.*;


/**
 * Instances of this class represent a cmis:folder backed by an underlying GREG <code>Node</code>.
 */
public class GregFolder extends GregObject {
    private static final Logger log = LoggerFactory.getLogger(GregFolder.class);

    public GregFolder(Registry repository, Resource node, GregTypeManager typeManager, PathManager pathManager) {
        super(repository, node, typeManager, pathManager);
    }

    /**
     * See CMIS 1.0 section 2.2.3.1 getChildren
     * 
     * @return  Iterator of <code>GregNode</code>. Children which are created in the checked out
     *      state are left out from the iterator.???????????
     * @throws CmisRuntimeException
     * 
     * TODO
     * 		Not sure about the spec
     * 		Do I have to give every resource (incl. Collections) except the checked out resources???
     */
    public Iterator<GregObject> getNodes() {
        try {
            String[] children = getNode().getChildren();
        	List<String> list = new ArrayList<String>();
        	
        	for(String child:children){
                Resource resource = null;
                try{
        		    resource = getRepository().get(child);
                } catch (RegistryException e){
                    log.debug(e.getMessage());
                    throw new CmisObjectNotFoundException(e.getMessage(), e);
                }
        		if (hasProperty(resource, GregProperty.GREG_IS_CHECKED_OUT)){
        			if(resource.getProperty(GregProperty.GREG_IS_CHECKED_OUT).equals("true")){
        	            String property = resource.getProperty(GregProperty.GREG_CREATED_AS_PWC);
                        if(property != null && property.equals("true")){
                            list.add(child);
                        }
                    } else{
        				list.add(child);
        			}
                } else {
        			list.add(child); //if property doesn't exist, still add it to the list
        		}
        	}

            //Sort by name
            Collections.sort(list);
            //Collections.reverse(list);

        	final Iterator<String> newListIterator = list.iterator();
        	
            Iterator<GregObject> gregObjects = new Iterator<GregObject>() {
                public boolean hasNext() {
                    return newListIterator.hasNext();
                }

                public GregObject next() {
                    try {
						return create(getRepository().get(newListIterator.next()));
					} catch (RegistryException e) {
						log.debug(e.getMessage(), e);
			            throw new CmisRuntimeException(e.getMessage(), e);
					}
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };

            return gregObjects;

            /* 
             * Lots of GREG code was here. Check with GregFolder.java in the future
             * 
             */

        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.2 createDocumentFromSource
     *
     * @throws CmisStorageException
     */
    public GregObject addNodeFromSource(GregDocument source, Properties properties) {
        try {
        	String filename = source.getNodeName();
            String destPath = getRepository().copy(source.getNode().getPath(), getNode().getPath() + "/" + filename);
            GregObject gregObject = create(getRepository().get(destPath));

            // overlay new properties
            if (properties != null && properties.getProperties() != null) {
                updateProperties(gregObject.getNode(), gregObject.getTypeId(), properties);
            }

            //session.save();
            return gregObject;
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }
    
    
    /**
     * See CMIS 1.0 section 2.2.4.14 deleteObject
     *
     * @throws CmisRuntimeException
     */
    @Override
    public void delete(boolean allVersions, boolean isPwc) {
        try {
            if (getNode().getChildCount()>0) {
                throw new CmisConstraintException("Folder is not empty!");
            } else {
                super.delete(allVersions, isPwc);
            }
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.15 deleteTree
     * 
     * In Greg, if we delete a collection it gets deleted. No worries.
     * Nothing will fail to delete.
     * TODO
     * 	Check whether checkedOut resources are deleted or not 
     */
    public FailedToDeleteDataImpl deleteTree() {
        FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
        result.setIds(Collections.<String>emptyList());
        String id = getId();
        try {
            String path = getNode().getPath();
            getRepository().delete(path);
        } catch (RegistryException e) {
        	log.debug(e.getMessage(), e); 
        }

        return result;
    }


    @Override
    protected void compileProperties(PropertiesImpl properties, Set<String> filter, ObjectInfoImpl objectInfo)
            throws RegistryException {

        super.compileProperties(properties, filter, objectInfo);

        objectInfo.setHasContent(false);
        objectInfo.setSupportsDescendants(true);
        objectInfo.setSupportsFolderTree(true);

        String typeId = getTypeIdInternal();

        addPropertyString(properties, typeId, filter, PropertyIds.PATH, getNode().getPath());

        // folder properties
        /*
         * TODO
         * 
         * */

        //The PARENT_ID is "not set" since all my types are base types. See spec 2.1.3.2.1

        //if (pathManager.isRoot(getNode())) {
        //    objectInfo.setHasParent(false);
        //}
        //else {
        //    objectInfo.setHasParent(true);
        //}
        //ParentId must be set for all folder objects except for root folder
        if(pathManager.isRoot(getNode())){
            addPropertyId(properties, typeId, filter, PropertyIds.PARENT_ID, GregProperty.GREG_PROPERTY_NOT_SET);
        } else{
            addPropertyId(properties, typeId, filter, PropertyIds.PARENT_ID, getNode().getPath());
        }

        //Allowable child object type ids
        List<String> allowableChildObjectTypeIds = new ArrayList<String>();
        allowableChildObjectTypeIds.add(GregTypeManager.FOLDER_TYPE_ID);
        allowableChildObjectTypeIds.add(GregTypeManager.DOCUMENT_TYPE_ID);
        addPropertyId(properties, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, allowableChildObjectTypeIds);

    }

    @Override
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        Set<Action> result = super.compileAllowableActions(aas);
        setAction(result, Action.CAN_GET_DESCENDANTS, true);
        setAction(result, Action.CAN_GET_CHILDREN, true);
        setAction(result, Action.CAN_GET_FOLDER_PARENT, !pathManager.isRoot(getNode()));
        setAction(result, Action.CAN_GET_OBJECT_PARENTS, !pathManager.isRoot(getNode()));
        setAction(result, Action.CAN_GET_FOLDER_TREE, true);
        setAction(result, Action.CAN_CREATE_DOCUMENT, true);
        setAction(result, Action.CAN_CREATE_FOLDER, true);

        if(getNode().getPath().equals("/")) {
            setAction(result, Action.CAN_DELETE_TREE, false);
        } else {
            setAction(result, Action.CAN_DELETE_TREE, true);
        }
        return result;
    }

    @Override
    protected Collection getContextNode() {
        return getNode();
    }

    @Override
    protected String getObjectId() throws RegistryException {
        return isRoot() ? PathManager.CMIS_ROOT_ID : super.getObjectId();
    }

    @Override
    protected BaseTypeId getBaseTypeId() {
        return BaseTypeId.CMIS_FOLDER;
    }

    @Override
    protected String getTypeIdInternal() {
        return GregTypeManager.FOLDER_TYPE_ID;
    }
    
    @Override
	public Collection getNode(){

    	return (Collection)(super.getNode());
    }

    public static void setProperties(Registry repository, Resource node, TypeDefinition type, Properties properties) {
        if (properties == null || properties.getProperties() == null) {
            throw new CmisConstraintException("No properties!");
        }

        Set<String> addedProps = new HashSet<String>();

        try {
            // check if all required properties are there
            for (PropertyData<?> prop : properties.getProperties().values()) {
                PropertyDefinition<?> propDef = type.getPropertyDefinitions().get(prop.getId());

                // do we know that property?
                if (propDef == null) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
                }

                // skip type id
                if (propDef.getId().equals(PropertyIds.OBJECT_TYPE_ID)) {
                    log.warn("Cannot set " + PropertyIds.OBJECT_TYPE_ID + ". Ignoring");
                    addedProps.add(prop.getId());
                    continue;
                }

                // skip content stream file name
                if (propDef.getId().equals(PropertyIds.CONTENT_STREAM_FILE_NAME)) {
                    log.warn("Cannot set " + PropertyIds.CONTENT_STREAM_FILE_NAME + ". Ignoring");
                    addedProps.add(prop.getId());
                    continue;
                }

                // can it be set?
                if (propDef.getUpdatability() == Updatability.READONLY) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
                }

                // empty properties are invalid
                if (PropertyHelper.isPropertyEmpty(prop)) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' must not be empty!");
                }

                // add it
                GregStaticMethods.setProperty(repository, node, prop);
                addedProps.add(prop.getId());
            }

            // check if required properties are missing and try to add default values if defined
            for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
                if (!addedProps.contains(propDef.getId()) && propDef.getUpdatability() != Updatability.READONLY) {
                    PropertyData<?> prop = PropertyHelper.getDefaultValue(propDef);
                    if (prop == null && propDef.isRequired()) {
                        throw new CmisConstraintException("Property '" + propDef.getId() + "' is required!");
                    } else if (prop != null) {
                        GregStaticMethods.setProperty(repository, node, prop);
                    }
                }
            }
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

	@Override
	protected GregObject create(Resource resource) {
		if  (resource instanceof CollectionImpl){
            FolderTypeHandler handler = new FolderTypeHandler(getRepository(), pathManager, typeManager);
		    return handler.getGregNode(resource);
        } else{
            DocumentTypeHandler documentTypeHandler = new DocumentTypeHandler(getRepository(),pathManager,typeManager);
            try {
                return documentTypeHandler.getGregNode(resource);
            } catch (RegistryException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
