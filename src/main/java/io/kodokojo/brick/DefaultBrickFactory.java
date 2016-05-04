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
