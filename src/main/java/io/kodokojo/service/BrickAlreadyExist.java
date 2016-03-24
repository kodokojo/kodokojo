package io.kodokojo.service;

public class BrickAlreadyExist extends Exception {

    private final String brickName;

    private final String projectName;

    public BrickAlreadyExist(String brickName, String projectName) {
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public BrickAlreadyExist(String message, String brickName, String projectName) {
        super(message);
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public BrickAlreadyExist(String message, Throwable cause, String brickName, String projectName) {
        super(message, cause);
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public BrickAlreadyExist(Throwable cause, String brickName, String projectName) {
        super(cause);
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public BrickAlreadyExist(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String brickName, String projectName) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public String getBrickName() {
        return brickName;
    }

    public String getProjectName() {
        return projectName;
    }
}
