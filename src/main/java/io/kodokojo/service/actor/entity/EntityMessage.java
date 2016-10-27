package io.kodokojo.service.actor.entity;

import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;

import static org.apache.commons.lang.StringUtils.isBlank;

public interface EntityMessage {
    class AddUserToEntityMsg extends UserRequestMessage {

        protected final String userId;

        protected final String entityId;

        public AddUserToEntityMsg(User requester, String userId, String entityId) {
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
