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
package io.kodokojo.bdd.stage.cluster;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.*;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterScenario;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.Launcher;
import io.kodokojo.bdd.MarathonBrickUrlFactory;
import io.kodokojo.bdd.stage.*;
import io.kodokojo.brick.*;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;
import io.kodokojo.commons.utils.servicelocator.marathon.MarathonServiceLocator;
import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.config.ApplicationConfig;
import io.kodokojo.config.MarathonConfig;
import io.kodokojo.config.module.*;
import io.kodokojo.config.module.endpoint.ProjectEndpointModule;
import io.kodokojo.config.module.endpoint.UserEndpointModule;
import io.kodokojo.endpoint.HttpEndpoint;
import io.kodokojo.endpoint.ProjectSparkEndpoint;
import io.kodokojo.endpoint.SparkEndpoint;
import io.kodokojo.endpoint.UserAuthenticator;
import io.kodokojo.service.*;
import io.kodokojo.service.dns.NoOpDnsManager;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import io.kodokojo.service.marathon.MarathonBrickManager;
import io.kodokojo.service.marathon.MarathonConfigurationStore;
import io.kodokojo.service.redis.RedisUserStore;
import io.kodokojo.service.store.EntityStore;
import io.kodokojo.service.store.ProjectStore;
import io.kodokojo.service.store.UserStore;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.service.authentification.SimpleUserAuthenticator;
import io.kodokojo.test.utils.TestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.websocket.Session;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClusterApplicationGiven<SELF extends ClusterApplicationGiven<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterApplicationGiven.class);

    private static final Properties VE_PROPERTIES = new Properties();
    public static final String DOCKER_MESOS_SLAVE_IMAGE_NAME = "mesosphere/mesos-slave:0.28.0-2.0.16.ubuntu1404";
    public static final String DOCKER_MARATHON_IMAGE_NAME = "mesosphere/marathon:latest";
    public static final String DOCKER_MESOS_MASTER_IMAGE_NAME = "mesosphere/mesos-master:0.28.0-2.0.16.ubuntu1404";
    public static final String DOCKER_ZOOKEEPER_IMAGE_NAME = "jplock/zookeeper:latest";

    static {
        VE_PROPERTIES.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE_PROPERTIES.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VE_PROPERTIES.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    }

    @ProvidedScenarioState
    Injector injector;

    @ProvidedScenarioState
    DockerTestSupport dockerTestSupport;

    @ProvidedScenarioState
    HttpEndpoint httpEndpoint;

    @ProvidedScenarioState
    String restEntryPointHost;

    @ProvidedScenarioState
    int restEntryPointPort;

    @ProvidedScenarioState
    ProjectManager projectManager;

    @ProvidedScenarioState
    EntityStore entityStore;

    @ProvidedScenarioState
    ProjectStore projectStore;

    @ProvidedScenarioState
    String marathonUrl;

    @ProvidedScenarioState
    Service redisService;

    @ProvidedScenarioState
    String domain;

    @ProvidedScenarioState
    UserStore userStore;

    @ProvidedScenarioState
    String testId;

    @ProvidedScenarioState
    UserInfo currentUser;

    @ProvidedScenarioState
    KeyPair userKeyPair;

    @ProvidedScenarioState
    Session currentUserWebSocket;

    @ProvidedScenarioState
    WebSocketEventsListener webSocketEventsListener;

    @ProvidedScenarioState
    HttpUserSupport httpUserSupport;

    @ProvidedScenarioState
    List<Service> services = new ArrayList<>();

    public SELF kodokojo_is_running(@Hidden DockerPresentMethodRule dockerPresentMethodRule) {
        dockerTestSupport = dockerPresentMethodRule.getDockerTestSupport();
        startMesosCluster();
        try {
            Thread.sleep(10000); //Allow to all component to start.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        marathonUrl = "http://" + dockerTestSupport.getServerIp() + ":8080";

        return kodokojo_is_running_on_domain_$("kodokojo.dev");
    }

    public SELF kodokojo_is_running_on_domain_$(@Quoted String domain) {

        this.domain = domain;
        testId = generateUid();
        startRedis();
        startKodokojo();
        return self();
    }

    public SELF i_am_user_$(@Quoted String username) {

        currentUser = httpUserSupport.createUser(null, username + "@kodokojo.dev");


        CountDownLatch nbMessageExpected = new CountDownLatch(1000);
        WebSocketConnectionResult webSocketConnectionResult = httpUserSupport.connectToWebSocketAndWaitMessage(currentUser, nbMessageExpected);
        webSocketEventsListener = webSocketConnectionResult.getListener();
        currentUserWebSocket = webSocketConnectionResult.getSession();
        return self();
    }

    @AfterScenario
    public void tearDown() {
        if (StringUtils.isNotBlank(marathonUrl)) {
            try {
                killAllAppInMarathon(marathonUrl);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (httpEndpoint != null) {
            httpEndpoint.stop();
            httpEndpoint = null;
        }
        if (injector != null) {
            ApplicationLifeCycleManager applicationLifeCycleManager = injector.getInstance(ApplicationLifeCycleManager.class);
            applicationLifeCycleManager.stop();
            Launcher.INJECTOR = null;
            injector = null;
        }
        for (Service service : services) {
            killApp(service.getName());
        }
        services.clear();
        services = null;
        redisService = null;
        dockerTestSupport.stopAndRemoveContainer();
        dockerTestSupport = null;

    }

    private String generateUid() {
        byte[] seed = new byte[1024];
        new Random(System.currentTimeMillis()).nextBytes(seed);
        SecureRandom secureRandom = new SecureRandom();
        return new BigInteger(130, secureRandom).toString().substring(0, 5);
    }

    private void startRedis() {
        redisService = StageUtils.startDockerRedis(dockerTestSupport);
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance("AES");
            userStore = new RedisUserStore(kg.generateKey(), redisService.getHost(), redisService.getPort());
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
    }

    private void startMesosCluster() {
        DockerClient dockerClient = dockerTestSupport.getDockerClient();
        String zookeeper = startZookeeper(dockerClient);
        String mesosMaster = startMesosMaster(dockerClient, zookeeper);
        startMesosSlave(dockerClient, mesosMaster);
        startMarathon(dockerClient, zookeeper);
    }

    private String startZookeeper(DockerClient dockerClient) {

        LOGGER.info("Pulling docker image jplock/zookeeper");
        dockerTestSupport.pullImage(DOCKER_ZOOKEEPER_IMAGE_NAME);

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(2181);
        portBinding.bind(exposedPort, Ports.Binding(null));
        CreateContainerResponse zookeeperContainer = dockerClient.createContainerCmd(DOCKER_ZOOKEEPER_IMAGE_NAME)
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort).exec();

        dockerClient.startContainerCmd(zookeeperContainer.getId()).exec();
        dockerTestSupport.addContainerIdToClean(zookeeperContainer.getId());
        return zookeeperContainer.getId();
    }

    private String startMesosMaster(DockerClient dockerClient, String zookeeperId) {

        LOGGER.info("Pulling docker image mesosphere/mesos-master");
        dockerTestSupport.pullImage(DOCKER_MESOS_MASTER_IMAGE_NAME);

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(5050);
        portBinding.bind(exposedPort, Ports.Binding(5050));

        int zookeeperPort = dockerTestSupport.getExposedPort(zookeeperId, 2181);
        String serverIp = dockerTestSupport.getServerIp();

        CreateContainerResponse mesosMasterContainer = dockerClient.createContainerCmd(DOCKER_MESOS_MASTER_IMAGE_NAME)
                .withCmd("--zk=zk://" + serverIp + ":" + zookeeperPort + "/mesos",
                        "--registry=in_memory", "--advertise_ip=" + serverIp,
                        "--no-hostname_lookup"
                )
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort)
                .exec();
        dockerClient.startContainerCmd(mesosMasterContainer.getId()).exec();
        dockerTestSupport.addContainerIdToClean(mesosMasterContainer.getId());
        return mesosMasterContainer.getId();

    }

    private String startMesosSlave(DockerClient dockerClient, String mesosMasterId) {

        LOGGER.info("Pulling docker image mesosphere/mesos-slave");
        dockerTestSupport.pullImage(DOCKER_MESOS_SLAVE_IMAGE_NAME);

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(5051);
        portBinding.bind(exposedPort, Ports.Binding(5051));

        int mesosMasterPort = dockerTestSupport.getExposedPort(mesosMasterId, 5050);
        String serverIp = dockerTestSupport.getServerIp();

        ArrayList<Bind> bind = new ArrayList<>(Arrays.asList(
                new Bind("/usr/local/bin/docker", new Volume("/usr/local/bin/docker")),
                new Bind("/var/run/docker.sock", new Volume("/var/run/docker.sock"))
        ));

        CreateContainerResponse mesosSlaveContainer = dockerClient.createContainerCmd(DOCKER_MESOS_SLAVE_IMAGE_NAME)
                .withCmd("--master=" + serverIp + ":" + mesosMasterPort,
                        "--containerizers=docker,mesos",
                        "--docker=/usr/local/bin/docker",
                        "--advertise_ip=" + serverIp,
                        "--no-hostname_lookup",
                        "--resources=mem(*):2048;ports(*):[80-80,443-443,10000-20000]"
                )
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort)
                .withBinds(bind.toArray(new Bind[0]))
                .withPrivileged(true)
                .exec();
        dockerClient.startContainerCmd(mesosSlaveContainer.getId()).exec();
        dockerTestSupport.addContainerIdToClean(mesosSlaveContainer.getId());
        return mesosSlaveContainer.getId();

    }

    private String startMarathon(DockerClient dockerClient, String zookeeperId) {

        LOGGER.info("Pulling docker image mesosphere/marathon");
        dockerTestSupport.pullImage(DOCKER_MARATHON_IMAGE_NAME);

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(8080);
        portBinding.bind(exposedPort, Ports.Binding(8080));

        int zookeeperPort = dockerTestSupport.getExposedPort(zookeeperId, 2181);
        String serverIp = dockerTestSupport.getServerIp();

        CreateContainerResponse marathonContainer = dockerClient.createContainerCmd(DOCKER_MARATHON_IMAGE_NAME)
                .withCmd("--master", "zk://" + serverIp + ":" + zookeeperPort + "/mesos",
                        "--zk", "zk://" + serverIp + ":" + zookeeperPort + "/marathon",
                        "--hostname", serverIp,
                        "--event_subscriber", "http_callback",
                        "--artifact_store", "file:///tmp/"
                )
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort)
                .exec();
        dockerClient.startContainerCmd(marathonContainer.getId()).exec();
        dockerTestSupport.addContainerIdToClean(marathonContainer.getId());
        return marathonContainer.getId();

    }


    private void killApp(String appId) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().delete().url(marathonUrl + "/v2/apps/" + appId).build();
        try {
            Response response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(200);
        } catch (IOException e) {
            LOGGER.error("Unable to kill app {}.", appId, e);
        }
    }


    private void startKodokojo() {
        String keystorePath = System.getProperty("javax.net.ssl.keyStore", null);
        if (StringUtils.isBlank(keystorePath)) {
            String keystorePathDefined = new File("").getAbsolutePath() + "/src/test/resources/keystore/mykeystore.jks";
            System.out.println(keystorePathDefined);

            System.setProperty("javax.net.ssl.keyStore", keystorePathDefined);
        }
        BrickUrlFactory brickUrlFactory = new MarathonBrickUrlFactory(marathonUrl);
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("security.ssl.rootCa.ks.alias", "rootcafake");
        System.setProperty("security.ssl.rootCa.ks.password", "password");
        System.setProperty("application.dns.domain", "kodokojo.io");
        System.setProperty("redis.host", redisService.getHost());
        System.setProperty("redis.port", "" + redisService.getPort());

        System.setProperty("marathon.url", "http://" + dockerTestSupport.getServerIp() + ":8080");
        System.setProperty("lb.defaultIp", dockerTestSupport.getServerIp());
        System.setProperty("application.dns.domain", "kodokojo.dev");
        LOGGER.debug("redis.port: {}", System.getProperty("redis.port"));

        injector = Guice.createInjector(new PropertyModule(new String[]{}),
                new RedisModule(),
                new SecurityModule(),
                new ServiceModule(),
                new ActorModule(),
                new AwsModule(),
                new EmailSenderModule(),
                new UserEndpointModule(),
                new ProjectEndpointModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {

                    }

                    @Provides
                    @Singleton
                    ServiceLocator provideServiceLocator(MarathonConfig marathonConfig) {
                        return new MarathonServiceLocator(marathonConfig.url());
                    }

                    @Provides
                    @Singleton
                    ConfigurationStore provideConfigurationStore(MarathonConfig marathonConfig) {
                        return new MarathonConfigurationStore(marathonConfig.url());
                    }

                    @Provides
                    @Singleton
                    BrickManager provideBrickManager(MarathonConfig marathonConfig, BrickConfigurerProvider brickConfigurerProvider, ProjectStore projectStore, ApplicationConfig applicationConfig, BrickUrlFactory brickUrlFactory) {
                        MarathonServiceLocator marathonServiceLocator = new MarathonServiceLocator(marathonConfig.url());
                        return new MarathonBrickManager(marathonConfig.url(), marathonServiceLocator, brickConfigurerProvider, projectStore, false, applicationConfig.domain(), brickUrlFactory);
                    }
                });
        Launcher.INJECTOR = injector;
        userStore = injector.getInstance(UserStore.class);
        projectStore = injector.getInstance(ProjectStore.class);
        entityStore = injector.getInstance(EntityStore.class);
        //BrickFactory brickFactory = injector.getInstance(BrickFactory.class);
        restEntryPointHost = "localhost";
        restEntryPointPort = TestUtils.getEphemeralPort();
        projectManager = new DefaultProjectManager(injector.getInstance(SSLKeyPair.class),
                domain,
                injector.getInstance(ConfigurationStore.class),
                projectStore,
                injector.getInstance(BootstrapConfigurationProvider.class),
                new NoOpDnsManager(),
                new DefaultBrickConfigurerProvider(brickUrlFactory),
                injector.getInstance(BrickConfigurationStarter.class),
                brickUrlFactory,
                300000000
        );
        httpUserSupport = new HttpUserSupport(new OkHttpClient(), restEntryPointHost + ":" + restEntryPointPort);
        Set<SparkEndpoint> sparkEndpoints = new HashSet<>(injector.getInstance(Key.get(new TypeLiteral<Set<SparkEndpoint>>() {
        })));
        Key<UserAuthenticator<SimpleCredential>> authenticatorKey = Key.get(new TypeLiteral<UserAuthenticator<SimpleCredential>>() {
        });
        UserAuthenticator<SimpleCredential> userAuthenticator = injector.getInstance(authenticatorKey);
        sparkEndpoints.add(new ProjectSparkEndpoint(userAuthenticator, userStore, projectStore, projectManager, injector.getInstance(BrickFactory.class)));
        httpEndpoint = new HttpEndpoint(restEntryPointPort, new SimpleUserAuthenticator(userStore), sparkEndpoints);
        Semaphore semaphore = new Semaphore(1);
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Thread t = new Thread(() -> {
            httpEndpoint.start();
            semaphore.release();
        });
        t.start();
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void killAllAppInMarathon(String marathonUrl) {
        OkHttpClient httpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(marathonUrl + "/v2/apps").get();
        Response response = null;
        Set<String> appIds = new HashSet<>();

        try {
            response = httpClient.newCall(builder.build()).execute();
            String body = response.body().string();
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(body);
            JsonArray apps = json.getAsJsonArray("apps");
            for (JsonElement appEl : apps) {
                JsonObject app = (JsonObject) appEl;
                appIds.add(app.getAsJsonPrimitive("id").getAsString());
            }
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response.body());
            }
        }
        appIds.stream().forEach(id -> {
            Request.Builder rmBuilder = new Request.Builder().url(marathonUrl + "/v2/apps/" + id).delete();
            Response responseRm = null;
            try {
                LOGGER.debug("Delete Marathon application id {}.", id);
                responseRm = httpClient.newCall(rmBuilder.build()).execute();
            } catch (IOException e) {
                fail(e.getMessage());
            } finally {
                if (responseRm != null) {
                    IOUtils.closeQuietly(responseRm.body());
                }
            }
        });

    }

}
