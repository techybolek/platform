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

package org.wso2.carbon.identity.entitlement.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.utils.exception.PolicyBuilderException;
import org.wso2.balana.utils.policy.PolicyBuilder;
import org.wso2.balana.utils.policy.dto.*;
import org.wso2.carbon.identity.entitlement.common.PolicyEditorException;
import org.wso2.carbon.identity.entitlement.ui.dto.*;
import org.wso2.carbon.identity.entitlement.ui.util.PolicyEditorUtil;

import java.lang.*;
import java.util.List;

/**
 * create XACML policy and convert it to a String Object
 */
public class EntitlementPolicyCreator {

    private static Log log = LogFactory.getLog(EntitlementPolicyCreator.class);

    /**
     * Create XACML policy using the data received from basic policy wizard
     *
     * @param basicPolicyDTO BasicPolicyDTO
     * @return  String object of the XACML policy
     * @throws PolicyEditorException throws
     */
    public String createBasicPolicy(BasicPolicyDTO basicPolicyDTO) throws PolicyEditorException {

        if(basicPolicyDTO == null){
            throw new PolicyEditorException("Policy object can not be null");
        }

        try {
            return PolicyBuilder.getInstance().build(basicPolicyDTO);
        } catch (PolicyBuilderException e) {
            log.error(e);
            throw new PolicyEditorException("Error while building policy");
        }
    }


    /**
     * Create XACML policy using the data received from basic policy wizard
     *
     * @param policyDTO PolicyDTO
     * @return  String object of the XACML policy
     * @throws PolicyEditorException throws
     */
    public String createPolicy(PolicyDTO policyDTO) throws PolicyEditorException {

        if(policyDTO == null){
            throw new PolicyEditorException("Policy object can not be null");
        }

        PolicyElementDTO policyElementDTO = new PolicyElementDTO();
        policyElementDTO.setPolicyName(policyDTO.getPolicyId());
        policyElementDTO.setRuleCombiningAlgorithms(policyDTO.getRuleAlgorithm());
        policyElementDTO.setPolicyDescription(policyDTO.getDescription());
        policyElementDTO.setVersion(policyDTO.getVersion());

        if(policyDTO.getTargetDTO() != null){
            TargetElementDTO targetElementDTO = PolicyEditorUtil.
                                                createTargetElementDTO(policyDTO.getTargetDTO());
            policyElementDTO.setTargetElementDTO(targetElementDTO);
        }

        if(policyDTO.getRuleDTOs() != null){
            for(RuleDTO ruleDTO : policyDTO.getRuleDTOs()){
                RuleElementDTO ruleElementDTO = PolicyEditorUtil.createRuleElementDTO(ruleDTO);
                policyElementDTO.addRuleElementDTO(ruleElementDTO);
            }
        }

        if(policyDTO.getObligationDTOs() != null){
            List<ObligationElementDTO> obligationElementDTOs = PolicyEditorUtil.
                                                    createObligation(policyDTO.getObligationDTOs());
            policyElementDTO.setObligationElementDTOs(obligationElementDTOs);
        }

        try {
            return PolicyBuilder.getInstance().build(policyElementDTO);
        } catch (PolicyBuilderException e) {
            throw new PolicyEditorException("Error while building XACML Policy");
        }
    }


    /**
     * Create XACML policy using the data received from basic policy wizard
     * 
     * @param policyEditorDTO complete policy editor object
     * @return  String object of the XACML policy
     * @throws PolicyEditorException throws
     */
    public String createSOAPolicy(SOAPolicyEditorDTO policyEditorDTO) throws PolicyEditorException {

        return PolicyEditorUtil.createSOAPolicy(policyEditorDTO);
    }


//    /**
//     * Create policy set using the added policy ot policy sets
//     *
//     * @param policySetDTO   policy set element
//     * @return String object of the XACML policy Set
//     * @throws EntitlementPolicyCreationException  throws
//     */
//    public String createPolicySet(PolicySetDTO policySetDTO)
//            throws EntitlementPolicyCreationException {
//        try {
//            Document doc = createNewDocument();
//            if(doc != null) {
//                doc.appendChild(PolicyCreatorUtil.createPolicySetElement(policySetDTO, doc));
//                StringBuilder policySet = new StringBuilder(PolicyCreatorUtil.getStringFromDocument(doc));
//                if(policySetDTO.getPolicies() != null){
//                    for(String policy : policySetDTO.getPolicies()){
//                        policySet.insert(policySet.indexOf(">") + 1, policy);
//                    }
//                }
//                return policySet.toString();
//            }
//        } catch (EntitlementPolicyCreationException e) {
//            throw new EntitlementPolicyCreationException("Error While Creating Policy Set", e);
//        }
//        return null;
//    }


    /**
     * Create basic XACML request
     *
     * @param requestElementDTO  request element
     * @return String object of the XACML request
     * @throws EntitlementPolicyCreationException  throws
     */
//    public String createBasicRequest(RequestElementDTO requestElementDTO)
//            throws EntitlementPolicyCreationException {
//        try {
//            Document doc = createNewDocument();
//            if(doc != null) {
//                doc.appendChild(PolicyCreatorUtil.createBasicRequestElement(requestElementDTO, doc));
//                return PolicyCreatorUtil.getStringFromDocument(doc);
//            }
//        } catch (EntitlementPolicyCreationException e) {
//            throw new EntitlementPolicyCreationException("Error While Creating XACML Request", e);
//        }
//        return null;
//    }
}
