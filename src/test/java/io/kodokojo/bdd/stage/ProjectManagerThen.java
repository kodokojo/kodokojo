package io.kodokojo.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import io.kodokojo.project.starter.BrickManager;
import org.mockito.Mockito;

public class ProjectManagerThen <SELF extends ProjectManagerThen<?>> extends Stage<SELF> {

    @ExpectedScenarioState
    BrickManager brickManager;

    public SELF brick_$_are_started(ExpectedProjectState projectState) {
        projectState.getBrickNamePresents().forEach(b -> {
            
        });
        return self();
    }
}
