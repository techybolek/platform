package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceStub;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceStub;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.lang.Exception;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

public class LifeCycleManagementClient {

    private LifeCycleManagementServiceStub lcmStub;
    private CustomLifecyclesChecklistAdminServiceStub stub;

    public LifeCycleManagementClient(String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {
        try {
            String epr = backendServerURL + "LifeCycleManagementService";
            lcmStub = new LifeCycleManagementServiceStub(configContext, epr);

            ServiceClient client = lcmStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate lifecycle management service client. " + e.getMessage();
            throw new RegistryException(msg, e);
        }
        try {
            String epr = backendServerURL + "CustomLifecyclesChecklistAdminService";
            stub = new CustomLifecyclesChecklistAdminServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            String msg = "Failed to initiate checklist lifecycle admin service client. " + e.getMessage();
            throw new RegistryException(msg, e);
        }
    }

    public void createLifecycle(String configuration) throws Exception {
        lcmStub.createLifecycle(configuration);
    }

    public void invokeAspect(String path, String aspect, String action, String[] items, Map<String, String> params)
            throws Exception {
        if (params.size() == 0) {
            stub.invokeAspect(path, aspect, action, items);
        } else {
            List<ArrayOfString> paramsList = new LinkedList<ArrayOfString>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                ArrayOfString arrayOfString = new ArrayOfString();
                arrayOfString.addArray(e.getKey());
                arrayOfString.addArray(e.getValue());
                paramsList.add(arrayOfString);
            }
            stub.invokeAspectWithParams(path, aspect, action, items, paramsList.toArray(
                    new ArrayOfString[paramsList.size()]));
        }
    }

    public void addAspect(String path, String aspect) throws Exception {
        stub.addAspect(path, aspect);
    }

    public void removeAspect(String path, String aspect) throws Exception {
        stub.removeAspect(path, aspect);
    }
}

