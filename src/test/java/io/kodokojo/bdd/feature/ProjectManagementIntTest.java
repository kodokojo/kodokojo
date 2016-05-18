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

import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.bdd.MarathonIsPresent;
import io.kodokojo.bdd.MarathonIsRequire;
import io.kodokojo.bdd.stage.cluster.ClusterApplicationGiven;
import io.kodokojo.bdd.stage.cluster.ClusterApplicationThen;
import io.kodokojo.bdd.stage.cluster.ClusterApplicationWhen;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Ignore
public class ProjectManagementIntTest extends ScenarioTest<ClusterApplicationGiven<?>, ClusterApplicationWhen<?>, ClusterApplicationThen<?>> {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @Rule
    public MarathonIsPresent marathonIsPresent = new MarathonIsPresent();

    @Test
    @DockerIsRequire
    public void create_a_project_with_jenkins_and_add_a_user() {
        given().kodokojo_is_running(dockerPresentMethodRule)
                .and().i_am_user_$("jpthiery");
        when().i_configure_a_project_with_name_$_and_only_brick_$("Acme", "jenkins")
        .and().i_start_the_project()
        .and().i_create_a_new_user_$("aletaxin@xebia.fr")
        .and().i_add_the_user_$_to_the_project("aletaxin");
        then().it_possible_to_log_on_brick_$_with_user_$("jenkins", "aletaxin");
    }

    @Test
    @Ignore
    @MarathonIsRequire
    public void create_a_simple_project_build_stack() {
        given().kodokojo_is_running(marathonIsPresent)
                .and().i_am_user_$("jpthiery");
        when().i_start_a_default_project_with_name_$("Acme")
        .and().i_start_the_project();
        then().i_have_a_valid_repository()
                .and().i_have_a_valid_scm()
                .and().i_have_a_valid_ci();
    }

}
