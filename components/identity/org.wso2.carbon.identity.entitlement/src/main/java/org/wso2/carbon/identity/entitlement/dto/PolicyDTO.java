/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.dto;

import java.util.*;

/**
 * This class encapsulate the XACML policy related the data
 */
public class PolicyDTO {

    private String policy;

    private String policyId;
    
    private boolean active;

    private boolean promote;

    private String policyType;

    private String policyEditor;

    private String[] policyEditorData = new String[0];

    private boolean policyEditable;

    private boolean policyCanDelete;

    private int policyOrder;

    private String version;

    private String neighborId;    

    private AttributeDTO[] attributeDTOs = new AttributeDTO[0];

    private String[] policySetIdReferences = new String[0];

    private String[] policyIdReferences = new String[0];

    private StatusHolder[] policyStatusHolders = new StatusHolder[0];

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }


    public String getPolicyType() {
        return policyType;
    }

    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }

    public String getPolicyEditor() {
        return policyEditor;
    }

    public void setPolicyEditor(String policyEditor) {
        this.policyEditor = policyEditor;
    }

    public String[] getPolicyEditorData() {
        return Arrays.copyOf(policyEditorData, policyEditorData.length);
    }

    public void setPolicyEditorData(String[] policyEditorData) {
        this.policyEditorData = Arrays.copyOf(policyEditorData,
                                                                policyEditorData.length);
    }

    public AttributeDTO[] getAttributeDTOs() {
        return Arrays.copyOf(attributeDTOs, attributeDTOs.length);
    }

    public void setAttributeDTOs(AttributeDTO[] attributeDTOs) {
        this.attributeDTOs = Arrays.copyOf(attributeDTOs, attributeDTOs.length);
    }

    public boolean isPolicyEditable() {
        return policyEditable;
    }

    public void setPolicyEditable(boolean policyEditable) {
        this.policyEditable = policyEditable;
    }

    public boolean isPolicyCanDelete() {
        return policyCanDelete;
    }

    public void setPolicyCanDelete(boolean policyCanDelete) {
        this.policyCanDelete = policyCanDelete;
    }

    public int getPolicyOrder() {
        return policyOrder;
    }

    public void setPolicyOrder(int policyOrder) {
        this.policyOrder = policyOrder;
    }

    public String getNeighborId() {
        return neighborId;
    }

    public void setNeighborId(String neighborId) {
        this.neighborId = neighborId;
    }

    public String[] getPolicySetIdReferences() {
        return Arrays.copyOf(policySetIdReferences, policySetIdReferences.length);
    }

    public void setPolicySetIdReferences(String[] policySetIdReferences) {
        this.policySetIdReferences = Arrays.copyOf(policySetIdReferences, policySetIdReferences.length);
    }

    public String[] getPolicyIdReferences() {
        return Arrays.copyOf(policyIdReferences, policyIdReferences.length);
    }

    public void setPolicyIdReferences(String[] policyIdReferences) {
        this.policyIdReferences = Arrays.copyOf(policyIdReferences, policyIdReferences.length);
    }

    public boolean isPromote() {
        return promote;
    }

    public void setPromote(boolean promote) {
        this.promote = promote;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public StatusHolder[] getPolicyStatusHolders() {
        return Arrays.copyOf(policyStatusHolders, policyStatusHolders.length);
    }

    public void setPolicyStatusHolders(StatusHolder[] policyStatusHolders) {
        this.policyStatusHolders = Arrays.copyOf(policyStatusHolders, policyStatusHolders.length);
    }

    public void addPolicyStatusHolder(List<StatusHolder> publishStatusHolders)  {
        List<StatusHolder> list = new ArrayList<StatusHolder>(Arrays.asList(this.policyStatusHolders));
        list.addAll(publishStatusHolders);
        this.policyStatusHolders = list.toArray(new StatusHolder[list.size()]);
//        List<StatusHolder> list = new ArrayList<StatusHolder>(Arrays.asList(this.policyStatusHolders));
//        for(StatusHolder holder : publishStatusHolders){
//            list.add(holder);
//        }
//
//        StatusHolder[] array = new StatusHolder[10];
//
//        for(int i = 0; i < list.size() ; i++ ){
//            array[i] = list.get((i));
//        }
//
//        this.policyStatusHolders  = array;
    }
}
