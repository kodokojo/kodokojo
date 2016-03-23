package io.kodokojo.bdd.feature;

import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.bdd.MarathonIsPresent;
import io.kodokojo.bdd.MarathonIsRequire;
import io.kodokojo.bdd.stage.ClusterApplicationGiven;
import io.kodokojo.bdd.stage.ClusterApplicationThen;
import io.kodokojo.bdd.stage.ClusterApplicationWhen;
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
    public void create_a_simple_project_build_stack() {
        given().kodokojo_is_running(marathonIsPresent)
                .and().i_am_user_$("jpthiery");
        when().i_create_a_default_project("Acme")
                .and().i_start_the_project();
        then().i_have_a_valid_scm()
                .and().i_have_a_valid_ci();
    }

}
