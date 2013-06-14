package org.wso2.carbon.bam.webapp.stat.publisher;

/**
 * User: kasun
 * Date: 5/2/13
 */
public class WebappPublisherException extends Exception {
    private static final long serialVersionUID = 3048028946241207694L;

    public WebappPublisherException() {
    }

    public WebappPublisherException(String message) {
        super(message);
    }

    public WebappPublisherException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebappPublisherException(Throwable cause) {
        super(cause);
    }

}
