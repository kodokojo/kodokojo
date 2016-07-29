package io.kodokojo.service.actor.entity;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
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

        receive(ReceiveBuilder.match(AddUserToEntityActor.AddUserToEntityMessage.class, msg -> {
            getContext().actorOf(AddUserToEntityActor.PROPS(entityRepository)).tell(msg, self());
        }).matchAny(this::unhandled).build());

    }




}
