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

package org.wso2.carbon.automation.core.context.featuremanagementContext;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.wso2.carbon.automation.core.context.ContextConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;


public class FeatureManagementContextFactory {
    FeatureManagementContext featureManagementContext;

    public void FeatureManagementContextFactory() {
        featureManagementContext = new FeatureManagementContext();
    }

    public FeatureManagementContext getFeatureManagementContext() {
        return featureManagementContext;
    }

    /*
     * List all P2repositories
     */
    public void createFeatureManagementContext(OMNode element) {

        HashMap<String, P2Repositories> p2RepositoriesMap = new HashMap<String, P2Repositories>();
        OMNode node;
        Iterator children = ((OMElementImpl) element).getChildElements();
        String p2RepoName = null;
        while (children.hasNext()) {

            P2Repositories p2Repositories = new P2Repositories();
            node = (OMNode) children.next();
            p2RepoName = ((OMElementImpl) node).getAttributeValue(QName.valueOf(ContextConstants.FEATURE_MANAGEMENT_CONTEXT_P2RESITORIES_NAME));
            p2Repositories.setName(p2RepoName);
            Iterator repoList = ((OMElementImpl) node).getChildElements();

            //add all repositories to current p2repository
            while (repoList.hasNext()) {
                Repository repository = new Repository();
                OMNode repositoryNode = (OMNode) repoList.next();
                String repoId = ((OMElementImpl) repositoryNode).getAttributeValue(QName.valueOf(ContextConstants.FEATURE_MANAGEMENT_CONTEXT_REPOSITORY_ID));
                String url = ((OMElementImpl) repositoryNode).getText();
                repository.setRepo_id(repoId);
                repository.setUrl(url);

                // add the current repository to the p2repository
                p2Repositories.addRepository(repository);


            }
            //add current p2repository to the hash map
            p2RepositoriesMap.put(p2RepoName, p2Repositories);

        }
        featureManagementContext.setP2Repositories(p2RepositoriesMap);
    }
}
