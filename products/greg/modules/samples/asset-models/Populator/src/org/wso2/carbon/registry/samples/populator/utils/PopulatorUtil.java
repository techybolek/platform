package org.wso2.carbon.registry.samples.populator.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;

import java.io.*;
import java.io.File;
import java.lang.String;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class PopulatorUtil {

    public static String authenticate(ConfigurationContext ctx, String serverURL, String username, String password) throws AxisFault, AuthenticationException {
        String serviceEPR = serverURL + "AuthenticationAdmin";

        AuthenticationAdminStub stub = new AuthenticationAdminStub(ctx, serviceEPR);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            boolean result = stub.login(username, password, new URL(serviceEPR).getHost());
            if (result){
                return (String) stub._getServiceClient().getServiceContext().
                        getProperty(HTTPConstants.COOKIE_STRING);
            }
            return null;
        } catch (Exception e) {
            String msg = "Error occurred while logging in";
            throw new AuthenticationException(msg, e);
        }
    }

    public static ReportConfigurationBean getReportConfigurationBean(String name, String template, String type,
                                                                     String reportClass){
        ReportConfigurationBean bean = new ReportConfigurationBean();
        bean.setName(name);
        bean.setTemplate(template);
        bean.setType(type.toLowerCase());
        bean.setReportClass(reportClass);
        return bean;
    }

    public static Workbook[] getWorkbooks(File usersDir, String prefix) {
        List<Workbook> workbooks = new LinkedList<Workbook>();
        FileFilter filter = new PrefixFileFilter(prefix);
        File[] files = usersDir.listFiles(filter);
        for (File file : files) {
            try {
                InputStream ins = new BufferedInputStream(new FileInputStream(files[0]));
                String extension = FilenameUtils.getExtension(files[0].getName());
                if (extension.equals("xlsx")) {
                    workbooks.add(new XSSFWorkbook(ins));
                } else {
                    POIFSFileSystem fs = new POIFSFileSystem(ins);
                    workbooks.add(new HSSFWorkbook(fs));
                }
            } catch (Exception e) {
                throw new RuntimeException("Workbook creation failed", e);
            }
        }
        return workbooks.toArray(new Workbook[workbooks.size()]);
    }

    public static Workbook getWorkbook(File usersDir, String prefix) {
        Workbook[] workbooks = getWorkbooks(usersDir, prefix);
        if (workbooks.length == 0) {
            return null;
        }
        return workbooks[0];
    }

}
