package org.wso2.carbon.mediation.library.connectors.salesforce;

import java.util.Iterator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.mediation.library.connectors.core.AbstractConnector;
import org.wso2.carbon.mediation.library.connectors.core.util.ConnectorUtils;

public class SetupDescribeSobjects extends AbstractConnector {
	 public void connect(MessageContext synCtx) {

	    	SynapseLog synLog = getLog(synCtx);

	        if (synLog.isTraceOrDebugEnabled()) {
	            synLog.traceOrDebug("Start : Salesforce describe SObjects mediator");

	            if (synLog.isTraceTraceEnabled()) {
	                synLog.traceTrace("Message : " + synCtx.getEnvelope());
	            }
	        }    	
	    		                	      
	        SOAPEnvelope envelope = synCtx.getEnvelope();
	        OMFactory fac = OMAbstractFactory.getOMFactory();
	        SOAPBody body = envelope.getBody();
	        Iterator<OMElement>bodyChildElements = body.getChildrenWithLocalName("describeSObjects");		        
	        if (bodyChildElements.hasNext()) {
	        	try{
				    OMElement bodyElement = bodyChildElements.next();     			    
				    String strSobject = (String)ConnectorUtils.lookupFunctionParam(synCtx,SalesforceUtil.SALESFORCE_SOBJECTS);
				    OMElement sObjects = AXIOMUtil.stringToOM(strSobject);
				    Iterator<OMElement>sObject = sObjects.getChildElements();
				    OMNamespace omNsurn = fac.createOMNamespace("urn:partner.soap.sforce.com", "urn");
				    //Loops sObject
				    while(sObject.hasNext()){	    			    	
				    	OMElement currentElement = sObject.next();				    				           
			            OMElement newElement = fac.createOMElement("sObjectType", omNsurn);
			            //Add the fields
			            newElement.addChild(fac.createOMText(currentElement.getText()));		            
				    	bodyElement.addChild(newElement);
				    }
	        	}catch(Exception e){
	        		synLog.error("Saleforce adaptor - error injecting sObjects to payload : " + e);
	        	}		
	        }
	        
	        if (synLog.isTraceOrDebugEnabled()) {
	        	synLog.traceOrDebug("End : Salesforce describe SObjects mediator");

	            if (synLog.isTraceTraceEnabled()) {
	                synLog.traceTrace("Message : " + synCtx.getEnvelope());
	            }
	        } 
	    }
}
