package io.kodokojo.config.module;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.project.starter.BrickManager;
import io.kodokojo.service.BrickConfigurationStarter;
import io.kodokojo.service.BrickStateMsgDispatcher;
import io.kodokojo.service.BrickStateMsgListener;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.actor.BrickConfigurationStarterActor;
import io.kodokojo.service.actor.BrickConfigurationStarterActorAdapter;
import io.kodokojo.service.actor.BrickStateMsgEndpoint;
import io.kodokojo.service.dns.DnsManager;

import javax.inject.Named;

public class ActorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ActorSystem.class).toInstance(ActorSystem.apply("kodokojo"));
    }

    @Provides
    @Named("brickConfigurationStarter")
    ActorRef provideBrickConfigurationStarterActor(ActorSystem system, BrickManager brickManager, ConfigurationStore configurationStore, DnsManager dnsManager,@Named("brickStateMsgEndpoint")  ActorRef stateListener) {
        return system.actorOf(Props.create(BrickConfigurationStarterActor.class, brickManager, configurationStore, dnsManager, stateListener));
    }

    @Provides
    @Named("brickStateMsgEndpoint")
    ActorRef provideBrickStateMsgEndpoint(ActorSystem system, BrickStateMsgDispatcher dispatcher) {
        return system.actorOf(Props.create(BrickStateMsgEndpoint.class, dispatcher));
    }

    @Provides
    @Singleton
    BrickConfigurationStarter provideBrickConfigurationStarter(@Named("brickConfigurationStarter") ActorRef brickConfigurationStarter) {
        return new BrickConfigurationStarterActorAdapter(brickConfigurationStarter);
    }
/*
    @Provides
    ActorRef providePushEventDispatcher(ActorSystem system, @Named("pushEventChecker") ActorRef pushEventChecker, @Named("registryRequestWorker") ActorRef registryRequestWorker) {
        return system.actorOf(Props.create(PushEventDispatcher.class, pushEventChecker, registryRequestWorker));
    }
*/
}
