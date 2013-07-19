package org.wso2.carbon.analytics.hive.exception;

public class AnnotationConfigException extends Exception {
    private String errorMessage;

    public AnnotationConfigException() {
    }

    public AnnotationConfigException(String message) {
        super(message);
        errorMessage = message;
    }

    public AnnotationConfigException(String message, Throwable cause) {
        super(message, cause);
        errorMessage = message;
    }

    public AnnotationConfigException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
