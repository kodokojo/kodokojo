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
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import akka.event.LoggingAdapter;
import akka.japi.pf.UnitPFBuilder;
import akka.pattern.Patterns;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Injector;
import io.kodokojo.commons.event.*;
import io.kodokojo.commons.service.actor.message.EventBusOriginMessage;
import io.kodokojo.commons.service.actor.message.EventReplyableMessage;
import org.apache.commons.lang.StringUtils;
import scala.concurrent.Future;

import static akka.event.Logging.getLogger;
import static java.util.Objects.requireNonNull;

public abstract class AbstractEndpointActor extends AbstractActor {

    public static final String ACTOR_PATH = "/user/endpoint";

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static final String NAME = "endpointAkka";

    protected final Injector injector;

    protected final EventBus eventBus;


    public AbstractEndpointActor(Injector injector) {
        this.injector = injector;
        requireNonNull(injector, "injector must be defined.");
        EventBuilderFactory eventBuilderFactory = injector.getInstance(EventBuilderFactory.class);
        eventBus = injector.getInstance(EventBus.class);
        receive(messageMatcherBuilder()
                .match(Event.class, event -> {
                    if (LOGGER.isDebugEnabled()) {
                        Gson gson = new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).setPrettyPrinting().create();
                        LOGGER.debug("Sending following message to EventBus:\n{}", gson.toJson(event));
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
                        Gson gson = new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).setPrettyPrinting().create();
                        LOGGER.debug("Reply following event to {}:\n {}", msg.originalEvent().getFrom(), gson.toJson(event));
                    }

                })
                .matchAny(this::unhandled)
                .build());
    }

    protected abstract UnitPFBuilder<Object> messageMatcherBuilder();

    protected void onEventReplyableMessagePreReply(EventReplyableMessage msg, EventBuilderFactory eventBuilderFactory) {
        //  Nothing to do.
    }

    protected void onEventReplyableMessagePostReply(EventReplyableMessage msg, EventBuilderFactory eventBuilderFactory) {
        //  Nothing to do.
    }

    protected void dispatch(Object msg, ActorRef sender, ActorRef target) {
        boolean telling = false;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Dispatch message class {} from actor {} to actor Ref {}.", msg.getClass().getCanonicalName(), sender.toString(), target.toString());
        }
        if (msg instanceof EventBusOriginMessage && ((EventBusOriginMessage) msg).initialSenderIsEventBus()) {
            EventBusOriginMessage eventBusOriginMessage = (EventBusOriginMessage) msg;
            Event event = eventBusOriginMessage.originalEvent();
            if (LOGGER.isDebugEnabled()) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).setPrettyPrinting().create();
                LOGGER.debug("Dispatch message contain following event :\n{}", gson.toJson(event));
            }
            if (StringUtils.isNotBlank(event.getReplyTo()) &&
                    StringUtils.isNotBlank(event.getCorrelationId())) {
                if (eventBusOriginMessage.requireToBeCompleteBeforeAckEventBus()) {
                    Future<Object> future = Patterns.ask(target, msg, eventBusOriginMessage.timeout());
                    Patterns.pipe(future, getContext().dispatcher()).to(sender);
                    future.onSuccess(new OnSuccess<Object>() {
                        @Override
                        public void onSuccess(Object result) throws Throwable {
                            if (result instanceof EventReplyableMessage) {
                                self().forward(msg, getContext());  //  Allow to process reply in eventbus...
                            }
                        }
                    }, getContext().dispatcher());
                } else {
                    sender.tell(Futures.successful(Boolean.TRUE), self());
                    target.tell(msg, self());
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("TELL following message to actor {} : {}", target.toString(), msg);
                }
                telling = true;
            }
        }
        if (!telling) {
            target.forward(msg, getContext());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("FORWARDING following message to actor {} : {}", target.toString(), msg);
            }
        }
    }

}