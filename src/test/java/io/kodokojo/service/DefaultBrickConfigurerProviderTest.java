package io.kodokojo.service;

import io.kodokojo.brick.*;
import io.kodokojo.model.Brick;
import io.kodokojo.model.BrickType;
import io.kodokojo.brick.gitlab.GitlabConfigurer;
import io.kodokojo.brick.jenkins.JenkinsConfigurer;
import io.kodokojo.brick.nexus.NexusConfigurer;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultBrickConfigurerProviderTest {

    private BrickFactory brickFactory;

    private BrickConfigurerProvider brickConfigurerProvider;

    @Before
    public void setup() {
        brickFactory = new DefaultBrickFactory(null);
        brickConfigurerProvider = new DefaultBrickConfigurerProvider(new DefaultBrickUrlFactory("kodokojo.dev"));
    }

    @Test
    public void unexpected_brick_type() {
        BrickConfigurer unknow = brickConfigurerProvider.provideFromBrick(new Brick("unknow", BrickType.ALTERTING));
        assertThat(unknow).isNull();
    }

    @Test
    public void get_jenkins_brick_configurer() {
        tests("jenkins", JenkinsConfigurer.class);
    }

    @Test
    public void get_gitlab_brick_configurer() {
        tests("gitlab", GitlabConfigurer.class);
    }

    @Test
    public void get_nexus_brick_configurer() {
        tests("nexus", NexusConfigurer.class);
    }

    private void tests(String brickName, Class expectedClass) {
        BrickConfigurer brickConfigurer = brickConfigurerProvider.provideFromBrick(brickFactory.createBrick(brickName));
        assertThat(brickConfigurer).isInstanceOf(expectedClass);
    }

}