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
