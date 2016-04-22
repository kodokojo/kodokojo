package io.kodokojo.service;

/**
 * Allow to provide informations required to start a Project stack and her Bricks.
 */
public interface BootstrapConfigurationProvider {

    /**
     * Provide the load balancer IP which may used to access to the project stack.
     * @param projectName Name of the project
     * @param stackName The stack name
     * @return Ip of the load balancer
     */
    String provideLoadBalancerIp(String projectName, String stackName);

    /**
     * Provide SSH port configured on the load balancer to access to the stack.
     * @param projectName Name of the project
     * @param stackName The stack name
     * @return SSH port to access to the project stack
     */
    int provideSshPortEntrypoint(String projectName, String stackName);

}
