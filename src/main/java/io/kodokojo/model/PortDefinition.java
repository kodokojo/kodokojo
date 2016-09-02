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

    private final String name;

    public PortDefinition(String name, Type type, int hostPort, int containerPort, int servicePort) {
        this.name = name;
        if (type == null) {
            this.type = Type.HTTP;
        } else {

            this.type = type;
        }
        this.hostPort = hostPort;
        this.containerPort = containerPort;
        this.servicePort = servicePort;
    }

    public PortDefinition(Type type, int hostPort, int containerPort, int servicePort) {
        this(null, type, hostPort, containerPort, servicePort);
    }

    public PortDefinition(Type type, int hostPort, int containerPort) {
        this(null, type, hostPort, containerPort, EXPOSED_SERVICE_PORT);
    }

    public PortDefinition(int containerPort) {
        this(null, Type.HTTP, DYNAMIC_PORT, containerPort, EXPOSED_SERVICE_PORT);
    }

    public String getName() {
        return name;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PortDefinition that = (PortDefinition) o;

        if (containerPort != that.containerPort) return false;
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + containerPort;
        return result;
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
