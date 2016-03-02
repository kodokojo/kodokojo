package io.kodokojo.commons.model;

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

import static org.apache.commons.lang.StringUtils.isBlank;

public enum Brick {

    JENKINS("jenkins", BrickType.CI),
    GITLAB("gitlab", BrickType.SCM),
    SONAR("sonar", BrickType.QA),
    DOCKER_REGISTRY("registry", BrickType.REPOSITORY),
    NEXUS("nexus", BrickType.REPOSITORY),
    LDAP("ldap", BrickType.AUTHENTIFICATOR),
    OAUTH("oauth", BrickType.AUTHENTIFICATOR),
    FLAPJACK("flapjack", BrickType.ALTERTING),
    SENSU("sensu", BrickType.MONITORING),
    HAPROXY("haproxy", BrickType.LOADBALANCER);

    private final String name;

    private final BrickType type;

    Brick(String name, BrickType type) {
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

    public BrickType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Brick{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
