/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/


package org.wso2.carbon.identity.entitlement;

import org.wso2.carbon.identity.entitlement.dto.StatusHolder;

import java.util.Comparator;

/**
 *
 */
public class StatusHolderComparator implements Comparator {



    @Override
    public int compare(Object o1, Object o2) {

        StatusHolder dto1 = (StatusHolder)o1;
        StatusHolder dto2 = (StatusHolder)o2;
        if(Long.parseLong(dto1.getTimeInstance()) > Long.parseLong(dto2.getTimeInstance())){
            return -1;
        } else if(Long.parseLong(dto1.getTimeInstance()) == Long.parseLong(dto2.getTimeInstance())){
            return 0;
        } else {
            return 1;
        }
    }
}
