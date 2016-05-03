package io.kodokojo.service;

public class ProjectConfigurationException extends Exception {

    public ProjectConfigurationException() {
    }

    public ProjectConfigurationException(String message) {
        super(message);
    }

    public ProjectConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectConfigurationException(Throwable cause) {
        super(cause);
    }

    public ProjectConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
