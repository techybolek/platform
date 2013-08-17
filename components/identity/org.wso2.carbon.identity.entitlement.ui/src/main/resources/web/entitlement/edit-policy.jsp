<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%>
<%@ page import="org.wso2.carbon.utils.ServerConstants"%>
<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%
    entitlementPolicyBean.cleanEntitlementPolicyBean();
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
	String forwardTo = null;
	String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie, serverURL, configContext);
    String policyId = request.getParameter("policyid");
    PolicyDTO policyDTO = client.getPolicy(policyId, false);
    String[] policyEditorData = policyDTO.getPolicyEditorData();

    try {
        if(EntitlementPolicyConstants.POLICY_SET_ELEMENT.equals(policyDTO.getPolicyType())){
//            PolicySetDTO policySetDTO = PolicyCreatorUtil.createPolicySetDTO(policyDTO.getPolicy());
//            entitlementPolicyBean.setPolicySetDTO(policySetDTO);
//            String[] policyMetaData = policyDTO.getBasicPolicyEditorMetaData();
//            BasicTargetDTO basicTargetDTO = PolicyCreatorUtil.
//                    createBasicTargetDTO(policyMetaData);
//            entitlementPolicyBean.setBasicTargetDTO(basicTargetDTO);
            forwardTo="create-policy-set.jsp";
        } else {
            if(EntitlementPolicyConstants.BASIC_POLICY_EDITOR.equals(policyDTO.getPolicyEditor())){
                BasicPolicyDTO basicPolicyDTO = PolicyEditorUtil.createBasicPolicyDTO(policyEditorData);

                entitlementPolicyBean.setPolicyName(basicPolicyDTO.getPolicyId());
                entitlementPolicyBean.setAlgorithmName(basicPolicyDTO.getRuleAlgorithm());
                entitlementPolicyBean.setPolicyDescription(basicPolicyDTO.getDescription());

                entitlementPolicyBean.setBasicTargetDTO(basicPolicyDTO.getTargetDTO());
                entitlementPolicyBean.setBasicRuleDTOs(basicPolicyDTO.getBasicRuleDTOs());
                entitlementPolicyBean.setEditPolicy(true);
                forwardTo="basic-policy-editor.jsp";

            } else if(EntitlementPolicyConstants.STANDARD_POLICY_EDITOR.equals(policyDTO.getPolicyEditor())){

                TargetDTO targetDTO = new TargetDTO();
                List<RuleDTO> ruleDTOs = new ArrayList<RuleDTO>();
                List<ObligationDTO> obligationDTOs = new ArrayList<ObligationDTO>();

                List<RuleElementDTO> ruleElementDTOs = PolicyEditorUtil.
                                                createRuleElementDTOs(policyDTO.getPolicy());
                PolicyElementDTO elementDTO = PolicyEditorUtil.createPolicyElementDTO(policyDTO.getPolicy());

                for(RuleElementDTO ruleElementDTO : ruleElementDTOs){
                    RuleDTO ruleDTO = new RuleDTO();
                    ruleDTO.setRuleId(ruleElementDTO.getRuleId());
                    ruleDTO.setRuleEffect(ruleElementDTO.getRuleEffect());
                    ruleDTO.setRuleDescription(ruleElementDTO.getRuleDescription());
                    ruleDTOs.add(ruleDTO);
                }

                PolicyEditorUtil.processTargetPolicyEditorData(targetDTO, policyEditorData);
                PolicyEditorUtil.processRulePolicyEditorData(ruleDTOs, policyEditorData);
                PolicyEditorUtil.processObligationPolicyEditorData(obligationDTOs, policyEditorData);

                entitlementPolicyBean.setPolicyName(elementDTO.getPolicyName());
                entitlementPolicyBean.setAlgorithmName(elementDTO.getRuleCombiningAlgorithms());
                entitlementPolicyBean.setPolicyDescription(elementDTO.getPolicyDescription());

                entitlementPolicyBean.setTargetDTO(targetDTO);
                entitlementPolicyBean.setRuleDTOs(ruleDTOs);
                entitlementPolicyBean.setObligationDTOs(obligationDTOs);
                entitlementPolicyBean.setEditPolicy(true);
                forwardTo="policy-editor.jsp";

            } else if (EntitlementPolicyConstants.SOA_POLICY_EDITOR.equals(policyDTO.getPolicyEditor())) {
                SimplePolicyEditorDTO editorDTO = PolicyEditorUtil.createSimplePolicyEditorDTO(policyEditorData);
                entitlementPolicyBean.setSimplePolicyEditorDTO(editorDTO);
                entitlementPolicyBean.setEditPolicy(true);
                forwardTo="simple-policy-editor.jsp";
            } else {
                session.setAttribute("policy", policyDTO.getPolicy());
                forwardTo="policy-view.jsp?policyid=" + policyId;
            }
        }
    } catch (Exception e) {
        session.setAttribute("policy", policyDTO.getPolicy());
        forwardTo="policy-view.jsp?policyid=" + policyId;    
    }
%>

<%@page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient"%>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.dto.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.PolicyEditorUtil" %>
<%@ page import="org.wso2.balana.utils.policy.dto.BasicPolicyDTO" %>
<%@ page import="org.wso2.balana.utils.policy.dto.PolicyElementDTO" %>
<%@ page import="org.wso2.balana.utils.policy.dto.RuleElementDTO" %>

<script
	type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
	}
</script>

<script type="text/javascript">
	forward();
</script>