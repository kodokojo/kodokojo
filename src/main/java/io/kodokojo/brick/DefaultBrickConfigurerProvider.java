package io.kodokojo.brick;

import io.kodokojo.brick.dockerregistry.DockerRegistryConfigurer;
import io.kodokojo.model.Brick;
import io.kodokojo.brick.gitlab.GitlabConfigurer;
import io.kodokojo.brick.jenkins.JenkinsConfigurer;
import io.kodokojo.brick.nexus.NexusConfigurer;

import javax.inject.Inject;

public class DefaultBrickConfigurerProvider implements BrickConfigurerProvider {

    private final BrickUrlFactory brickUrlFactory;

    @Inject
    public DefaultBrickConfigurerProvider(BrickUrlFactory brickUrlFactory) {
        if (brickUrlFactory == null) {
            throw new IllegalArgumentException("brickUrlFactory must be defined.");
        }
        this.brickUrlFactory = brickUrlFactory;
    }

    @Override
    public BrickConfigurer provideFromBrick(Brick brick) {
        if (brick == null) {
            throw new IllegalArgumentException("brick must be defined.");
        }
        switch (brick.getName()) {
            case DefaultBrickFactory.GITLAB:
                return new GitlabConfigurer(brickUrlFactory);
            case DefaultBrickFactory.JENKINS:
                return new JenkinsConfigurer();
            case DefaultBrickFactory.NEXUS:
                return new NexusConfigurer();
            case DefaultBrickFactory.DOCKER_REGISTRY:
                return new DockerRegistryConfigurer();
            default:
                return null;
        }
    }
}
