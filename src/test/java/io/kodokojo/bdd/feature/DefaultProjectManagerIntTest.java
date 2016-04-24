package io.kodokojo.bdd.feature;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.bdd.stage.ExpectedProjectState;
import io.kodokojo.bdd.stage.ProjectManagerGiven;
import io.kodokojo.bdd.stage.ProjectManagerThen;
import io.kodokojo.bdd.stage.ProjectManagerWhen;
import io.kodokojo.model.BrickType;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@RunWith(DataProviderRunner.class)
public class DefaultProjectManagerIntTest extends ScenarioTest<ProjectManagerGiven<?>, ProjectManagerWhen<?>, ProjectManagerThen<?>> {

    @DataProvider
    public static Object[][] projectConfigurationCases() {
        List<String> defaultStackName = Collections.singletonList("build-A");
        return new Object[][]{
                {"Acme", "Default", new ExpectedProjectState(defaultStackName, Arrays.asList(BrickType.CI, BrickType.SCM, BrickType.REPOSITORY))}
        };
    }

    @Test
    @UseDataProvider("projectConfigurationCases")
    public void start_a_default_stack(String projectName, String configurationName, ExpectedProjectState projectState) {
        given().i_bootstrap_project_$(projectName);
        when().i_start_project_with_the_project_configuration_$(configurationName);
        then().brick_$_are_started(projectState);
    }
}
