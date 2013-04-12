/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.pap;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeTreeNodeDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
public class BasicPolicyEditorDataFinder implements PolicyEditorDataFinderModule {


    private Map<String, String> attributeIdMap = new HashMap<String, String>();

    private Map<String, String> attributeDataTypeMap = new HashMap<String, String>();

    @Override
    public void init(Properties properties) throws Exception {

        if(properties != null){
            for(Object o :  properties.keySet()){
                String attribute = (String) o;
                String[] values= properties.getProperty(attribute).split(",");
                if(values.length > 0){
                    attributeIdMap.put(attribute, values[0]);
                }
                if(values.length > 1){
                    attributeDataTypeMap.put(attribute, values[1]);
                }
            }
        }

    }

    @Override
    public final String getModuleName() {
        return null;
    }

    @Override
    public final Map<String, String> getSupportedCategories() {
        
        Map<String, String> newMap = new HashMap<String, String>();

        newMap.put(EntitlementConstants.RESOURCE_CATEGORY_ID, EntitlementConstants.RESOURCE_CATEGORY_URI);
        newMap.put(EntitlementConstants.SUBJECT_CATEGORY_ID, EntitlementConstants.SUBJECT_CATEGORY_URI);
        newMap.put(EntitlementConstants.ACTION_CATEGORY_ID, EntitlementConstants.ACTION_CATEGORY_URI);
        newMap.put(EntitlementConstants.ENVIRONMENT_CATEGORY_ID, EntitlementConstants.ENVIRONMENT_CATEGORY_URI);
        
        return newMap;
    }

    @Override
    public final AttributeTreeNodeDTO getAttributeValueData(String category) throws Exception {
        return null;
    }

    @Override
    public Map<String, String> getSupportedAttributeIds(String category) throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        if(EntitlementConstants.SUBJECT_CATEGORY_ID.equals(category)){
            int tenantId =  CarbonContext.getCurrentContext().getTenantId();
            ClaimManager claimManager = EntitlementServiceComponent.getRealmservice().
                    getTenantUserRealm(tenantId).getClaimManager();
            ClaimMapping[] claims = claimManager.getAllClaimMappings(UserCoreConstants.DEFAULT_CARBON_DIALECT);
            for(ClaimMapping mapping : claims){
            	Claim claim = mapping.getClaim();
                if(claim.isSupportedByDefault()){
                    values.put(claim.getDisplayTag(), claim.getClaimUri());
                }
            }
        }
        values.putAll(attributeIdMap);
        return values;
    }

    @Override
    public final Set<String> getAttributeDataTypes(String category) throws Exception {
        return null;  
    }

    @Override
    public final boolean isFullPathSupported() {
        return false;
    }

    @Override
    public final boolean isHierarchicalTree() {
        return false;
    }

    @Override
    public final Map<String, String> getSupportedRuleFunctions() {
        return null;
    }

    @Override
    public Map<String, String> getAttributeIdDataTypes() {
        Map<String, String> values = new HashMap<String, String>();
        values.put("http://wso2.org/claims/age", "http://www.w3.org/2001/XMLSchema#integer");
        values.putAll(attributeDataTypeMap);
        return values;
    }

    @Override
    public final Map<String, String> getSupportedTargetFunctions() {
        return null;
    }

    @Override
    public String getDefaultAttributeId(String category) {
        if(EntitlementConstants.RESOURCE_CATEGORY_ID.equals(category)){
            return EntitlementConstants.RESOURCE_ID_DEFAULT;
        } else if(EntitlementConstants.ACTION_CATEGORY_ID.equals(category)){
            return EntitlementConstants.ACTION_ID_DEFAULT;
        } else if(EntitlementConstants.SUBJECT_CATEGORY_ID.equals(category)){
            return EntitlementConstants.SUBJECT_ID_DEFAULT;
        } else if(EntitlementConstants.ENVIRONMENT_CATEGORY_ID.equals(category)){
            return EntitlementConstants.ENVIRONMENT_ID_DEFAULT;
        }

        return null;
    }

    @Override
    public final String getDefaultAttributeDataType(String category) {
        return EntitlementConstants.STRING_DATA_TYPE;
    }
}
