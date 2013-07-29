package org.wso2.carbon.mediation.library.connectors.salesforce;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.wso2.carbon.mediation.library.connectors.core.util.ConnectorUtils;

public final class SalesforceUtil {

	public static final String SALESFORCE_CRUD_ALLORNONE = "salesforce.crud.allOrNone";
	public static final String SALESFORCE_CRUD_ALLOWFIELDTRUNCATE = "salesforce.crud.allowFieldTruncate";
	public static final String SALESFORCE_UPDATE_EXTERNALID = "salesforce.update.externalId";
	public static final String SALESFORCE_UPSERT_SOBJECTS = "salesforce.upsert.sobjects";	
	
	public static final String SALESFORCE_UPDATE_SOBJECTS = "salesforce.update.sobjects";	

	public static final String SALESFORCE_DELETE_SOBJECTS = "salesforce.delete.sobjects";
	
	public static final String SALESFORCE_DESCRIBE_SOBJECTS = "salesforce.describe.sobjects";
	
	public static final String SALESFORCE_EMAIL_SENDEMAILMESSAGE = "salesforce.email.sendEmailMessage";
	
	public static final String SALESFORCE_EMAIL_SENDEMAIL = "salesforce.email.sendEmail";
	
	public static final String SALESFORCE_RETRIVE_OBJECTIDS = "salesforce.retrieve.objectIDS";
	
	public static final String SALESFORCE_CREATE_SOBJECTTYPE = "type";
	public static final String SALESFORCE_CREATE_SOBJECTS = "salesforce.create.sobjects";
	
	public SalesforceUtil(){
		
	}
	
	public static synchronized SalesforceUtil getSalesforceUtil(){
		return new SalesforceUtil();
	}
	
	public void addSobjects(String strOperation, String strParamName, MessageContext synCtx, SynapseLog synLog, String strExternalId){
        SOAPEnvelope envelope = synCtx.getEnvelope();
        OMFactory fac = OMAbstractFactory.getOMFactory();
        SOAPBody body = envelope.getBody();
        Iterator<OMElement>bodyChildElements = body.getChildrenWithLocalName(strOperation);		        
        if (bodyChildElements.hasNext()) {
        	try{
			    OMElement bodyElement = bodyChildElements.next();     			    
			    if(strExternalId != null){	    		            
		            OMNamespace omNs = fac.createOMNamespace("urn:partner.soap.sforce.com", "urn");
		            OMElement value = fac.createOMElement("externalIDFieldName", omNs);
		            value.addChild(fac.createOMText(strExternalId));          			    	
			    	bodyElement.addChild(value);
			    }
			    String strSobject = (String)ConnectorUtils.lookupFunctionParam(synCtx,strParamName);
			    OMElement sObjects = AXIOMUtil.stringToOM(strSobject);
			    Iterator<OMElement>sObject = sObjects.getChildElements();
			    String strType = sObjects.getAttributeValue(new QName(SalesforceUtil.SALESFORCE_CREATE_SOBJECTTYPE));
			    OMElement tmpElement = null;
	            OMNamespace omNsurn = fac.createOMNamespace("urn:partner.soap.sforce.com", "urn");
	            OMNamespace omNsurn1 = fac.createOMNamespace("urn:sobject.partner.soap.sforce.com", "urn1");			    
			    //Loops sObject
			    while(sObject.hasNext()){	    			    	
			    	OMElement currentElement = sObject.next();			    	
		            OMElement newElement = fac.createOMElement("sObjects", omNsurn);
		            //Add Object type
		            if(strType != null){
		            	tmpElement = fac.createOMElement("type", omNsurn1);
		            	tmpElement.addChild(fac.createOMText(strType));
		            	newElement.addChild(tmpElement);
		            }
		            //Add the fields
		            Iterator<OMElement>sObjectFields = currentElement.getChildElements();
		            while(sObjectFields.hasNext()){
		            	OMElement sObjectField = sObjectFields.next();
			            tmpElement = fac.createOMElement(sObjectField.getLocalName(), omNsurn1);
    		            tmpElement.addChild(fac.createOMText(sObjectField.getText()));
    		            newElement.addChild(tmpElement);
		            }
		            
			    	bodyElement.addChild(newElement);
			    }
        	}catch(Exception e){
        		synLog.error("Saleforce adaptor - error injecting sObjects to payload : " + e);
        	}		
        }
	}	
	public void addIds(String strOperation, String strParamName, MessageContext synCtx, SynapseLog synLog){
        SOAPEnvelope envelope = synCtx.getEnvelope();
        OMFactory fac = OMAbstractFactory.getOMFactory();
        SOAPBody body = envelope.getBody();
        Iterator<OMElement>bodyChildElements = body.getChildrenWithLocalName(strOperation);		        
        if (bodyChildElements.hasNext()) {
        	try{			    
        		OMElement bodyElement = bodyChildElements.next();			    
        		Iterator<OMElement>cElements = bodyElement.getChildElements();
        		if (cElements != null && cElements.hasNext()) {
        			cElements.next();
        		}        		
			    String strSobject = (String)ConnectorUtils.lookupFunctionParam(synCtx,strParamName);
			    OMElement sObjects = AXIOMUtil.stringToOM(strSobject);
			    Iterator<OMElement>sObject = sObjects.getChildElements();
			    OMNamespace omNsurn = fac.createOMNamespace("urn:partner.soap.sforce.com", "urn");
			    //Loops sObject
			    while(sObject.hasNext()){	    			    	
			    	OMElement currentElement = sObject.next();			    			            
		            OMElement newElement = fac.createOMElement("ids", omNsurn);
		            //Add the fields
		            newElement.addChild(fac.createOMText(currentElement.getText()));		            
			    	bodyElement.addChild(newElement);
			    }
        	}catch(Exception e){
        		synLog.error("Saleforce adaptor - error injecting sObjects to payload : " + e);
        	}		
        }
	}	
}
