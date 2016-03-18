package io.kodokojo.model;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BootstrapStackData {

    private final String projectName;

    private final String stackName;

    private final String loadBalancerIp;

    private final int sshPort;

    public BootstrapStackData(String projectName, String stackName, String loadBalancerIp, int sshPort) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        if (isBlank(loadBalancerIp)) {
            throw new IllegalArgumentException("loadBalancerIp must be defined.");
        }
        this.projectName = projectName;
        this.stackName = stackName;
        this.loadBalancerIp = loadBalancerIp;
        this.sshPort = sshPort;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getStackName() {
        return stackName;
    }

    public String getLoadBalancerIp() {
        return loadBalancerIp;
    }

    public int getSshPort() {
        return sshPort;
    }

    @Override
    public String toString() {
        return "BootstrapStackData{" +
                "projectName='" + projectName + '\'' +
                ", stackName='" + stackName + '\'' +
                ", loadBalancerIp='" + loadBalancerIp + '\'' +
                ", sshPort=" + sshPort +
                '}';
    }
}
