package io.kodokojo.service.actor.entity;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.UserRequestMessage;
import io.kodokojo.service.repository.EntityRepository;

import static org.apache.commons.lang.StringUtils.isBlank;

public class AddUserToEntityActor extends AbstractActor {

    public static Props PROPS(EntityRepository entityRepository) {
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        return Props.create(AddUserToEntityActor.class, entityRepository);
    }

    public AddUserToEntityActor(EntityRepository entityRepository) {
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        receive(ReceiveBuilder.match(AddUserToEntityMessage.class, msg -> {
            entityRepository.addUserToEntity(msg.userId, msg.entityId);
            getContext().stop(self());
        }).matchAny(this::unhandled).build());
    }

    public static class AddUserToEntityMessage extends UserRequestMessage {

        protected final String userId;

        protected final String entityId;

        public AddUserToEntityMessage(User requester, String userId, String entityId) {
            super(requester);
            if (isBlank(userId)) {
                throw new IllegalArgumentException("userId must be defined.");
            }
            if (isBlank(entityId)) {
                throw new IllegalArgumentException("entityId must be defined.");
            }
            this.userId = userId;
            this.entityId = entityId;
        }
    }
}
