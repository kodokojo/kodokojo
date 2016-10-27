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
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;
import io.kodokojo.service.repository.EntityRepository;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class AddUserToEntityActor extends AbstractActor {

    private final EntityRepository entityRepository;

    public AddUserToEntityActor(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;

        receive(ReceiveBuilder
                .match(EntityMessage.AddUserToEntityMsg.class, this::onAddUserToEntityMesg)
                .matchAny(this::unhandled)
                .build());
    }

    private void onAddUserToEntityMesg(EntityMessage.AddUserToEntityMsg msg) {
        entityRepository.addUserToEntity(msg.userId, msg.entityId);
        getContext().stop(self());
    }

    public static Props PROPS(EntityRepository entityRepository) {
        requireNonNull(entityRepository, "entityRepository must be defined.");
        return Props.create(AddUserToEntityActor.class, entityRepository);
    }

}
