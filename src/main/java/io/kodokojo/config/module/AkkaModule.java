package io.kodokojo.config.module;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.service.EmailSender;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.actor.AkkaApplicationLifeCycleListener;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.repository.Repository;

public class AkkaModule extends AbstractModule {



    @Override
    protected void configure() {
        bind(ActorSystem.class).toInstance(ActorSystem.apply("kodokojo"));
    }

    @Provides
    @Singleton
    ActorRef provideEndpointActor(ActorSystem system, ApplicationLifeCycleManager applicationLifeCycleManager, Repository repository, EmailSender emailSender, ProjectManager projectManager, BrickFactory brickFactory) {
        applicationLifeCycleManager.addService(new AkkaApplicationLifeCycleListener(system));
        return system.actorOf(EndpointActor.PROPS(repository, emailSender, projectManager, brickFactory), "endpoint");
    }



}
