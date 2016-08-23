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
package io.kodokojo.service.actor.entity;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.repository.EntityRepository;

import static akka.event.Logging.getLogger;

public class EntityEndpointActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(EntityRepository entityRepository, ActorRef eventEndpointActor) {
        return Props.create(EntityEndpointActor.class, entityRepository, eventEndpointActor);
    }

    public EntityEndpointActor(EntityRepository entityRepository, ActorRef eventEndpointActor) {
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        if (eventEndpointActor == null) {
            throw new IllegalArgumentException("eventEndpointActor must be defined.");
        }

        receive(ReceiveBuilder.match(AddUserToEntityActor.AddUserToEntityMsg.class, msg -> {
            getContext().actorOf(AddUserToEntityActor.PROPS(entityRepository)).forward(msg, getContext());
        })
                .match(EntityCreatorActor.EntityCreateMsg.class, msg -> {
                    getContext().actorOf(EntityCreatorActor.PROPS(entityRepository)).forward(msg, getContext());
                })
                .matchAny(this::unhandled).build());

    }




}
