package io.kodokojo.bdd.feature;

import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.bdd.MarathonIsPresent;
import io.kodokojo.bdd.MarathonIsRequire;
import io.kodokojo.bdd.stage.cluster.ClusterApplicationGiven;
import io.kodokojo.bdd.stage.cluster.ClusterApplicationThen;
import io.kodokojo.bdd.stage.cluster.ClusterApplicationWhen;
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
    @MarathonIsRequire
    public void create_a_simple_project_build_stack() {
        given().kodokojo_is_running(marathonIsPresent)
                .and().i_am_user_$("jpthiery");
        when().i_start_a_default_project_with_name_$("Acme");
        then().i_have_a_valid_repository();
    }

}
