/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.brick.DefaultBrickFactory;
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
            BrickFactory brickFactory = new DefaultBrickFactory();
            brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick("jenkins")));
            brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick("gitlab")));
            brickConfigurations.add(new BrickConfiguration(brickFactory.createBrick("nexus")));
            StackConfiguration stackConfiguration = new StackConfiguration(stackName, stackType, brickConfigurations, bootstrapStackData.getLoadBalancerHost(), bootstrapStackData.getSshPort());
            stackConfigurations.add(stackConfiguration);
            List<User> users = Arrays.asList(user);
            projectConfiguration = new ProjectConfiguration("123456", "7890",configurationName, users, stackConfigurations, users);
        }


        try {
            project = projectManager.start(projectConfiguration);
        } catch (ProjectAlreadyExistException e) {
            fail(e.getMessage());
        }
        return self();
    }

}
