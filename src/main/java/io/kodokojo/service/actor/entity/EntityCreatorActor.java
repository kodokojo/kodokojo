package io.kodokojo.service.actor.entity;

import akka.actor.AbstractActor;
import akka.actor.Props;
import io.kodokojo.model.Entity;
import io.kodokojo.service.repository.EntityRepository;

public class EntityCreatorActor extends AbstractActor {

    public static Props PROPS(EntityRepository entityRepository) {
        return Props.create(EntityCreatorActor.class, entityRepository);
    }

    private final EntityRepository entityRepository;

    public EntityCreatorActor(EntityRepository entityRepository) {
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        this.entityRepository = entityRepository;
    }

    public static class EntityCreatorMessage {

        protected final Entity entity;

        public EntityCreatorMessage(Entity entity) {
            if (entity == null) {
                throw new IllegalArgumentException("entity must be defined.");
            }
            this.entity = entity;
        }
    }

}
