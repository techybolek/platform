/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.bam.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.common.util.AppFactoryUtil;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;

import java.util.concurrent.LinkedBlockingQueue;

public class BamDataPublisher {


    private String APP_CREATION_STREAM = "org.wso2.carbon.appfactory.appCreation" ;
    private String APP_CREATION_STREAM_VERSION = "1.0.0";
    private LinkedBlockingQueue<Event> publishDataQueue;
    private int MAX_QUEUE_SIZE = 10;
    private boolean ENABLE_DATA_PUBLISHING;

    private static Log log = LogFactory.getLog(BamDataPublisher.class);


    public BamDataPublisher(){
        try {
            AppFactoryConfiguration config = AppFactoryUtil.getAppfactoryConfiguration();
            String EnableStatPublishing = config.getFirstProperty("BAM.EnableStatPublishing");
            if (EnableStatPublishing != null && EnableStatPublishing.equals("true")) {
                ENABLE_DATA_PUBLISHING = true;
            }

        } catch (AppFactoryException e) {
            String errorMsg = "Unable to create Data publisher " + e.getMessage();
            log.error(errorMsg, e);
        }
    }


    private String appCreationStream =  "{"+
            " 'name': '"+APP_CREATION_STREAM+"'," +
            " 'version': '"+APP_CREATION_STREAM_VERSION+"',"+
            " 'nickName': 'Application Creation Information',"+
            " 'description': 'This stream will store app creation data to BAM',"+
            "   'payloadData':["+
            "    {'name':'applicationName','type':'string'},"+
            "    {'name':'applicationKey','type':'string'},"+
            "    {'name':'timeStamp','type':'double'},"+
            "    {'name':'user',  'type':'string' },"+
            "    {'name':'appType','type':'string' },"+
            "    {'name':'repoType', 'type':'string'},"+
            "    {'name':'appDescription', 'type':'string'},"+
            "    {'name':'tenantId', 'type':'string'}"+
            "    ]"+
            "    }";

    AsyncDataPublisher asyncDataPublisher=new AsyncDataPublisher("tcp://localhost:7614", "admin", "admin");

    public void PublishAppCreationEvent(String appName, String appKey, String appDescription,String appType,String repoType, double timestamp, String tenantId, String username){

        if(ENABLE_DATA_PUBLISHING != true){
            return;
        }

        Event event = new Event();
        if(!asyncDataPublisher.isStreamDefinitionAdded(APP_CREATION_STREAM,APP_CREATION_STREAM_VERSION)){
           asyncDataPublisher.addStreamDefinition(appCreationStream,APP_CREATION_STREAM,APP_CREATION_STREAM_VERSION);

        }

        event.setTimeStamp(System.currentTimeMillis());
        event.setMetaData(null);
        event.setCorrelationData(null);
        event.setPayloadData(new Object[]{appName,appKey,timestamp,username,appType,repoType,appDescription,tenantId});
        try {
            publishEvents(event,APP_CREATION_STREAM,APP_CREATION_STREAM_VERSION);
        } catch (AgentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public void publishEvents(Event event,String Stream, String version) throws AgentException, InterruptedException {
        asyncDataPublisher.publish(Stream,version,event);
        asyncDataPublisher.stop();



//     if(publishDataQueue.size()<MAX_QUEUE_SIZE){
//
//             publishDataQueue.put(event);
//
//     } else if(publishDataQueue.size() == MAX_QUEUE_SIZE){
//        for(int i = 0; i<MAX_QUEUE_SIZE; i++){
//           asyncDataPublisher.publish(publishDataQueue.poll());
//        }
//     }

    }
}
