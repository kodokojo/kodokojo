package io.kodokojo.brick;

import io.kodokojo.model.Brick;
import io.kodokojo.brick.gitlab.GitlabConfigurer;
import io.kodokojo.brick.jenkins.JenkinsConfigurer;
import io.kodokojo.brick.nexus.NexusConfigurer;

public class DefaultBrickConfigurerProvider implements BrickConfigurerProvider {

    @Override
    public BrickConfigurer provideFromBrick(Brick brick) {
        if (brick == null) {
            throw new IllegalArgumentException("brick must be defined.");
        }
        switch (brick.getName()) {
            case DefaultBrickFactory.GITLAB:
                return new GitlabConfigurer();
            case DefaultBrickFactory.JENKINS:
                return new JenkinsConfigurer();
            case DefaultBrickFactory.NEXUS:
                return new NexusConfigurer();
            default:
                return null;
        }
    }
}
