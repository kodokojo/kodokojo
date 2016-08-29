/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.model;


import io.kodokojo.service.actor.message.BrickStateEvent;

import java.io.Serializable;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Stack implements Serializable {

    private final String name;

    private final StackType stackType;

    private final Set<BrickStateEvent> brickStateEvents;

    public Stack(String name, StackType stackType, Set<BrickStateEvent> brickStateEvents) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (stackType == null) {
            throw new IllegalArgumentException("stackType must be defined.");
        }
        this.name = name;
        this.stackType = stackType;
        this.brickStateEvents = brickStateEvents;
    }

    public String getName() {
        return name;
    }


    public StackType getStackType() {
        return stackType;
    }

    public Set<BrickStateEvent> getBrickStateEvents() {
        return brickStateEvents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stack stack = (Stack) o;

        if (!name.equals(stack.name)) return false;
        if (stackType != stack.stackType) return false;
        return brickStateEvents.equals(stack.brickStateEvents);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + stackType.hashCode();
        result = 31 * result + brickStateEvents.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Stack{" +
                "name='" + name + '\'' +
                ", stackType=" + stackType +
                ", brickStateEvents=" + brickStateEvents +
                '}';
    }
}
