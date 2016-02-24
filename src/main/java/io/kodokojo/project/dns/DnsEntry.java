package io.kodokojo.project.dns;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DnsEntry {

    public enum Type {
        A,
        AAAA,
        CNAME,
        SRV,
        NS,
        MX,
        SOA,
        TXT,
        PTR,
        SPDF
    }

    private final String name;

    private final Type type;

    private final String value;

    public DnsEntry(String name, Type type, String value) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must be defined.");
        }
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DnsEntry{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
