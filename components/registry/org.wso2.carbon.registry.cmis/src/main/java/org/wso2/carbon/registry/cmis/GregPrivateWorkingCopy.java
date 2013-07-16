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

import org.apache.chemistry.opencmis.server.support.TypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.cmis.impl.DocumentTypeHandler;
import org.wso2.carbon.registry.cmis.impl.GregProperty;

/**
 * Instances of this class represent a private working copy of a cmis:document backed by an underlying
 * GREG <code>Node</code>.
 */
public class GregPrivateWorkingCopy extends GregVersionBase {
    private static final Logger log = LoggerFactory.getLogger(GregPrivateWorkingCopy.class);

    /**
     * Name of a private working copy
     */
    public static final String PWC_NAME = "pwc";

    public GregPrivateWorkingCopy(Registry repository, Resource node, GregTypeManager typeManager, PathManager pathManager) {
        
        super(repository, node, typeManager, pathManager);
    }

    /**
     * @return <code>true</code> iff <code>versionName</code> is the name of private working copy.
     * @see GregPrivateWorkingCopy#PWC_NAME
     */
    public static boolean denotesPwc(Registry registry, String versionName) {
        try {
            Resource resource = registry.get(versionName);
            String property = resource.getProperty(GregProperty.GREG_CREATED_AS_PWC);
            String checkedOut = resource.getProperty(GregProperty.GREG_IS_CHECKED_OUT);

            if(resource.getPath().endsWith("_pwc") || ( property != null && property.equals("true") )
                                                   || ( checkedOut != null && checkedOut.equals("true") )){
                return true;
            }
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    /**
     * @param objectId
     * @return <code>true</code> iff <code>objectId</code> is the id of a private working copy.
     * @see GregPrivateWorkingCopy#PWC_NAME
     */
    public static boolean isPwc(Registry registry, String objectId) {
        String property = null;
        try {
            property = registry.get(objectId).getProperty(GregProperty.GREG_CREATED_AS_PWC);
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        boolean createdAsPwc = (property != null && property.equals("true"));
        return objectId.endsWith('_' + PWC_NAME) || createdAsPwc;
    }

    @Override
    protected Resource getContextNode() {
        return getNode();
    }

    @Override
    protected String getPwcId() throws RegistryException {
        return getObjectId();
    }

    @Override
    protected String getObjectId() throws RegistryException {
        String property = getNode().getProperty(GregProperty.GREG_CREATED_AS_PWC);
        if(property != null && property.equals("true")){
            return getVersionSeriesId();
        } else{
            return getVersionSeriesId() + '_' + PWC_NAME;
        }
    }

    @Override
    protected String getVersionLabel() throws RegistryException {
        return PWC_NAME;
    }

    @Override
    protected boolean isLatestVersion() throws RegistryException {
        return false;
    }

    @Override
    protected boolean isMajorVersion() throws RegistryException {
        return false;
    }

    @Override
    protected boolean isLatestMajorVersion() throws RegistryException {
        return false; 
    }

    @Override
    protected String getCheckInComment() {
        return "";
    }

	@Override
	public GregObject create(Resource resource) {
		// TODO Auto-generated method stub
		//call the relevant typeHandler's getGregNode method
		//return new GregVersionBase(getRepository(), super.version, resource, typeManager, pathManager);
	    return null;
    }

}
