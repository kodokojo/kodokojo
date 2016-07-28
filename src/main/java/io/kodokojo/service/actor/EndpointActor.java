package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.model.Entity;
import io.kodokojo.service.actor.entity.EntityCreatorActor;
import io.kodokojo.service.actor.entity.EntityEndpointActor;
import io.kodokojo.service.actor.user.UserCreatorActor;
import io.kodokojo.service.actor.user.UserEndpointActor;
import io.kodokojo.service.repository.EntityRepository;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.Repository;
import io.kodokojo.service.repository.UserRepository;
import org.apache.commons.lang.StringUtils;

public class EndpointActor extends AbstractActor {

    public static Props PROPS(UserRepository userRepository, EntityRepository entityRepository, ProjectRepository projectRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(EndpointActor.class, userRepository, entityRepository, projectRepository);
    }

    public static Props PROPS(Repository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must be defined.");
        }
        return PROPS(repository, repository, repository);
    }

    private final ActorRef userEndpoint;

    private final ActorRef entityEndpoint;

    private final ActorRef projectEndpoint;

    public EndpointActor(UserRepository userRepository, EntityRepository entityRepository, ProjectRepository projectRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }

        userEndpoint = getContext().actorOf(UserEndpointActor.PROPS(userRepository));
        entityEndpoint = getContext().actorOf(EntityEndpointActor.PROPS(entityRepository));
        projectEndpoint = null;

        receive(ReceiveBuilder.match(UserCreatorActor.UserCreateMsg.class, msg -> {

            getContext().actorOf(UserCreatorActor.PROPS(userRepository)).tell(msg,self());

        }).match(UserCreatorActor.UserCreatedMessage.class, msg -> {
            if (StringUtils.isBlank(msg.getEntityNameRequested())) {
                Entity entity = new Entity(msg.getUser().getEmail(), msg.getUser());
                entityEndpoint.tell(new EntityCreatorActor.EntityCreatorMessage(entity), self());
            }
        })
                .matchAny(this::unhandled).build());
    }
}
