package io.kodokojo.service.actor.user;

import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;

import static java.util.Objects.requireNonNull;

public interface UserMessage {


    class UserUpdateMessageResult extends UserRequestMessage {

        private final boolean success;

        public UserUpdateMessageResult(User requester,boolean success) {
            super(requester);
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    class UserUpdateMessage extends UserRequestMessage {

        private final User userToUpdate;

        private final String newPassword;

        private final String newSSHPublicKey;

        public UserUpdateMessage(User requester, User userToUpdate, String newPassword, String newSSHPublicKey) {
            super(requester);
            requireNonNull(userToUpdate, "userToUpdate must be defined.");
            this.userToUpdate = userToUpdate;
            this.newPassword = newPassword;
            this.newSSHPublicKey = newSSHPublicKey;
        }

        public User getUserToUpdate() {
            return userToUpdate;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public String getNewSSHPublicKey() {
            return newSSHPublicKey;
        }
    }
}
