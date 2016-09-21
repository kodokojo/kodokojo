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
package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import io.kodokojo.brick.BrickStartContext;
import io.kodokojo.service.actor.message.BrickStateEvent;
import io.kodokojo.service.actor.entity.AddUserToEntityActor;
import io.kodokojo.service.actor.entity.EntityCreatorActor;
import io.kodokojo.service.actor.entity.EntityEndpointActor;
import io.kodokojo.service.actor.event.EventEndpointActor;
import io.kodokojo.service.actor.project.*;
import io.kodokojo.service.actor.user.*;

import static akka.event.Logging.getLogger;

public class EndpointActor extends AbstractActor {

    public static final String ACTOR_PATH = "/user/endpoint";

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(Injector injector) {
        if (injector == null) {
            throw new IllegalArgumentException("injector must be defined.");
        }
        return Props.create(EndpointActor.class, injector);
    }

    public static final String NAME = "endpointAkka";

    private final ActorRef userEndpoint;

    private final ActorRef entityEndpoint;

    private final ActorRef projectEndpoint;

    private final ActorRef eventEndpointNotifier;

    public EndpointActor(Injector injector) {

        eventEndpointNotifier = getContext().actorOf(injector.getInstance(Key.get(Props.class, Names.named(EventEndpointActor.NAME))), "eventEndpoint");
        userEndpoint = getContext().actorOf(injector.getInstance(Key.get(Props.class, Names.named(UserEndpointActor.NAME))), "userEndpoint");
        entityEndpoint = getContext().actorOf(injector.getInstance(Key.get(Props.class, Names.named(EntityEndpointActor.NAME))), "entityEndpoint");
        projectEndpoint = getContext().actorOf(injector.getInstance(Key.get(Props.class, Names.named(ProjectEndpointActor.NAME))), "projectEndpoint");
        receive(ReceiveBuilder.match(UserGenerateIdentifierActor.UserGenerateIdentifierMsg.class, msg -> {
            userEndpoint.forward(msg, getContext());
        }).match(UserCreatorActor.UserCreateMsg.class, msg -> {
            //eventEndpointNotifier.tell(new EventEndpointActor.EventMsg(msg.getRequester(), ""));
            userEndpoint.forward(msg, getContext());
        }).match(UserFetcherActor.UserFetchMsg.class, msg -> {
            userEndpoint.forward(msg, getContext());
        }).match(UserServiceCreatorActor.UserServiceCreateMsg.class, msg -> {
            userEndpoint.forward(msg, getContext());
        }).match(EntityCreatorActor.EntityCreateMsg.class, msg -> {
            entityEndpoint.forward(msg, getContext());
        }).match(AddUserToEntityActor.AddUserToEntityMsg.class, msg -> {
            entityEndpoint.forward(msg, getContext());
        }).match(ProjectConfigurationBuilderActor.ProjectConfigurationBuildMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(BootstrapStackActor.BootstrapStackMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(ProjectConfigurationDtoCreatorActor.ProjectConfigurationDtoCreateMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(ProjectConfigurationBuilderActor.ProjectConfigurationBuildMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(BrickStartContext.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(BrickStateEvent.class, msg -> {
            LOGGER.debug("Forward BrickStateEvent to EventEndpoint.");
            eventEndpointNotifier.forward(msg, getContext());
        }).match(ProjectCreatorActor.ProjectCreateMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(ProjectUpdaterActor.ProjectUpdateMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(StackConfigurationStarterActor.StackConfigurationStartMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(ProjectConfigurationStarterActor.ProjectConfigurationStartMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(ProjectConfigurationUpdaterActor.ProjectConfigurationUpdaterMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        }).match(BrickPropertyToBrickConfigurationActor.BrickPropertyToBrickConfigurationMsg.class, msg -> {
            projectEndpoint.forward(msg, getContext());
        })
                .matchAny(this::unhandled).build());
    }


}
