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

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.repository.UserRepository;

import static org.apache.commons.lang.StringUtils.isBlank;

public class UserGenerateIdentifierActor extends AbstractActor {

    public static Props PROPS(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        return Props.create(UserGenerateIdentifierActor.class, userRepository);
    }

    public UserGenerateIdentifierActor(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }

        receive(ReceiveBuilder.match(UserGenerateIdentifierMsg.class, msg -> {

            String generateId = userRepository.generateId();
            sender().tell(new UserGenerateIdentifierResultMsg(generateId), self());
            getContext().stop(self());

        }).matchAny(this::unhandled).build());
    }

    public static class UserGenerateIdentifierMsg {

    }

    public static class UserGenerateIdentifierResultMsg {

        private final String generateId;

        public UserGenerateIdentifierResultMsg(String generateId) {
            if (isBlank(generateId)) {
                throw new IllegalArgumentException("generateId must be defined.");
            }
            this.generateId = generateId;
        }

        public String getGenerateId() {
            return generateId;
        }
    }

}
