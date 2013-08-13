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

package org.wso2.carbon.hdfs.mgt.ui;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hdfs.mgt.stub.fs.HDFSAdminHDFSServerManagementException;
import org.wso2.carbon.hdfs.mgt.stub.fs.xsd.FolderInformation;


public class GetHDFSTreeProcessor {

    private static final Log log = LogFactory.getLog(GetHDFSTreeProcessor.class);
    private static final String ROOT_PATH = "/";
    private static final String PATH_SEPARATOR = "/";    

    public static String process(
            HttpServletRequest request, HttpServletResponse response, ServletConfig config)
            throws UIException {
        return process(request, response, config, ROOT_PATH, null);
    }

    public static String process(HttpServletRequest request, HttpServletResponse response,
                                 ServletConfig config, String resourcePath, String parentId)
            throws UIException {
       
        HDFSAdminClient client;
        try {
            client = new HDFSAdminClient(config.getServletContext(), request.getSession());
        } catch (Exception e) {
            String msg = "Failed to initialize the resource service client " +
                    "to get resource tree data. " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }

        String textBoxId = request.getParameter("textBoxId");
        try {
            HDFSTreeData resourceTreeData = new HDFSTreeData();
            fillSubResourceTree(resourcePath, resourceTreeData, client,textBoxId, parentId,
                    request.getParameter("hideResources") != null);

            String displayHTML = "";
            displayHTML += resourceTreeData.getResourceTree();
            return displayHTML;

        } catch (HDFSAdminHDFSServerManagementException e) {

            String msg = "Failed to generate the resource tree for the resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new UIException(msg, e);
        }
    }

    private static String getTreeFolderIcon() {
//        if (entryBean.getSymlink() == null) {
//            return "folder.png";
//        } else if (entryBean.getSymlink().equals("symlink")) {
//            return "collection-extn.gif";
//        }
        return "folder.png";
    }

    private static String getTreeResourceIcon() {
//        if (entryBean.getSymlink() == null) {
//            return "resource.gif";
//        } else if (entryBean.getSymlink().equals("symlink")) {
//            return "resource-extn.gif";
//        }
        return "file.gif";
    }



    private static void fillSubResourceTree(
            String resourcePath,HDFSTreeData treeData, HDFSAdminClient client,String textBoxId,
            String parentId, boolean hideResources) throws HDFSAdminHDFSServerManagementException {

        String[] childPaths = {""};
        String resourceName = "";
        boolean hasChildren = false;
        
       FolderInformation[] resourceEntry;
        try {
        	resourceEntry = client.getCurrentUserFSObjects(resourcePath);
           // resourceEntry = client.getHDFSTreeEntry(resourcePath);
        } catch (Exception e) {
            String msg = "Failed to get resource tree entry for resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new HDFSAdminHDFSServerManagementException(msg, e);
        }
//        if (resourceEntry.getCollection()) {
        if (resourceEntry.length >0) {
            childPaths = Utils.getFolderPathsFromFolderInformations(resourceEntry);
            if (childPaths != null && childPaths.length > 0) hasChildren = true;
        } else if (hideResources) {
            return;
        }
        if (hasChildren) {
            for (int i = 0; childPaths.length > i; i++) {
                String[] parts = childPaths[i].split(PATH_SEPARATOR);
                String fatherId = "father_" + parentId + "_" + i;
                String childId = "child_" + parentId + "_" + i;
                if (parts != null && parts.length > 1) {
                    resourceName = parts[parts.length - 1];
                }
                /* get the child entry for the current entry */
                //HDFSTreeEntryBean childResouceEntry;
                FolderInformation[] childResourceEntry;
                try {
                	childResourceEntry = client.getCurrentUserFSObjects(childPaths[i]);
                   // childResouceEntry = client.getHDFSTreeEntry(childPaths[i]);
                } catch (Exception e) {
                    String msg = "Failed to get resource tree entry fgetSortedChildNodesor resource " +
                            childPaths[i] + ". " + e.getMessage();
                    log.warn(msg, e);
                    continue;
                }
                boolean childHasChildren = false;
                if (((FolderInformation)resourceEntry[i]).getFolder()) {
                    String []childChildPaths = Utils.getFolderPathsFromFolderInformations(childResourceEntry);
                    if (childChildPaths != null && childChildPaths.length > 0) childHasChildren = true;
                } else if (hideResources) {
                    continue;
                }
                treeData.appendToTree("<div class=\"father-object\" id=\"" + fatherId + "\">");
                /* if this has children we let it expandable */
                if(((FolderInformation)resourceEntry[i]).getFolder()) {
                  if (childHasChildren) {
                        treeData.appendToTree("<a onclick=\"loadSubTree('" + childPaths[i] + "', '" + parentId + "_" + i + "', '" + textBoxId + "', '" + (hideResources? "true" : "false") + "')\">");
                        treeData.appendToTree("<img src=\"../hdfsmgt/images/icon-tree-plus.jpg\" id=\"plus_" + parentId + "_" + i + "\" style=\"margin-right:5px;\"  />" +
                                "<img src=\"../hdfsmgt/images/icon-tree-minus.jpg\" id=\"minus_" + parentId + "_" + i + "\" style=\"display:none;margin-right:5px;\"/>");
                    } else {
                       treeData.appendToTree("<img src=\"../resources/images/spacer.gif\" style=\"width:18px;height:10px;\" />");
                    }
                    treeData.appendToTree("<a onclick=\"pickPath('" + childPaths[i] + "','" + textBoxId + "', '" + parentId + "_" + i + "');\" title=\"" + childPaths[i] + "\">" +
                            "<img src=\"../hdfsmgt/images/" + getTreeFolderIcon() + "\" style=\"margin-right:2px;\" />" +
                            resourceName +
                            "</a>");
                    treeData.appendToTree("</div>" + "<div class=\"child-objects\" id=\"" + childId + "\"></div>");
                } else {
                    treeData.appendToTree("<img src=\"../hdfsmgt/images/spacer.gif\" style=\"width:18px;height:10px;\" />");
                    treeData.appendToTree("<a class=\"plane-resource\" onclick=\"pickPath('" + childPaths[i] + "','" + textBoxId + "', '" + parentId + "_" + i + "');\" title=\"" + childPaths[i] + "\">" + "<img src=\"../hdfsmgt/images/" + getTreeResourceIcon() + "\" style=\"margin-right:2px;\"/>" + resourceName + "</a></div>");
                }
            }
        }
    }
}
