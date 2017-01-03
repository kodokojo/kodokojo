/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
