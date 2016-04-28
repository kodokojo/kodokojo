package io.kodokojo.config.module;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.service.BrickManager;
import io.kodokojo.brick.BrickConfigurationStarter;
import io.kodokojo.brick.BrickStateMsgDispatcher;
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
        //TODO Put Router configuration in file.
        return system.actorOf(new RoundRobinPool(4).props(Props.create(BrickConfigurationStarterActor.class, brickManager, configurationStore, dnsManager, stateListener)),"brickConfigurationStarter");
    }

    @Provides
    @Named("brickStateMsgEndpoint")
    ActorRef provideBrickStateMsgEndpoint(ActorSystem system, BrickStateMsgDispatcher dispatcher) {
        return system.actorOf(Props.create(BrickStateMsgEndpoint.class, dispatcher), "brickStateMsgEndpoint");
    }

    @Provides
    @Singleton
    BrickConfigurationStarter provideBrickConfigurationStarter(@Named("brickConfigurationStarter") ActorRef brickConfigurationStarter) {
        return new BrickConfigurationStarterActorAdapter(brickConfigurationStarter);
    }

}
