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


//import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.impl.GregProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Arrays;


/**
 * Instances of this class represent a versionable cmis:document and its versions backed by an underlying
 * GREG <code>Node</code>.
 */
public abstract class GregVersionBase extends GregDocument {
    private static final Logger log = LoggerFactory.getLogger(GregVersionBase.class);

    protected GregVersionBase(Registry repository, Resource node, GregTypeManager typeManager, PathManager pathManager) {
        super(repository, node, typeManager, pathManager);
    }

    /**
     * See CMIS 1.0 section 2.2.7.6 getAllVersions
     */
    public Iterator<GregVersion> getVersions() {
        try {
            String[] versionArray = getRepository().getVersions(getNode().getPath());
            List<String> versions;
            if(versionArray == null){
                //Get the base node
                versions = new ArrayList<String>();
                versions.add(getNode().getPath());
            } else {
                //int endIndex = versionArray.length-1;
                //skip base node
                //versionArray = (String [])Arrays.copyOfRange(versionArray, 0, endIndex-1);
        	    versions = new ArrayList<String>( Arrays.asList(versionArray) );
            }

            //Collections.reverse(versions); //sort();

            final Iterator<String> iterator = versions.iterator();
            return new Iterator<GregVersion>() {
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public GregVersion next() {
                    String nextVersion = iterator.next();
                    try {
                        return new GregVersion(getRepository(), getRepository().get(nextVersion), nextVersion, typeManager, pathManager);
                    } catch (RegistryException e) {
                        throw new CmisRuntimeException("Error iterating over versions");
                    }

                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }
    
    @Override
    public void delete(boolean allVersions, boolean isPwc) {
        Resource node = getNode();
        try {
            //2nd condition exists to check whether this is the original doc or the pwc
            //Also checks for a Doc created AS a PWC
            if (isCheckedOut(node) && !node.getPath().endsWith("_pwc")) {
                if (isPwc) {
                    cancelCheckout(getRepository(), node);
                } else {
                    throw new CmisStorageException("Cannot delete checked out document: " + node.getId());
                }
            } else if (allVersions) {
                //checkout(getRepository(), node);
                String path = node.getPath();

                //delete all versions
                String[] versions = getRepository().getVersions(path);
                if(versions!=null){
                    for(String version: versions){

                        String resourcePath = path;
                        String permanentPath = version;

                        int beginIndex = permanentPath.indexOf(":")+1;
                        int endIndex = permanentPath.length();

                        String versionNumber = permanentPath.substring(beginIndex,endIndex);
                        long snapshotId = Long.parseLong(versionNumber);


                        getRepository().removeVersionHistory(resourcePath, snapshotId);

                    }
                }
                //Delete major resource TODO- check whether this has to execute
                getRepository().delete(path);
            } else {
                //Delete the specific version
                //Permanent path = /abc/def/resourceName;version=xxxx
                String resourcePath = node.getPath();
                String permanentPath = node.getPermanentPath();

                int beginIndex = permanentPath.indexOf(":")+1;
                int endIndex = permanentPath.length();

                String versionNumber = permanentPath.substring(beginIndex,endIndex);
                long snapshotId = Long.parseLong(versionNumber);

                getRepository().removeVersionHistory(resourcePath, snapshotId);

            }
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.7.1 checkOut
     *
     * @throws CmisRuntimeException
     */
    public GregPrivateWorkingCopy checkout() {
        Resource node = getNode();
        try {
            if (isCheckedOut(node)) {
                throw new CmisConstraintException("Document is already checked out " + node.getId());
            }

            return getPwc(checkout(getRepository(), node));
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.7.3 checkedIn
     *
     * @throws CmisRuntimeException
     */
    public GregVersion checkin(Properties properties, ContentStream contentStream, String checkinComment) {
        Resource node = getNode();

        try {
            if (!isCheckedOut(node)) {
                throw new CmisStorageException("Not checked out: " + node.getId());
            }

            if (properties != null && !properties.getPropertyList().isEmpty()) {
                updateProperties(properties);
            }

            if (contentStream != null) {
                setContentStream(contentStream, true);
            }

            // todo handle checkinComment
            Resource resource = checkin();
            String pathOfLatestVersion = getRepository().getVersions(resource.getPath())[0];
            return new GregVersion(getRepository(), resource, pathOfLatestVersion, typeManager, pathManager);
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.7.2 cancelCheckout
     *
     * @throws CmisRuntimeException
     */
    public void cancelCheckout() {
        Resource node = getNode();
        try {
            //TODO If node is the original copy then the pwc has to be passed!
            cancelCheckout(getRepository(),node);
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get the private working copy of the versions series or throw an exception if not checked out.
     *
     * @return  a {@link GregPrivateWorkingCopy} instance
     * @throws CmisObjectNotFoundException  if not checked out
     * @throws CmisRuntimeException
     */
    public GregPrivateWorkingCopy getPwc(Resource node) {
        if (node.getPath().endsWith("_pwc")) {
		    return new GregPrivateWorkingCopy(getRepository(), node, typeManager, pathManager);
		} else {
		    throw new CmisObjectNotFoundException("Not checked out document has no private working copy");
		}
    }

    public GregPrivateWorkingCopy getPwc() {

        Resource node = getNode();
        if (isCheckedOut(node)) {
            return new GregPrivateWorkingCopy(getRepository(), node, typeManager, pathManager);
		} else {
		    throw new CmisObjectNotFoundException("Not checked out document has no private working copy");
		}
    }

    /**
     * Get a specific version by name
     * @param name  name of the version to get
     * @return  a {@link GregVersion} instance for <code>name</code>
     * @throws CmisObjectNotFoundException  if a version <code>name</code> does not exist
     * @throws CmisRuntimeException
     */
    public GregVersion getVersion(String name) {
        try {
            Resource node = getNode();
            String[] versions = getRepository().getVersions(node.getPath());
            if(versions==null){
                throw new CmisObjectNotFoundException("GregVersionBase.java: No versions exist!!");
            }
            String gotVersion = null;
            for (String version: versions){
            	if(version.equals(name)){
            		gotVersion = version;
            	}
            }
            if(gotVersion==null){
            	throw new CmisObjectNotFoundException("GregVersionBase.java: No version found!!");
            }
            return new GregVersion(getRepository(), node, gotVersion, typeManager, pathManager);
        }
        catch (RegistryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    //------------------------------------------< protected >---

    /**
     * @return  Id of the version representing the base of this versions series
     * @throws RegistryException
     */
    protected String getBaseNodeId() throws RegistryException {
        if( getRepository().getVersions(getNode().getPath()) != null){
            String[] versions = getRepository().getVersions(getNode().getPath());
            String baseNodePath = versions[versions.length-1];
    	    return baseNodePath;
        } else{
            String baseNodePath = getNode().getPath();
    	    return baseNodePath;
        }

    }

    /**
     * @return  Id of the private working copy of this version series
     * @throws RegistryException
     */
    protected String getPwcId() throws RegistryException {
        return null; //// WHAT the heck we return
    }
    
    @Override
    protected void compileProperties(PropertiesImpl properties, Set<String> filter, ObjectInfoImpl objectInfo)
            throws RegistryException {

        super.compileProperties(properties, filter, objectInfo);

        objectInfo.setWorkingCopyOriginalId(getBaseNodeId());
        objectInfo.setWorkingCopyId(getPwcId());
    }

    @Override
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        Set<Action> result = super.compileAllowableActions(aas);
        setAction(result, Action.CAN_GET_ALL_VERSIONS, true);
        try {
            if(isCheckedOut()){
                setAction(result, Action.CAN_CANCEL_CHECK_OUT, true);
                setAction(result, Action.CAN_CHECK_IN, true);
                setAction(result, Action.CAN_CHECK_OUT, false);
            } else{
                //setAction(result, Action.CAN_CANCEL_CHECK_OUT, false);
                //setAction(result, Action.CAN_CHECK_IN, false);
                setAction(result, Action.CAN_CHECK_OUT, true);
            }
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }
    
    @Override
    protected String getTypeIdInternal() {
        return GregTypeManager.DOCUMENT_TYPE_ID;
    }

    @Override
    protected boolean isCheckedOut() throws RegistryException {
        return isCheckedOut(getNode());
    }

    @Override
    protected String getCheckedOutId() throws RegistryException {
        if (isCheckedOut()){
            String property = getNode().getProperty(GregProperty.GREG_CREATED_AS_PWC);
            if(property != null && property.equals("true")){
                return getVersionSeriesId();
            } else{
                return getVersionSeriesId()+"_pwc";
            }
        } else{
            return null;
        }

    }

    @Override
    protected String getCheckedOutBy() throws RegistryException {
        return isCheckedOut()
                ? getNode().getProperty(GregProperty.GREG_CHECKED_OUT_BY)
                : null;
    }
    

    public static Resource checkout(Registry repository, Resource node) throws RegistryException {

        /*
        * When checked out, the file structure is as below
        *
        * Original Document: /abc/def/resourceName
        * Checked out Doc :  /abc/def/resourceName_pwc
        *
        * */

        //TODO get User Name from context object
    	node.setProperty(GregProperty.GREG_CHECKED_OUT_BY, "user");
        repository.put(node.getPath(), node);

        //Make a private working copy (/resourceName_pwc)
        String destPath = node.getPath()+"_pwc";
        repository.copy(node.getPath(), destPath);

        node.setProperty(GregProperty.GREG_IS_CHECKED_OUT, "true");
        repository.put(node.getPath(), node);

        //put the checked out doc path in checkedOut tracker
        Resource resource = null;
        if(repository.resourceExists(GregProperty.GREG_CHECKED_OUT_TRACKER)){
            resource = repository.get(GregProperty.GREG_CHECKED_OUT_TRACKER);
        } else{
            resource  = repository.newResource();
            //Have to set content, otherwise Greg will throw exception when browsing this file in Workbench
            resource.setContent("tracker");
        }

        resource.setProperty(destPath, "true");
        repository.put(GregProperty.GREG_CHECKED_OUT_TRACKER, resource);

        return repository.get(destPath);
    }

    //------------------------------------------< private >---

    private static String getResourceName(String path) {
        if(path.equals("/")){
            return "/";
        }
    	String[] parts = path.split("/");
    	if (parts ==  null) {
    		return path;
    	} else{
    		return parts[parts.length-1];
    	}
    }

    private Resource checkin() throws RegistryException {

        getNode().setProperty(GregProperty.GREG_IS_CHECKED_OUT, "false");
        if(getNode().getProperty(GregProperty.GREG_CHECKED_OUT_BY)  != null ){
    	    getNode().removeProperty(GregProperty.GREG_CHECKED_OUT_BY);
        }
        getRepository().put(getNode().getPath(), getNode());

        String nodePath = getNode().getPath();
        if(nodePath.endsWith("_pwc")){

            //Remove checkedOut doc from tracker
            Resource resource = getRepository().resourceExists(GregProperty.GREG_CHECKED_OUT_TRACKER)
                                ? getRepository().get(GregProperty.GREG_CHECKED_OUT_TRACKER)
                                : null;
            if(resource!=null){
                if(resource.getProperty(nodePath)!=null){
                    resource.removeProperty(nodePath);
                    getRepository().put(resource.getPath(), resource);
                } else{
                    throw new CmisRuntimeException("Checked out doc not in tracker!");
                }
            } else{
                throw new CmisRuntimeException("Tracker not found");
            }


            String destPath = nodePath.substring(0, nodePath.indexOf("_pwc"));
            getRepository().move(nodePath, destPath);

            //Create a version
            getRepository().createVersion(destPath);

            //get the latest version
            String pathOfLatestVersion = getRepository().getVersions(destPath)[0];
            return getRepository().get(pathOfLatestVersion);
        } else{
            //This code is run when a newly created document is checked in

            //create base version
            getRepository().createVersion(getNode().getPath());
            //String pathOfLatestVersion = getRepository().getVersions(getNode().getPath())[0];
            return getNode();
        }
    }

    private static void cancelCheckout(Registry repository, Resource node) throws RegistryException {
        /*
         *  2.2.7.2 cancelCheckOut
            Description: Reverses the effect of a check-out. Removes the private working copy of the checked-out
            document, allowing other documents in the version series to be checked out again. If the private working
            copy has been created by createDocument, cancelCheckOut MUST delete the created document.
        *
        * TODO Think on this later
        */

        //Remove from tracker
        //Remove checkedOut doc from tracker
        Resource tracker = repository.resourceExists(GregProperty.GREG_CHECKED_OUT_TRACKER)
                ? repository.get(GregProperty.GREG_CHECKED_OUT_TRACKER)
                : null;

        if(tracker!=null){
            if(tracker.getProperty(node.getPath())!=null){
                tracker.removeProperty(node.getPath());
                repository.put(tracker.getPath(), tracker);
            } else{
                throw new CmisRuntimeException("Checked out doc not in tracker!");
            }
        } else{
            throw new CmisRuntimeException("Tracker not found");
        }

        if(node.getPath().endsWith("_pwc")){

            /*
            * Path of original copy     : /abc/def/resourceName
            * Path of checked out copy  : /abc/def/resourceName_pwc
            *
            * node==checked out doc
            * */

            //Get the original copy
            String pathOfPwc = node.getPath();
            String pathOfOriginalCopy = pathOfPwc.substring(0, pathOfPwc.indexOf("_pwc"));

            Resource resource = repository.get(pathOfOriginalCopy);
            //Reset properties
            //Allow the version series to be checked out again
            resource.setProperty(GregProperty.GREG_IS_CHECKED_OUT, "false");
            if(resource.getProperty(GregProperty.GREG_CHECKED_OUT_BY)  != null ){
    	        resource.removeProperty(GregProperty.GREG_CHECKED_OUT_BY);
            }
            repository.put(pathOfOriginalCopy, resource);
            //delete the pwc
            repository.delete(node.getPath());

        } else{
            //Document created as a pwc
            repository.delete(node.getPath());
        }

    	
    }
    
    //----Checks if isCheckedOut property exists and true
    
    private boolean isCheckedOut(Resource node){

        if(node.getPath().endsWith("_pwc")){

            String path = node.getPath();
            try {
                node = getRepository().get( path.substring(0, path.indexOf("_pwc")));
            } catch (RegistryException e) {
                log.debug(e.getMessage());
                throw new CmisObjectNotFoundException(e.getMessage(), e);
            }
        }
    	String property = node.getProperty(GregProperty.GREG_IS_CHECKED_OUT);
    	if (property == null){
    		return false;
    	}
    	if(property.equals("true")){
    		return true;
    	} else{
    		return false;
    	}
    }

}
