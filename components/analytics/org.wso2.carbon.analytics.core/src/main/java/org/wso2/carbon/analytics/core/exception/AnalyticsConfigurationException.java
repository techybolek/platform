package org.wso2.carbon.analytics.core.exception;

public class AnalyticsConfigurationException extends Exception {
    private String errorMessage;

    public AnalyticsConfigurationException() {
    }

    public AnalyticsConfigurationException(String message) {
        super(message);
        errorMessage = message;
    }

    public AnalyticsConfigurationException(String message, Throwable cause) {
        super(message, cause);
        errorMessage = message;
    }

    public AnalyticsConfigurationException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
