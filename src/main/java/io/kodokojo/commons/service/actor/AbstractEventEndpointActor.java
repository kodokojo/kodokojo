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

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.event.LoggingAdapter;
import akka.japi.pf.UnitPFBuilder;
import akka.pattern.Patterns;
import com.google.inject.Injector;
import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventBuilder;
import io.kodokojo.commons.event.EventBuilderFactory;
import io.kodokojo.commons.event.EventBus;
import io.kodokojo.commons.model.User;
import io.kodokojo.commons.service.actor.message.EventBusOriginMessage;
import io.kodokojo.commons.service.actor.message.EventReplyableMessage;
import io.kodokojo.commons.service.repository.UserFetcher;
import javaslang.control.Try;
import org.apache.commons.lang.StringUtils;
import scala.PartialFunction;
import scala.concurrent.Future;

import javax.inject.Inject;

import java.util.function.Consumer;

import static akka.event.Logging.getLogger;
import static java.util.Objects.requireNonNull;

public abstract class AbstractEventEndpointActor extends AbstractActor {

    public static final String ACTOR_PATH = "/user/endpoint";

    public static final String NO_PROCESSED = "No processed";

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final String NAME = "endpointAkka";

    protected final Injector injector;

    protected final UserFetcher userFetcher;

    protected final EventBus eventBus;

    protected final EventBuilderFactory eventBuilderFactory;

    @Inject
    public AbstractEventEndpointActor(Injector injector) {
        requireNonNull(injector, "injector must be defined.");
        this.injector = injector;
        userFetcher = injector.getInstance(UserFetcher.class);
        eventBuilderFactory = injector.getInstance(EventBuilderFactory.class);
        eventBus = injector.getInstance(EventBus.class);

        receive(messageMatcherBuilder()
                .match(EventFromEventBusWrapper.class, wrapper -> {
                    this.receive(wrapper.getEvent(), sender());
                })
                .match(Event.class, event -> {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Sending following message to EventBus:\n{}", Event.convertToPrettyJson(event));
                    }
                    eventBus.send(event);
                })
                .match(EventReplyableMessage.class, msg -> {

                    onEventReplyableMessagePreReply(msg, eventBuilderFactory);
                    EventBuilder eventBuilder = eventBuilderFactory.create();
                    eventBuilder.setPayload(msg.payloadReply());
                    eventBuilder.setEventType(msg.eventType());
                    eventBuilder.setCategory(Event.Category.BUSINESS);
                    Event event = eventBuilder.build();
                    eventBus.reply(msg.originalEvent(), event);
                    onEventReplyableMessagePostReply(msg, eventBuilderFactory);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Reply following event to {}:\n {}", msg.originalEvent().getFrom(), Event.convertToPrettyJson(event));
                    }

                })
                .matchAny(this::unhandled)
                .build());

    }

    protected void dispatch(Object msg, ActorRef sender, ActorRef target) {
        target.forward(msg, getContext());
    }

    protected abstract Try<ActorRefWithMessage> convertToActorRefWithMessage(Event event, User requester);

    protected abstract UnitPFBuilder<Object> messageMatcherBuilder();

    protected void onEventReplyableMessagePreReply(EventReplyableMessage msg, EventBuilderFactory eventBuilderFactory) {
        //  Nothing to do.
    }

    protected void onEventReplyableMessagePostReply(EventReplyableMessage msg, EventBuilderFactory eventBuilderFactory) {
        //  Nothing to do.
    }


    public void receive(Event event, ActorRef sender) {
        requireNonNull(event, "event must be defined.");
        User requester = null;
        String requesterIdentifier = event.getCustom().get(Event.REQUESTER_ID_CUSTOM_HEADER);
        if (StringUtils.isNotBlank(requesterIdentifier)) {
            requester = userFetcher.getUserByIdentifier(requesterIdentifier);
        }
        Try<ActorRefWithMessage> eventBusOriginMessageTry = convertToActorRefWithMessage(event, requester);
        eventBusOriginMessageTry.andThen(tupleTry -> {
            if (tupleTry.actorRef != null && tupleTry.eventBusOriginMessage != null && tupleTry.eventBusOriginMessage.requireToBeCompleteBeforeAckEventBus()) {
                LOGGER.debug("Delegate processing to actor {}.", tupleTry.actorRef);
                Future<Object> future = Patterns.ask(tupleTry.actorRef, tupleTry.eventBusOriginMessage, tupleTry.eventBusOriginMessage.timeout());
                Patterns.pipe(future, getContext().dispatcher()).to(sender);
                future.onComplete(new OnComplete() {
                    @Override
                    public void onComplete(Throwable failure, Object result) throws Throwable {
                        if (failure == null) {
                            if (result instanceof EventReplyableMessage) {
                                self().tell(result, self());
                                LOGGER.debug("Process an event success");
                            } else {
                                LOGGER.debug("Process an event FAIL");
                                if (result instanceof Throwable) {
                                    throw (Throwable) result;
                                }
                            }
                        } else {
                            LOGGER.error("An error occur when process event :\n{}\n{}", Event.convertToPrettyJson(event), failure);
                            throw failure;
                        }
                    }

                }, getContext().dispatcher());
            } else {
                sender.tell(NO_PROCESSED, self());    //  Not mapped, ignore this event and drop it.
            }
        }).orElseRun(throwable -> {
            LOGGER.debug("Not mapped");
            sender.tell(NO_PROCESSED, self());    //  Not mapped, ignore this event and drop it.
        });
    }

    public static class EventFromEventBusWrapper {

        private final Event event;

        public EventFromEventBusWrapper(Event event) {
            requireNonNull(event, "event must be defined.");
            this.event = event;
        }

        public Event getEvent() {
            return event;
        }
    }


    protected static class ActorRefWithMessage {

        private final ActorRef actorRef;

        private final EventBusOriginMessage eventBusOriginMessage;

        public ActorRefWithMessage(ActorRef actorRef, EventBusOriginMessage eventBusOriginMessage) {
            this.actorRef = actorRef;
            this.eventBusOriginMessage = eventBusOriginMessage;
        }

        public EventBusOriginMessage getEventBusOriginMessage() {
            return eventBusOriginMessage;
        }

        public ActorRef getActorRef() {
            return actorRef;
        }
    }
}
