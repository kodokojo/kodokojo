package io.kodokojo.commons.service.actor;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.kodokojo.commons.event.Event;
import io.kodokojo.commons.event.EventBus;
import io.kodokojo.commons.event.GsonEventSerializer;
import io.kodokojo.commons.model.User;
import io.kodokojo.commons.service.actor.message.EventBusOriginMessage;
import io.kodokojo.commons.service.repository.UserFetcher;
import javaslang.control.Try;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public abstract class AbstractEventToActorGateway implements EventBus.EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEventToActorGateway.class);

    protected final ActorRef endpoint;

    protected final UserFetcher userFetcher;

    @Inject
    public AbstractEventToActorGateway(ActorRef endpoint, UserFetcher userFetcher) {
        requireNonNull(endpoint, "endpoint must be defined.");
        requireNonNull(userFetcher, "userFetcher must be defined.");
        this.endpoint = endpoint;
        this.userFetcher = userFetcher;
    }

    protected abstract EventBusOriginMessage messageToTell(Event event, User requester);

    @Override
    public void receive(Event event) {
        requireNonNull(event, "event must be defined.");
        User requester = null;
        String requesterIdentifier = event.getCustom().get(Event.REQUESTER_ID_CUSTOM_HEADER);
        if (StringUtils.isNotBlank(requesterIdentifier)) {
            requester = userFetcher.getUserByIdentifier(requesterIdentifier);
        }
        EventBusOriginMessage msg = messageToTell(event, requester);
        if (msg != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("TELL to actor {} message : {}", endpoint.toString(), msg);
            }
            if (msg.requireToBeCompleteBeforeAckEventBus()) {
                Future<Object> future = Patterns.ask(endpoint, msg, msg.timeout());

                Object result = null;
                try {
                    result = Await.result(future, Duration.apply(msg.timeout(), TimeUnit.MILLISECONDS));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to get a valid result for event " +event, e);
                }
                if (result == null) {
                    LOGGER.warn("Ack message from a Null result.");
                }

            } else {
                endpoint.tell(msg, ActorRef.noSender());
            }
        } else if (LOGGER.isDebugEnabled()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Event.class, new GsonEventSerializer()).setPrettyPrinting().create();
            LOGGER.debug("Drop following event which not match any actor :\n {}", gson.toJson(event));
        }
    }
}
