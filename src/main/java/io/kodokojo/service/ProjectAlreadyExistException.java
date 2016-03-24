package io.kodokojo.service;

public class ProjectAlreadyExistException extends Exception {

    private final String projectName;

    public ProjectAlreadyExistException(String projectName) {
        this.projectName = projectName;
    }

    public ProjectAlreadyExistException(String message, String projectName) {
        super(message);
        this.projectName = projectName;
    }

    public ProjectAlreadyExistException(String message, Throwable cause, String projectName) {
        super(message, cause);
        this.projectName = projectName;
    }

    public ProjectAlreadyExistException(Throwable cause, String projectName) {
        super(cause);
        this.projectName = projectName;
    }

    public ProjectAlreadyExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String projectName) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }
}
