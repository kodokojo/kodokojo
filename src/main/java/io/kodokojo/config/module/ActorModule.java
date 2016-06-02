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
package io.kodokojo.config.module;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.service.BrickManager;
import io.kodokojo.brick.BrickConfigurationStarter;
import io.kodokojo.brick.BrickStateMsgDispatcher;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.SSLCertificatProvider;
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
    ActorRef provideBrickConfigurationStarterActor(ActorSystem system, BrickManager brickManager, ConfigurationStore configurationStore, BrickUrlFactory brickUrlFactory, SSLCertificatProvider sslCertificatProvider, @Named("brickStateMsgEndpoint")  ActorRef stateListener) {
        //TODO Put Router configuration in file.
        return system.actorOf(new RoundRobinPool(4).props(Props.create(BrickConfigurationStarterActor.class, brickManager, configurationStore, brickUrlFactory,sslCertificatProvider, stateListener)),"brickConfigurationStarter");
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
