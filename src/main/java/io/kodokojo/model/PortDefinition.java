package io.kodokojo.model;

import java.io.Serializable;

public class PortDefinition implements Serializable {

    public static final int EXPOSED_SERVICE_PORT = 0;
    public static final int DYNAMIC_PORT = -1;

    public enum Type {
        HTTP,
        HTTPS,
        WS,
        WSS,
        SSH,
        TCP,
        UDP
    }

    private final Type type;

    private final int hostPort;

    private final int containerPort;

    private final int servicePort;

    public PortDefinition(Type type, int hostPort, int containerPort, int servicePort) {
        this.type = type;
        this.hostPort = hostPort;
        this.containerPort = containerPort;
        this.servicePort = servicePort;
    }

    public PortDefinition(Type type, int hostPort, int containerPort) {
        this(type, hostPort, containerPort, EXPOSED_SERVICE_PORT);
    }

    public PortDefinition(int containerPort) {
        this(Type.HTTP, DYNAMIC_PORT, containerPort, EXPOSED_SERVICE_PORT);
    }

    public Type getType() {
        return type;
    }

    public int getHostPort() {
        return hostPort;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public int getServicePort() {
        return servicePort;
    }

    @Override
    public String toString() {
        return "PortDefinition{" +
                "type=" + type +
                ", hostPort=" + hostPort +
                ", containerPort=" + containerPort +
                ", servicePort=" + servicePort +
                '}';
    }
}
