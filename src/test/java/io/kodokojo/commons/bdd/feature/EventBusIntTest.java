package io.kodokojo.commons.bdd.feature;

import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.bdd.stage.EventBusGiven;
import io.kodokojo.commons.bdd.stage.EventBusThen;
import io.kodokojo.commons.bdd.stage.EventBusWhen;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class EventBusIntTest extends ScenarioTest<EventBusGiven<?>, EventBusWhen<?>, EventBusThen<?>> {

    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @Test
    @DockerIsRequire
    public void service_connect_successfully_to_event_bus() throws IOException, TimeoutException {
        given().a_new_event_bus_is_available(dockerPresentMethodRule.getDockerTestSupport())
                .and().it_exit_a_fake_service();
        when().service_had_started();
        then().event_bus_receive_the_hello_event_of_given_service();
    }


}
