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
package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.EmailSender;
import io.kodokojo.service.actor.entity.AddUserToEntityActor;
import io.kodokojo.service.actor.entity.EntityCreatorActor;
import io.kodokojo.service.actor.entity.EntityEndpointActor;
import io.kodokojo.service.actor.event.EventEndpointActor;
import io.kodokojo.service.actor.project.ProjectEndpointActor;
import io.kodokojo.service.actor.user.UserCreatorActor;
import io.kodokojo.service.actor.user.UserEndpointActor;
import io.kodokojo.service.actor.user.UserGenerateIdentifierActor;
import io.kodokojo.service.repository.EntityRepository;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.Repository;
import io.kodokojo.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static akka.event.Logging.getLogger;

public class EndpointActor extends AbstractActor {

    public static final String ACTOR_PATH = "/user/endpoint";

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(UserRepository userRepository, EntityRepository entityRepository, ProjectRepository projectRepository, EmailSender emailSender) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        if (emailSender == null) {
            throw new IllegalArgumentException("emailSender must be defined.");
        }
        return Props.create(EndpointActor.class, userRepository, entityRepository, projectRepository, emailSender);
    }

    public static Props PROPS(Repository repository, EmailSender emailSender) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must be defined.");
        }
        return PROPS(repository, repository, repository, emailSender);
    }

    private final ActorRef userEndpoint;

    private final ActorRef entityEndpoint;

    private final ActorRef projectEndpoint;

    private final ActorRef eventEndpointNotifier;

    public EndpointActor(UserRepository userRepository, EntityRepository entityRepository, ProjectRepository projectRepository, EmailSender emailSender) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        if (entityRepository == null) {
            throw new IllegalArgumentException("entityRepository must be defined.");
        }
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        if (emailSender == null) {
            throw new IllegalArgumentException("emailSender must be defined.");
        }

        this.eventEndpointNotifier = getContext().actorOf(EventEndpointActor.PROPS(), "eventEndpoint");
        userEndpoint = getContext().actorOf(UserEndpointActor.PROPS(userRepository, emailSender, eventEndpointNotifier), "userEndpoint");
        entityEndpoint = getContext().actorOf(EntityEndpointActor.PROPS(entityRepository,eventEndpointNotifier), "entityEndpoint");
        projectEndpoint = getContext().actorOf(ProjectEndpointActor.PROPS(projectRepository), "projectEndpoint");
        receive(ReceiveBuilder.match(UserGenerateIdentifierActor.UserGenerateIdentifierMsg.class, msg -> {
            userEndpoint.forward(msg, getContext());
        }).match(UserCreatorActor.UserCreateMsg.class, msg -> {
            //eventEndpointNotifier.tell(new EventEndpointActor.EventMsg(msg.getRequester(), ""));
            userEndpoint.forward(msg, getContext());
        }).match(EntityCreatorActor.EntityCreateMsg.class, msg -> {

            entityEndpoint.forward(msg, getContext());
        }).match(AddUserToEntityActor.AddUserToEntityMsg.class, msg -> {

            entityEndpoint.forward(msg, getContext());
        }).match(EntityCreatorActor.EntityCreatedResultMsg.class, msg -> {

        })
                .matchAny(this::unhandled).build());
    }


}
