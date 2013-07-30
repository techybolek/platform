package org.wso2.carbon.appfactory.bam.integration;

import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: gayan
 * Date: 7/17/13
 * Time: 12:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class BamDataPublisher {


    private String APP_CREATION_STREAM = "org.wso2.carbon.appfactory.appCreation" ;
    private String APP_CREATION_STREAM_VERSION = "1.0.0";
    private LinkedBlockingQueue<Event> publishDataQueue;
    private int MAX_QUEUE_SIZE = 10;


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
        System.out.println(appName);

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
