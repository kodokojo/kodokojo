package io.kodokojo.service;

import io.kodokojo.model.Brick;
import io.kodokojo.project.gitlab.GitlabConfigurer;
import io.kodokojo.project.jenkins.JenkinsConfigurer;
import io.kodokojo.project.nexus.NexusConfigurer;
import io.kodokojo.project.starter.BrickConfigurer;

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
