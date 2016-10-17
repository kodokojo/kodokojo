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
package io.kodokojo.config.module;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import io.kodokojo.brick.BrickConfigurerProvider;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.brick.BrickStateEventDispatcher;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.service.BootstrapConfigurationProvider;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.SSLCertificatProvider;
import io.kodokojo.service.actor.DeadLetterActor;
import io.kodokojo.service.actor.entity.EntityEndpointActor;
import io.kodokojo.service.actor.event.EventEndpointActor;
import io.kodokojo.service.actor.project.ProjectEndpointActor;
import io.kodokojo.service.actor.user.UserEndpointActor;
import io.kodokojo.service.dns.DnsManager;
import io.kodokojo.service.repository.EntityRepository;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.UserRepository;


public class AkkaModule extends AbstractModule {
    @Override
    protected void configure() {
        ActorSystem actorSystem = ActorSystem.apply("kodokojo");
        ActorRef deadletterlistener = actorSystem.actorOf(DeadLetterActor.PROPS(), "deadletterlistener");
        actorSystem.eventStream().subscribe(deadletterlistener, DeadLetter.class);
        bind(ActorSystem.class).toInstance(actorSystem);
    }

    @Provides
    @Named(UserEndpointActor.NAME)
    Props provideUserEndpointProps(UserRepository userRepository) {
        return UserEndpointActor.PROPS(userRepository);
    }

    @Provides
    @Named(ProjectEndpointActor.NAME)
    Props provideProjectEndpointProps(ApplicationConfig applicationConfig, ProjectRepository projectRepository, UserRepository userRepository, DnsManager dnsManager, BrickFactory brickFactory, BrickManager brickManager, BootstrapConfigurationProvider bootstrapConfigurationProvider, ConfigurationStore configurationStore, BrickUrlFactory brickUrlFactory, SSLCertificatProvider sslCertificatProvider, BrickConfigurerProvider brickConfigurerProvider) {
        return ProjectEndpointActor.PROPS(applicationConfig, projectRepository, userRepository, dnsManager, brickFactory, brickManager, bootstrapConfigurationProvider, configurationStore, brickUrlFactory, sslCertificatProvider, brickConfigurerProvider);
    }

    @Provides
    @Named(EntityEndpointActor.NAME)
    Props provideEntityEndpointProps(EntityRepository entityRepository) {
        return EntityEndpointActor.PROPS(entityRepository);
    }

    @Provides
    @Named(EventEndpointActor.NAME)
    Props provideEventEndpointProps(BrickStateEventDispatcher brickStateEventDispatcher, ProjectRepository projectRepository) {
        return EventEndpointActor.PROPS(brickStateEventDispatcher, projectRepository);
    }

}
