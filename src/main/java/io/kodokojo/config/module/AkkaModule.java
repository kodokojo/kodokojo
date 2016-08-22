package io.kodokojo.config.module;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.service.EmailSender;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.repository.Repository;

public class AkkaModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ActorSystem.class).toInstance(ActorSystem.apply("kodokojo"));
    }

    @Provides
    @Singleton
    ActorRef provideEndpointActor(ActorSystem system, Repository repository, EmailSender emailSender) {
        return system.actorOf(EndpointActor.PROPS(repository, emailSender), "endpoint");
    }

}
