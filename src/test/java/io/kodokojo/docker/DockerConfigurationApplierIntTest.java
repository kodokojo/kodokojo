package io.kodokojo.docker;

/*
 * #%L
 * project-manager
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

import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.config.DockerConfig;
import io.kodokojo.commons.model.*;
import io.kodokojo.commons.utils.docker.DockerSupport;
import io.kodokojo.commons.utils.properties.PropertyResolver;
import io.kodokojo.commons.utils.properties.provider.DockerConfigValueProvider;
import io.kodokojo.commons.utils.properties.provider.SystemEnvValueProvider;
import io.kodokojo.model.*;
import io.kodokojo.project.jenkins.JenkinsConfigurer;
import io.kodokojo.project.starter.ConfigurerData;
import io.kodokojo.service.DefaultBrickFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore
public class DockerConfigurationApplierIntTest {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    private DockerConfig dockerConfig;

    @Before
    public void setup() {
        PropertyResolver resolver = new PropertyResolver(new DockerConfigValueProvider(new SystemEnvValueProvider()));
        dockerConfig = resolver.createProxy(DockerConfig.class);
    }

    @Test
    @DockerIsRequire
    public void launch_jenkins() {
        Map<String, BrickDockerCommandFactory> commands = new HashMap<>();

        commands.put("jenkins", new JenkinsCommandFactory());

        DockerConfigurationApplier launcher = new DockerConfigurationApplier(new DockerSupport(dockerConfig), commands);

        BrickDeploymentState jenkins = launcher.apply(new BrickConfiguration(new DefaultBrickFactory(null).createBrick(DefaultBrickFactory.JENKINS)));
        JenkinsConfigurer configurer = new JenkinsConfigurer();
        Service service = jenkins.getServices().get(0);
        String entrypoint = "http://" + service.getHost() + ":" + service.getPort();

        List<User> users = new ArrayList<>();
        User user = new User("jpthiery","Jean-Pascal THIERY", "jpthiery", "jpthiery@xebia.fr", "jpascal", "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJlKssV2knMeGglt31NWQVznhlzgCJtblmiN/QG5y+X4cJ9pDXzpRJ13L88ay7rGL5SC6UwjJTQB4y1RI0Jte4naYRprDHfOUbMfi3KEiRY0I9LCDjThmTJNK6KhK5iv1ybcEKQd65wu5lGFpPG+TQfxocIXIPe9y0ZPSCxOHPjP9c6Akq6ryBNo4tANPVXEB37K9c2/2l/y7hfLqzkUf36V9el4ptgtYwrhR3Jbx4Q4uadLOK/KO4Kh8HAWsFmTLNv4c9DurLZlNNn9iw6/pBR2C5agM+cPjQ8NDJhMAktcQo5pjeYFS91EXrb/0EsDUn1mUdFPQH8GiENdz5WVgf jpthiery@xebia.fr");
        User adminUser = user;
        users.add(user);
        user = new User("aletaxin","Antoine LE TAXIN", "altaxin", "ataxin@xebia.fr", "antoine", "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJlKssV2knMeGglt31NWQVznhlzgCJtblmiN/QG5y+X4cJ9pDXzpRJ13L88ay7rGL5SC6UwjJTQB4y1RI0Jte4naYRprDHfOUbMfi3KEiRY0I9LCDjThmTJNK6KhK5iv1ybcEKQd65wu5lGFpPG+TQfxocIXIPe9y0ZPSCxOHPjP9c6Akq6ryBNo4tANPVXEB37K9c2/2l/y7hfLqzkUf36V9el4ptgtYwrhR3Jbx4Q4uadLOK/KO4Kh8HAWsFmTLNv4c9DurLZlNNn9iw6/pBR2C5agM+cPjQ8NDJhMAktcQo5pjeYFS91EXrb/0EsDUn1mUdFPQH8GiENdz5WVgf ataxin@xebia.fr");
        users.add(user);
        ConfigurerData configurerData = new ConfigurerData("acme", entrypoint, "kodokojo.dev", adminUser, users);
        configurer.configure(configurerData);

        //BrickDeploymentState jenkins = launcher.apply(Brick.JENKINS);
        //System.out.println(jenkins);
    }

    @Test
    @DockerIsRequire
    public void launch_gitlab() {
        Map<String, BrickDockerCommandFactory> commands = new HashMap<>();

        commands.put("gitlab", new GitlabCommandFactory());
        /*
        DockerConfigurationApplier launcher = new DockerConfigurationApplier(new DockerSupport(dockerConfig), commands);
        BrickDeploymentState gitlab = launcher.apply(Brick.GITLAB);

        Service service = gitlab.getServices().get(0);
        GitlabConfigurer configurer = new GitlabConfigurer(new PropertyValueProvider() {
            @Override
            public <T> T providePropertyValue(Class<T> classType, String key) {
                return (T) "admin1234";
            }
        });

        String privateToken = configurer.configure("http://" + service.getHost() + ":" + service.getPort());

        System.out.println(gitlab);
        System.out.println("Private token :" + privateToken);
        */
    }

}