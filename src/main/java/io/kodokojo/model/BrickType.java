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

public enum BrickType {

    SCM(true, StackType.BUILD, true),
    QA(false, StackType.BUILD, true),
    CI(true, StackType.BUILD, true),
    REPOSITORY(true, StackType.BUILD, true),
    MONITORING(false, StackType.RUN, true),
    ALTERTING(false, StackType.RUN, true),
    AUTHENTIFICATOR(false, StackType.RUN, false),
    LOADBALANCER(true, StackType.RUN, false);

    private final boolean requiered;

    private final StackType stackType;

    private final boolean requiredHttpExposed;

    BrickType(boolean requiered, StackType stackType, boolean requiredHttpExposed) {
        this.requiered = requiered;
        this.stackType = stackType;
        this.requiredHttpExposed = requiredHttpExposed;
    }


    public boolean isRequiredHttpExposed() {
        return requiredHttpExposed;
    }

    public boolean isRequiered() {
        return requiered;
    }

    public StackType getStackType() {
        return stackType;
    }
}
