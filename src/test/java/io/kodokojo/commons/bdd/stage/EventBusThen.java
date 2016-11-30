package io.kodokojo.commons.bdd.stage;

import com.rabbitmq.client.Connection;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventPoller;
import io.kodokojo.commons.event.JsonToEventConverter;
import io.kodokojo.commons.rabbitmq.RabbitMqConnectionFactory;
import io.kodokojo.commons.rabbitmq.RabbitMqEventBus;
import io.kodokojo.commons.rabbitmq.RabbitMqEventPoller;
import io.kodokojo.commons.utils.DockerTestApplicationBuilder;
import org.assertj.core.api.Assertions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EventBusThen<SELF extends EventBusThen<?>> extends Stage<SELF> implements DockerTestApplicationBuilder, JsonToEventConverter, RabbitMqConnectionFactory {

    @ExpectedScenarioState
    Connection connection;

    @ExpectedScenarioState
    RabbitMqEventBus eventBus;

    @ExpectedScenarioState
    RabbitMqEventPoller rabbitMqEventListener;

    public SELF event_bus_receive_the_hello_event_of_given_service() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<Event> events = rabbitMqEventListener.poll();

        assertThat(events).isNotEmpty();
        events.stream().forEach(System.out::println);

        EventPoller poller = eventBus.provideEventPoller();
        assertThat(poller).isNotNull();
        assertThat(poller.getClass()).isAssignableFrom(RabbitMqEventPoller.class);
        RabbitMqEventPoller rabbitMqEventPoller = (RabbitMqEventPoller) poller;
        events = rabbitMqEventPoller.poll();

        assertThat(events).isNotEmpty();
        events.stream().forEach(System.out::println);
        eventBus.disconnect();
        connection.abort();

        return self();
    }
}