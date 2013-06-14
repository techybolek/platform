/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.bam.jmx.agent;

import org.apache.log4j.Logger;
import org.wso2.carbon.bam.jmx.agent.profiles.Profile;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;

public class JmxAgent {

    private static Logger log = Logger.getLogger(JmxAgent.class);
    private Profile profile;

    public JmxAgent(Profile profile) {
        this.profile = profile;

    }


    public Object getAttribute(String mBean, String attr) {

        JMXServiceURL jmxServiceURL;
        JMXConnector jmxc = null;
        MBeanServerConnection mbsc;

        //TODO: should find an elegant way to catch the non existing attributes
        try {

            jmxServiceURL = new JMXServiceURL(profile.getUrl());

            //set-up authentication
            HashMap map = new HashMap();
            String[] credentials = new String[2];
            credentials[0] = profile.getUserName();
            credentials[1] = profile.getPass();

            map.put("jmx.remote.credentials", credentials);

            jmxc = JMXConnectorFactory.connect(jmxServiceURL, map);
            mbsc = jmxc.getMBeanServerConnection();

            ObjectName mBeanName = new ObjectName(mBean);

            return mbsc.getAttribute(mBeanName, attr);


        } catch (MBeanException e1) {
            e1.printStackTrace();
        } catch (AttributeNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstanceNotFoundException e1) {
            e1.printStackTrace();
        } catch (ReflectionException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (MalformedObjectNameException e1) {
            e1.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } finally {
            if (jmxc != null) {

                try {
                    jmxc.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }


        return null;
    }

    public Profile getProfile() {
        return profile;
    }
}
