package io.kodokojo.model;

/*
 * #%L
 * kodokojo-commons
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Stack implements Serializable {

    public enum OrchestratorType {
        DOCKER,
        MARATHON
    }

    private final String name;

    private final OrchestratorType orchestratorType;

    private final StackType stackType;

    private final Set<BrickDeploymentState> brickEntities;

    public Stack(String name, StackType stackType, OrchestratorType orchestratorType, Set<BrickDeploymentState> brickEntities) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (orchestratorType == null) {
            throw new IllegalArgumentException("orchestratorType must be defined.");
        }
        if (stackType == null) {
            throw new IllegalArgumentException("stackType must be defined.");
        }
        if (CollectionUtils.isEmpty(brickEntities)) {
            throw new IllegalArgumentException("brickEntities must be defined.");
        }
        this.name = name;
        this.orchestratorType = orchestratorType;
        this.stackType = stackType;
        this.brickEntities = brickEntities;
    }

    public String getName() {
        return name;
    }

    public OrchestratorType getOrchestratorType() {
        return orchestratorType;
    }

    public StackType getStackType() {
        return stackType;
    }

    public Set<BrickDeploymentState> getBrickEntities() {
        return brickEntities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stack stack = (Stack) o;

        if (!name.equals(stack.name)) return false;
        if (orchestratorType != stack.orchestratorType) return false;
        if (stackType != stack.stackType) return false;
        return brickEntities.equals(stack.brickEntities);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + orchestratorType.hashCode();
        result = 31 * result + stackType.hashCode();
        result = 31 * result + brickEntities.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Stack{" +
                "name='" + name + '\'' +
                ", orchestratorType=" + orchestratorType +
                ", stackType=" + stackType +
                ", brickEntities=" + brickEntities +
                '}';
    }
}
