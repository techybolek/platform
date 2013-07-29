package org.wso2.carbon.mediation.library.connectors.salesforce;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;

public class SetupRetriveSobjects extends AbstractConnector {
	 public void connect(MessageContext synCtx) {

	    	SynapseLog synLog = getLog(synCtx);

	        if (synLog.isTraceOrDebugEnabled()) {
	            synLog.traceOrDebug("Start : Salesforce Retrive SObjects mediator");

	            if (synLog.isTraceTraceEnabled()) {
	                synLog.traceTrace("Message : " + synCtx.getEnvelope());
	            }
	        }    	
	    	
        	SalesforceUtil salesforceUtil = SalesforceUtil.getSalesforceUtil();
        	salesforceUtil.addIds("retrieve", SalesforceUtil.SALESFORCE_RETRIVE_OBJECTIDS, synCtx, synLog);
	        	
	        if (synLog.isTraceOrDebugEnabled()) {
	        	synLog.traceOrDebug("End : Salesforce Retrive SObjects mediator");

	            if (synLog.isTraceTraceEnabled()) {
	                synLog.traceTrace("Message : " + synCtx.getEnvelope());
	            }
	        } 
	    }
}