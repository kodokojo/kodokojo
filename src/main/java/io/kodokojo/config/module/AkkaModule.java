package io.kodokojo.config.module;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.brick.BrickStateEventDispatcher;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.service.*;
import io.kodokojo.service.actor.entity.EntityEndpointActor;
import io.kodokojo.service.actor.event.EventEndpointActor;
import io.kodokojo.service.actor.project.ProjectEndpointActor;
import io.kodokojo.service.actor.user.UserEndpointActor;
import io.kodokojo.service.repository.EntityRepository;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.UserRepository;


public class AkkaModule extends AbstractModule {
    @Override
    protected void configure() {
        ActorSystem actorSystem = ActorSystem.apply("kodokojo");
        bind(ActorSystem.class).toInstance(actorSystem);
    }

    @Provides
    @Named(UserEndpointActor.NAME)
    Props provideUserEndpointProps(UserRepository userRepository, EmailSender emailSender) {
        return UserEndpointActor.PROPS(userRepository, emailSender);
    }

    @Provides
    @Named(ProjectEndpointActor.NAME)
    Props provideProjectEndpointProps(ProjectRepository projectRepository, UserRepository userRepository, ProjectManager projectManager, BrickFactory brickFactory, BrickManager brickManager, BootstrapConfigurationProvider bootstrapConfigurationProvider, ConfigurationStore configurationStore, BrickUrlFactory brickUrlFactory, SSLCertificatProvider sslCertificatProvider) {
        return ProjectEndpointActor.PROPS(projectRepository, userRepository, projectManager, brickFactory, brickManager, bootstrapConfigurationProvider, configurationStore, brickUrlFactory, sslCertificatProvider);
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
