package org.apache.synapse.transport.nhttp.util;

import org.apache.axis2.AxisFault;

/**
 * @author Jeewantha
 */
public interface MultiSSLProfileReloaderMBean {

    public String reloadSSLProfileConfig() throws AxisFault;
}
