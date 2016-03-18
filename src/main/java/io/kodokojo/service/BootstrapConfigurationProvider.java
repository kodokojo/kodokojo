package io.kodokojo.service;

public interface BootstrapConfigurationProvider {

    String provideLoadBalancerIp(String projectName, String name);

    int provideSshPortEntrypoint(String projectName, String name);

}
