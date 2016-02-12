package io.kodokojo.project.model;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Stack {

    enum Type {
        BUILD,
        RUN
    }

    private final String name;

    private final Type type;

    private final Set<Brick> bricks;

    public Stack(String name, Type type, Set<Brick> bricks) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must be defined.");
        }
        if (bricks == null) {
            throw new IllegalArgumentException("bricks must be defined.");
        }

        this.name = name;
        this.type = type;
        this.bricks = bricks;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Set<Brick> getBricks() {
        return new HashSet<>(bricks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stack stack = (Stack) o;

        if (!name.equals(stack.name)) return false;
        if (type != stack.type) return false;
        return bricks.equals(stack.bricks);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + bricks.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Stack{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", bricks=" + bricks +
                '}';
    }
}
