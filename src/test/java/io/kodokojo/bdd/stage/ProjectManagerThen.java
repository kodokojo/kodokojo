package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.Stack;
import io.kodokojo.project.starter.BrickManager;
import io.kodokojo.service.BrickAlreadyExist;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class ProjectManagerThen <SELF extends ProjectManagerThen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    BrickManager brickManager;

    @ExpectedScenarioState
    ProjectConfiguration projectConfiguration;

    @ExpectedScenarioState
    Project project;

    public SELF brick_$_are_started(ExpectedProjectState projectState) {
        assertThat(project).isNotNull();
        assertThat(project.getSslRootCaKey()).isNotNull();
        assertThat(project.getName()).isEqualTo(projectConfiguration.getName());
        Set<Stack> stacks = project.getStacks();
        assertThat(stacks).isNotEmpty();
        assertThat(stacks.size()).isEqualTo(1);
        Stack stack = stacks.iterator().next();
        assertThat(stack).isNotNull();

        projectState.getBrickTypePresents().forEach(b -> {
            try {
                verify(brickManager).start(any(), eq(b));
                verify(brickManager).configure(any(), eq(b));
            } catch (BrickAlreadyExist e) {
                fail(e.getMessage());
            }
        });
        return self();
    }
}
