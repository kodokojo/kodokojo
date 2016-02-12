package io.kodokojo.project.model;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Brick {

    public enum Type {
        SCM,
        QA,
        CI,
        REPOSITORY,
        MONITORING,
        ALTERTING,
        AUTHENTIFICATOR,
        LOADBALANCER
    }

    private final String name;

    private final Type type;

    public Brick(String name, Type type) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must be defined.");
        }
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Brick brick = (Brick) o;

        if (!name.equals(brick.name)) return false;
        return type == brick.type;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Brick{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
