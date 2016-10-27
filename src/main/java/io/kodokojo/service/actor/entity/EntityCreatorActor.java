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
import io.kodokojo.model.Entity;
import io.kodokojo.service.repository.EntityRepository;

import static java.util.Objects.requireNonNull;

public class EntityCreatorActor extends AbstractActor {

    private final EntityRepository entityRepository;

    public EntityCreatorActor(EntityRepository entityRepository) {
        this.entityRepository = entityRepository;
        receive(ReceiveBuilder
                .match(EntityCreateMsg.class, this::onEntityCreateMsg)
                .matchAny(this::unhandled)
                .build());
    }

    public static Props PROPS(EntityRepository entityRepository) {
        requireNonNull(entityRepository, "entityRepository must be defined.");
        return Props.create(EntityCreatorActor.class, entityRepository);
    }

    private void onEntityCreateMsg(EntityCreateMsg msg) {
        String entityId = entityRepository.addEntity(msg.entity);
        sender().tell(new EntityCreatedResultMsg(entityId), self());
        getContext().stop(self());
    }

    public static class EntityCreateMsg {

        protected final Entity entity;

        public EntityCreateMsg(Entity entity) {
            if (entity == null) {
                throw new IllegalArgumentException("entity must be defined.");
            }
            this.entity = entity;
        }
    }

    public class EntityCreatedResultMsg {

        private final String entityId;

        public EntityCreatedResultMsg(String entityId) {
            this.entityId = entityId;
        }

        public String getEntityId() {
            return entityId;
        }
    }

}
