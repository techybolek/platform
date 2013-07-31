package org.wso2.carbon.mediation.library.connectors.salesforce;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;

public class SetupUpsertSobjects extends AbstractConnector {
	 public void connect(MessageContext synCtx) {

	    	SynapseLog synLog = getLog(synCtx);

	        if (synLog.isTraceOrDebugEnabled()) {
	            synLog.traceOrDebug("Start : Salesforce Upsert SObjects mediator");

	            if (synLog.isTraceTraceEnabled()) {
	                synLog.traceTrace("Message : " + synCtx.getEnvelope());
	            }
	        }    	
	    	
	        SalesforceUtil salesforceUtil = SalesforceUtil.getSalesforceUtil();
	        salesforceUtil.addSobjects("upsert", SalesforceUtil.SALESFORCE_SOBJECTS, synCtx, synLog, (String)synCtx.getProperty(SalesforceUtil.SALESFORCE_EXTERNALID));	           

	        if (synLog.isTraceOrDebugEnabled()) {
	        	synLog.traceOrDebug("End : Salesforce Upsert SObjects mediator");

	            if (synLog.isTraceTraceEnabled()) {
	                synLog.traceTrace("Message : " + synCtx.getEnvelope());
	            }
	        }  
	    }
}
