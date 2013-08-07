/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.message.processor.impl.sampler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.processor.impl.ScheduledMessageProcessor;
import org.quartz.JobDataMap;

import java.util.StringTokenizer;

public class SamplingProcessor extends ScheduledMessageProcessor {
    private static final Log log = LogFactory.getLog(SamplingProcessor.class);

    public static final String CONCURRENCY = "concurrency";

    public static final String SEQUENCE = "sequence";

    private SamplingProcessorView view;

    @Override
    public void init(SynapseEnvironment se) {

        String thisServerName = se.getServerContextInformation().getServerConfigurationInformation()
                .getServerName();
        Object pinnedServersObj = this.parameters.get("pinnedServers");

        if (pinnedServersObj != null && pinnedServersObj instanceof String) {

            boolean pinned = false;
            String pinnedServers = (String) pinnedServersObj;
            StringTokenizer st = new StringTokenizer(pinnedServers, " ,");

            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (thisServerName.equals(token)) {
                    pinned = true;
                }
            }
            if (!pinned) {
                log.info("Message processor '" + name + "' pinned on '" + pinnedServers + "' not starting on" +
                        " this server '" + thisServerName + "'");
            }
        }

        super.init(se);
        view = new SamplingProcessorView(this);
        // register MBean
        org.apache.synapse.commons.jmx.MBeanRegistrar.getInstance().registerMBean(view,
                "Message Sampling Processor view", getName());
    }

    @Override
    protected JobDataMap getJobDataMap() {
        JobDataMap jdm = new JobDataMap();
        jdm.put(PROCESSOR_INSTANCE, this);
        return jdm;
    }

    public boolean isActive() {
        return !isDeactivated();
    }

    public SamplingProcessorView getView() {
        return view;
    }
}
