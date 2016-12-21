package io.kodokojo.commons.service.actor;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventBus;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class EventToEndpointGateway implements EventBus.EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventToEndpointGateway.class);

    public static final int DURATION = 10;  // minute unit

    private final ActorRef endpoint;

    @Inject
    public EventToEndpointGateway(ActorRef endpoint) {
        requireNonNull(endpoint, "endpoint must be defined.");
        this.endpoint = endpoint;
    }

    @Override
    public Try<Boolean> receive(Event event) {
        requireNonNull(event, "event must be defined.");
        return Try.of(() -> {
            long timeoutMillis = DURATION * 60000;
            Future<Object> future = Patterns.ask(endpoint, new AbstractEventEndpointActor.EventFromEventBusWrapper(event), timeoutMillis);
            Object result = Await.result(future, scala.concurrent.duration.Duration.create(DURATION, TimeUnit.MINUTES));
            if (LOGGER.isDebugEnabled() && AbstractEventEndpointActor.NO_PROCESSED.equals(result)) {
                LOGGER.debug("Following event ignored, drop it from queue:\n{}", Event.convertToPrettyJson(event));
            }
            if (result instanceof Throwable) {
                throw new RuntimeException("Unable to process following event:\n"+ Event.convertToPrettyJson(event), (Throwable) result);
            }
            return result != null;
        });
    }
}
