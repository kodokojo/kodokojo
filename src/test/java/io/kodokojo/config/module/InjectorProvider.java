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
