package io.kodokojo.bdd.feature;

import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.bdd.stage.BrickStateNotificationGiven;
import io.kodokojo.bdd.stage.BrickStateNotificationThen;
import io.kodokojo.bdd.stage.BrickStateNotificationWhen;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import org.junit.Rule;
import org.junit.Test;

public class BrickStateNotificationIntTest extends ScenarioTest<BrickStateNotificationGiven<?>, BrickStateNotificationWhen<?>, BrickStateNotificationThen<?>> {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @Test
    @DockerIsRequire
    public void user_receive_all_notification_when_start_a_project() {
        given().kodokojo_is_started()
        .and().i_am_user_$("jpthiery");
        when().i_create_a_project_configuration_with_default_brick();
        then().i_receive_all_notification();
    }

}
