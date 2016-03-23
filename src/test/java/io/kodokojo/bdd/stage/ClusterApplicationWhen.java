package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.model.*;
import io.kodokojo.service.BrickFactory;
import io.kodokojo.service.DefaultBrickFactory;
import io.kodokojo.service.ProjectManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class ClusterApplicationWhen<SELF extends ClusterApplicationWhen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    User currentUser;

    @ExpectedScenarioState
    ProjectManager projectManager;

    @ProvidedScenarioState
    ProjectConfiguration projectConfiguration;

    @ProvidedScenarioState
    String loadBalancerIp;

    public SELF i_create_a_default_project(String projectName) {

        BrickFactory brickFactory = new DefaultBrickFactory(null);
        Set<StackConfiguration> stackConfigurations = new HashSet<>();
        Set<BrickConfiguration> brickConfigurations = new HashSet<>();

        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.JENKINS)));
        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.GITLAB)));
        brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.HAPROXY), false));

        loadBalancerIp = "52.19.37.28";    //Ha proxy may be reloadable in a short future.
        StackConfiguration stackConfiguration = new StackConfiguration("build-A", StackType.BUILD, brickConfigurations, loadBalancerIp, 10022);
        stackConfigurations.add(stackConfiguration);

        this.projectConfiguration = new ProjectConfiguration(projectName, currentUser.getEmail(), stackConfigurations, Collections.singletonList(currentUser));

        return self();
    }

    public SELF i_start_the_project() {
        projectManager.start(projectConfiguration);
        return self();
    }


}
