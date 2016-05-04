package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.model.*;
import io.kodokojo.service.DefaultProjectManager;
import io.kodokojo.service.ProjectAlreadyExistException;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.fail;

public class ProjectManagerWhen<SELF extends ProjectManagerWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    DefaultProjectManager projectManager;

    @ExpectedScenarioState
    String projectName;

    @ProvidedScenarioState
    ProjectConfiguration projectConfiguration;

    @ProvidedScenarioState
    Project project;

    public SELF i_start_project_with_the_project_configuration_$(@Quoted String configurationName) {
        KeyPair keyPair = null;
        try {
            keyPair = RSAUtils.generateRsaKeyPair();
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }

        User user = new User("12345", "Jean-Pascal THIERY", "jpthiery", "jpthiery@kodokojo.io", "jpthiery", RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), "jpthiery@kodokojo.io"));
        String stackName = "build-a";
        StackType stackType = StackType.BUILD;

        BootstrapStackData bootstrapStackData = projectManager.bootstrapStack(projectName, stackName, stackType);


        if ("Default".equals(configurationName)) {
            Set<StackConfiguration> stackConfigurations = new HashSet<>();
            Set<BrickConfiguration> brickConfigurations = new HashSet<>();
            brickConfigurations.add(new BrickConfiguration(new Brick("jenkins", BrickType.CI)));
            brickConfigurations.add(new BrickConfiguration(new Brick("gitlab", BrickType.SCM)));
            brickConfigurations.add(new BrickConfiguration(new Brick("nexus", BrickType.REPOSITORY)));
            StackConfiguration stackConfiguration = new StackConfiguration(stackName, stackType, brickConfigurations, bootstrapStackData.getLoadBalancerIp(), bootstrapStackData.getSshPort());
            stackConfigurations.add(stackConfiguration);
            List<User> users = Arrays.asList(user);
            projectConfiguration = new ProjectConfiguration("123456",configurationName, users, stackConfigurations, users);
        }


        try {
            project = projectManager.start(projectConfiguration);
        } catch (ProjectAlreadyExistException e) {
            fail(e.getMessage());
        }
        return self();
    }

}
