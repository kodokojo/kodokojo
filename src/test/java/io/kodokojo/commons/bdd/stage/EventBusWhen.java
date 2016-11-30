package io.kodokojo.commons.bdd.stage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.commons.event.JsonToEventConverter;
import io.kodokojo.commons.rabbitmq.RabbitMqConnectionFactory;
import io.kodokojo.commons.rabbitmq.RabbitMqEventBus;
import io.kodokojo.commons.utils.DockerTestApplicationBuilder;

public class EventBusWhen<SELF extends EventBusWhen<?>> extends Stage<SELF> implements DockerTestApplicationBuilder, JsonToEventConverter, RabbitMqConnectionFactory {

    @ProvidedScenarioState
    RabbitMqEventBus eventBus;

    public SELF service_had_started() {
        eventBus.connect();
        return self();
    }
}