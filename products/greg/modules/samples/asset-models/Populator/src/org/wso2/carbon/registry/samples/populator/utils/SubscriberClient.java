package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.governance.notifications.stub.InfoAdminServiceStub;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.stream.XMLStreamException;
import java.lang.String;
import java.rmi.RemoteException;

public class SubscriberClient {

    private InfoAdminServiceStub stub;

    public SubscriberClient(String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {
        try {
            String epr = backendServerURL + "InfoAdminService";
            stub = new InfoAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate info admin service client. " + e.getMessage();
            throw new RegistryException(msg, e);
        }
    }

    public void subscribe(String path, String endpoint, String eventName) throws Exception {
        stub.subscribe(path, endpoint, eventName, null);
    }
}

