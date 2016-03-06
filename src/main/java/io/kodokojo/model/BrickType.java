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

    SCM(true, StackType.BUILD),
    QA(false, StackType.BUILD),
    CI(true, StackType.BUILD),
    REPOSITORY(true, StackType.BUILD),
    MONITORING(false, StackType.RUN),
    ALTERTING(false, StackType.RUN),
    AUTHENTIFICATOR(false, StackType.RUN),
    LOADBALANCER(true, StackType.RUN);

    private final boolean requiered;

    private final StackType stackType;

    BrickType(boolean requiered, StackType stackType) {
        this.requiered = requiered;
        this.stackType = stackType;
    }

    public boolean isRequiered() {
        return requiered;
    }

    public StackType getStackType() {
        return stackType;
    }
}
