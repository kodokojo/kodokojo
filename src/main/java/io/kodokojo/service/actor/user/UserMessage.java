/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
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

        private final String email;

        private final String firstName;

        private final String lastName;

        public UserUpdateMessage(User requester, User userToUpdate, String newPassword, String newSSHPublicKey, String firstName, String lastName, String email) {
            super(requester);
            requireNonNull(userToUpdate, "userToUpdate must be defined.");
            this.userToUpdate = userToUpdate;
            this.newPassword = newPassword;
            this.newSSHPublicKey = newSSHPublicKey;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
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

        public String getEmail() {
            return email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }
}
