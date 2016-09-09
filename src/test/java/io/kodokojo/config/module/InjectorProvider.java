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
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.config.module.endpoint.BrickEndpointModule;
import io.kodokojo.config.module.endpoint.ProjectEndpointModule;
import io.kodokojo.config.module.endpoint.UserEndpointModule;
import io.kodokojo.service.BrickManager;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.actor.EndpointActor;

public class InjectorProvider {

    private final String[] args;
    private final DockerTestSupport dockerTestSupport;
    private final int httpPort;
    private final String redisHost;
    private final int redisPort;
    private final ProjectManager projectManager;
    private final BrickManager brickManager;
    private final ConfigurationStore configurationStore;

    public InjectorProvider(String[] args, DockerTestSupport dockerTestSupport, int httpPort, String redisHost, int redisPort, ProjectManager projectManager, BrickManager brickManager, ConfigurationStore configurationStore) {
        this.args = args;
        this.dockerTestSupport = dockerTestSupport;
        this.httpPort = httpPort;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.projectManager = projectManager;
        this.brickManager = brickManager;
        this.configurationStore = configurationStore;
    }

    public Injector provideInjector() {
        Injector propertyInjector = Guice.createInjector(new TestPropertyModule(args, dockerTestSupport,httpPort, redisHost, redisPort));
        Injector servicesInjector = propertyInjector.createChildInjector(new ServiceModule(), new DatabaseModule(), new TestSecurityModule());
        Injector orchestratorInjector = servicesInjector.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProjectManager.class).toInstance(projectManager);
                bind(BrickManager.class).toInstance(brickManager);
                bind(ConfigurationStore.class).toInstance(configurationStore);
            }
        });
        Injector akkaInjector = orchestratorInjector.createChildInjector(new AkkaModule());
        ActorSystem actorSystem = akkaInjector.getInstance(ActorSystem.class);
        ActorRef endpointActor = actorSystem.actorOf(EndpointActor.PROPS(akkaInjector), "endpoint");
        Injector res = akkaInjector.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ActorRef.class).annotatedWith(Names.named(EndpointActor.NAME)).toInstance(endpointActor);
            }
        }, new HttpModule(), new UserEndpointModule(), new ProjectEndpointModule(), new BrickEndpointModule());
        return  res;
    }



}
