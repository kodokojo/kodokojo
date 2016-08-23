/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.EmailSender;
import io.kodokojo.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserEndpointActor extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpointActor.class);

    public static Props PROPS(UserRepository userRepository, EmailSender emailSender, ActorRef eventEndpointActor) {
        return Props.create(UserEndpointActor.class, userRepository, emailSender, eventEndpointActor);
    }

    public UserEndpointActor(UserRepository userRepository, EmailSender emailSender, ActorRef eventEndpointActor) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }

        receive(ReceiveBuilder.match(UserGenerateIdentifierActor.UserGenerateIdentifierMsg.class, msg -> {
            getContext().actorOf(UserGenerateIdentifierActor.PROPS(userRepository)).forward(msg, getContext());
        })
                .match(UserCreatorActor.UserCreateMsg.class, msg -> {
                    getContext().actorOf(UserCreatorActor.PROPS(userRepository, emailSender)).forward(msg, getContext());
                })
                .match(UserFetcherActor.UserFetchMsg.class, msg -> {
                    getContext().actorOf(UserFetcherActor.PROPS(userRepository)).forward(msg, getContext());

                })
                .matchAny(this::unhandled)
                .build());

    }


}
