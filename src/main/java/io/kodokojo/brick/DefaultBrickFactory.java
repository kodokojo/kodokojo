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

import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.BrickType;
import io.kodokojo.model.PortDefinition;

import javax.inject.Inject;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DefaultBrickFactory implements BrickFactory {

    public static final String JENKINS = "jenkins";

    public static final String GITLAB = "gitlab";

    public static final String HAPROXY = "haproxy";

    public static final String NEXUS = "nexus";

    public static final String DOCKER_REGISTRY = "dockerregistry";

    private final Map<String, BrickConfiguration> cache ;

    @Inject
    public DefaultBrickFactory() {
        cache = new HashMap<>();
        cache.put(JENKINS, new BrickConfiguration(JENKINS, BrickType.CI, "1.651.3", Collections.singleton(new PortDefinition(8080))));
        Set<PortDefinition> gitlbaPorts  = new HashSet<>();
        gitlbaPorts.add(new PortDefinition(PortDefinition.Type.HTTP, -1, 80, -1));
        gitlbaPorts.add(new PortDefinition(PortDefinition.Type.SSH, -1, 22, -1));
        cache.put(GITLAB, new BrickConfiguration(GITLAB, BrickType.SCM, "8.12.0-ce.0", gitlbaPorts));
        cache.put(HAPROXY, new BrickConfiguration(HAPROXY, BrickType.LOADBALANCER, "1.6",  Collections.singleton(new PortDefinition(80))));
        cache.put(NEXUS, new BrickConfiguration(NEXUS, BrickType.REPOSITORY, "2.13",  Collections.singleton(new PortDefinition(80))));
     //   cache.put(DOCKER_REGISTRY, new BrickConfiguration(DOCKER_REGISTRY,  Collections.singleton(new PortDefinition(80))))
    }

    @Override
    public BrickConfiguration createBrick(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return cache.get(name);
    }

    @Override
    public List<BrickConfiguration> listBrickAvailable() {
        return new ArrayList<>(cache.values());
    }
}
