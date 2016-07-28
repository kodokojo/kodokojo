package io.kodokojo.service.actor.entity;

import akka.actor.AbstractActor;
import akka.actor.Props;
import io.kodokojo.service.repository.EntityRepository;

public class EntityEndpointActor extends AbstractActor {

    public static Props PROPS(EntityRepository entityRepository) {
        return Props.create(EntityEndpointActor.class, entityRepository);
    }

    private final EntityRepository entityRepository;

    public EntityEndpointActor(EntityRepository entityRepository) {
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        this.entityRepository = entityRepository;



    }




}
