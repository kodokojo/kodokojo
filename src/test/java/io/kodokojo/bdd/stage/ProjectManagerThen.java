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
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.Stack;
import io.kodokojo.brick.BrickConfigurationStarter;
import io.kodokojo.brick.BrickStartContext;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ProjectManagerThen<SELF extends ProjectManagerThen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    BrickConfigurationStarter brickStarter;

    @ExpectedScenarioState
    ProjectConfiguration projectConfiguration;

    @ExpectedScenarioState
    Project project;

    public SELF brick_$_are_started(ExpectedProjectState projectState) {
        assertThat(project).isNotNull();
        assertThat(project.getName()).isEqualTo(projectConfiguration.getName());
        Set<Stack> stacks = project.getStacks();
        assertThat(stacks).isNotEmpty();
        assertThat(stacks.size()).isEqualTo(1);
        Stack stack = stacks.iterator().next();
        assertThat(stack).isNotNull();

        ArgumentCaptor<BrickStartContext> captor = ArgumentCaptor.forClass(BrickStartContext.class);
        verify(brickStarter, times(projectState.getBrickTypePresents().size())).start(captor.capture());
        List<BrickStartContext> brickStartContexts = captor.getAllValues();

        assertThat(brickStartContexts).extracting("brickConfiguration.type.name").contains(projectState.getBrickTypePresents().stream().map(Enum::name).collect(Collectors.toList()).toArray());

        return self();
    }
}
