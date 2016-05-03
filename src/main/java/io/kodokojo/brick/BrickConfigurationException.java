package io.kodokojo.brick;

public class BrickConfigurationException extends Exception {

    public BrickConfigurationException() {
    }

    public BrickConfigurationException(String message) {
        super(message);
    }

    public BrickConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrickConfigurationException(Throwable cause) {
        super(cause);
    }

    public BrickConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
