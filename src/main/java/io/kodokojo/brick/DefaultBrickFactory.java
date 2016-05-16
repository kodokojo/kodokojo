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
package io.kodokojo.brick;

import io.kodokojo.brick.BrickFactory;
import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.model.Brick;
import io.kodokojo.model.BrickType;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DefaultBrickFactory implements BrickFactory {

    public static final String JENKINS = "jenkins";

    public static final String GITLAB = "gitlab";

    public static final String HAPROXY = "haproxy";

    public static final String NEXUS = "nexus";

    public static final String DOCKER_REGISTRY = "dockerregistry";

    private final Map<String, Brick> cache ;

    @Inject
    public DefaultBrickFactory() {
        cache = new HashMap<>();
        cache.put(JENKINS, new Brick(JENKINS, BrickType.CI));
        cache.put(GITLAB, new Brick(GITLAB, BrickType.SCM));
        cache.put(HAPROXY, new Brick(HAPROXY, BrickType.LOADBALANCER));
        cache.put(NEXUS, new Brick(NEXUS, BrickType.REPOSITORY));
        cache.put(DOCKER_REGISTRY, new Brick(DOCKER_REGISTRY, BrickType.REPOSITORY));
    }

    @Override
    public Brick createBrick(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return cache.get(name);
    }
}
