package io.kodokojo.service;

import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.model.Brick;
import io.kodokojo.model.BrickType;
import io.kodokojo.project.gitlab.GitlabConfigurer;
import io.kodokojo.project.jenkins.JenkinsConfigurer;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DefaultBrickFactory implements BrickFactory {



    public static final String JENKINS = "jenkins";

    public static final String GITLAB = "gitlab";

    private final Map<String, Brick> cache ;

    @Inject
    public DefaultBrickFactory(PropertyValueProvider propertyValueProvider) {
        /*
        if (propertyValueProvider == null) {
            throw new IllegalArgumentException("propertyValueProvider must be defined.");
        }
        */
        cache = new HashMap<>();
        cache.put(JENKINS, new Brick(JENKINS, BrickType.CI, new JenkinsConfigurer()));
        cache.put(GITLAB, new Brick(GITLAB, BrickType.SCM, new GitlabConfigurer()));
    }

    @Override
    public Brick createBrick(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        return cache.get(name);
    }
    /*
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
*/
}
