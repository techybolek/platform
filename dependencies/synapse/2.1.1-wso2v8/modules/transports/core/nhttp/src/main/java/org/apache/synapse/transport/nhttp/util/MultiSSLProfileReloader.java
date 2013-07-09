package org.apache.synapse.transport.nhttp.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.transport.nhttp.HttpCoreNIOMultiSSLListener;
import org.apache.synapse.transport.nhttp.config.ServerConnFactoryBuilder;

/**
 * @author Jeewantha
 */
public class MultiSSLProfileReloader implements MultiSSLProfileReloaderMBean {

    HttpCoreNIOMultiSSLListener httpCoreNIOMultiSSLListener;
    TransportInDescription transportInDescription;

    public MultiSSLProfileReloader(HttpCoreNIOMultiSSLListener multiSSLListener, TransportInDescription inDescription) {
        this.httpCoreNIOMultiSSLListener = multiSSLListener;
        this.transportInDescription = inDescription;
        MBeanRegistrar.getInstance().registerMBean(this, "MultiSSLProfileReload", "reload");
    }

    public String reloadSSLProfileConfig() throws AxisFault {
        Parameter oldParameter = transportInDescription.getParameter("SSLProfiles");
        Parameter profilePathParam = transportInDescription.getParameter("SSLProfilesConfigPath");
        if(oldParameter!=null && profilePathParam!=null) {
            transportInDescription.removeParameter(oldParameter);
            ServerConnFactoryBuilder builder = new ServerConnFactoryBuilder(transportInDescription, null);
            TransportInDescription loadedTransportIn = builder.loadMultiProfileSSLConfig();
            if (loadedTransportIn != null) {
                transportInDescription=loadedTransportIn;
                httpCoreNIOMultiSSLListener.reload(transportInDescription);
                return "SSLProfiles reloaded Successfully";
            }
            //add old value back
            transportInDescription.addParameter(oldParameter);
        }
        return "Failed to reload SSLProfiles";
    }
}
