package org.wso2.carbon.appfactory.bam.integration;

/**
 * Created with IntelliJ IDEA.
 * User: gayan
 * Date: 7/18/13
 * Time: 2:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class BamDataPublisherAgent {

    public static void main(String[] args) {
        BamDataPublisher dataPublisher = new BamDataPublisher();
        dataPublisher.PublishAppCreationEvent("app1", "app1", "test", "war", "git", System.currentTimeMillis(), "wso2.com", "admin");

    }
}
