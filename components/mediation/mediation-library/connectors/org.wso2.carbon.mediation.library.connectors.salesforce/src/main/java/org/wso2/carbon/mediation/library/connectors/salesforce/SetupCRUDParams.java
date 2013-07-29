package org.wso2.carbon.mediation.library.connectors.salesforce;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.util.ConnectorUtils;

public class SetupCRUDParams extends AbstractConnector {
	public void connect(MessageContext messageContext) {
		setDefaultValue(messageContext, SalesforceUtil.SALESFORCE_CRUD_ALLORNONE, "1");
		setDefaultValue(messageContext, SalesforceUtil.SALESFORCE_CRUD_ALLOWFIELDTRUNCATE, "0");
		setDefaultValue(messageContext, SalesforceUtil.SALESFORCE_UPDATE_EXTERNALID, "Id");
	}

	private void setDefaultValue(MessageContext messageContext, String strParamName, String strDefaultValue){
		String strValue = (String)ConnectorUtils.lookupFunctionParam(messageContext,strParamName);
		if(strValue == null || "".equals(strValue)){		
			strValue = strDefaultValue;
		}
		messageContext.setProperty(strParamName, strValue);
	}
	
}
