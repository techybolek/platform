/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.objectmodel.context;

import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.dataservices.objectmodel.context.FieldContextPath.PathComponent;

/**
 * This class contains utility methods required for field contexts.
 */
public class FieldContextUtils {

    public static FieldContextPath parseFieldContextPath(String path) throws FieldContextException {
        if (path == null || path.length() == 0) {
            throw new FieldContextException("The field context path should not be empty");
        }
        String[] strComps = path.split("\\.");
        List<PathComponent> pathComps = new ArrayList<FieldContextPath.PathComponent>();
        for (int i = 0; i < strComps.length; i++) {
            // sample: rs1.employees[0].address.name
            if (strComps[i].startsWith("[")) {
                throw new FieldContextException("A field context path component cannot "
                        + "start with \"[\" - " + strComps[i]);
            } else if (strComps[i].endsWith("]")) {
                int start = strComps[i].lastIndexOf('[');
                if (start == -1 || start == 0) {
                    throw new FieldContextException("Invalid field context index path "
                            + "component: " + strComps[i]);
                }
                pathComps.add(new PathComponent(strComps[i].substring(0, start)));
                try {
                    int index = Integer.parseInt(strComps[i].substring(start + 1,
                            strComps[i].length() - 1));
                    pathComps.add(new PathComponent(index));
                } catch (NumberFormatException e) {
                    throw new FieldContextException("Invalid field context index path "
                            + "component: " + strComps[i]);
                }
            } else {
                pathComps.add(new PathComponent(strComps[i]));
            }
        }
        return new FieldContextPath(pathComps.toArray(new PathComponent[pathComps.size()]));
    }

}
