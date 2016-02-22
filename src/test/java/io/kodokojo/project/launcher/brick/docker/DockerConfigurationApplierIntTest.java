package io.kodokojo.project.launcher.brick.docker;

import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.config.DockerConfig;
import io.kodokojo.commons.project.model.Brick;
import io.kodokojo.commons.project.model.BrickEntity;
import io.kodokojo.commons.project.model.BrickType;
import io.kodokojo.commons.project.model.Service;
import io.kodokojo.commons.utils.docker.DockerSupport;
import io.kodokojo.commons.utils.properties.PropertyResolver;
import io.kodokojo.commons.utils.properties.provider.DockerConfigValueProvider;
import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.commons.utils.properties.provider.SystemEnvValueProvider;
import io.kodokojo.project.gitlab.GitlabConfigurer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        //BrickEntity jenkins = launcher.apply(Brick.JENKINS);
        //System.out.println(jenkins);
    }

    @Test
    @DockerIsRequire
    public void launch_gitlab() {
        Map<String, BrickDockerCommandFactory> commands = new HashMap<>();

        commands.put("gitlab", new GitlabCommandFactory());
        /*
        DockerConfigurationApplier launcher = new DockerConfigurationApplier(new DockerSupport(dockerConfig), commands);
        BrickEntity gitlab = launcher.apply(Brick.GITLAB);

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