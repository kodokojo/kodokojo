package io.kodokojo.bdd.stage;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterScenario;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.bdd.MarathonIsPresent;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.entrypoint.RestEntrypoint;
import io.kodokojo.model.User;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.user.SimpleUserAuthenticator;
import io.kodokojo.service.user.redis.RedisUserManager;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class ClusterApplicationGiven<SELF extends ClusterApplicationGiven<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterApplicationGiven.class);

    private static final Properties VE_PROPERTIES = new Properties();

    static {
        VE_PROPERTIES.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE_PROPERTIES.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VE_PROPERTIES.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    }

    @ProvidedScenarioState
    DockerTestSupport dockerTestSupport = new DockerTestSupport();

    @ProvidedScenarioState
    RestEntrypoint restEntrypoint;

    @ProvidedScenarioState
    String marathonUrl;

    @ProvidedScenarioState
    Service redisService;

    @ProvidedScenarioState
    String domain;

    @ProvidedScenarioState
    RedisUserManager redisUserManager;

    @ProvidedScenarioState
    String testId;

    @ProvidedScenarioState
    User currentUser;

    @ProvidedScenarioState
    KeyPair userKeyPair;

    @ProvidedScenarioState
    List<Service> services = new ArrayList<>();

    public SELF kodokojo_is_running(@Hidden MarathonIsPresent marathonIsPresent) {
        marathonUrl = marathonIsPresent.getMarathonUrl();
        return kodokojo_is_running_on_domain_$("kodokojo.io");
    }

    public SELF kodokojo_is_running(@Hidden DockerPresentMethodRule dockerPresentMethodRule) {
        startMesosCluster();
        try {
            Thread.sleep(12000); //Allow to all component to start.
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
        String identifier = redisUserManager.generateId();
        try {
            userKeyPair = RSAUtils.generateRsaKeyPair();
            String email = username + "@kodokojo.io";
            currentUser = new User(identifier, username, username, email, username, RSAUtils.encodePublicKey((RSAPublicKey) userKeyPair.getPublic(), email));
            redisUserManager.addUser(currentUser);
            LOGGER.info("Current user {} with password {}", currentUser, currentUser.getPassword());
        } catch (NoSuchAlgorithmException e) {
            fail("Unable to generate a new RSA key pair for user " + username, e);
        }
        return self();
    }

    @AfterScenario
    public void tear_down() {
        dockerTestSupport.stopAndRemoveContainer();
    }

    private String generateUid() {
        byte[] seed = new byte[1024];
        new Random(System.currentTimeMillis()).nextBytes(seed);
        SecureRandom secureRandom = new SecureRandom();
        return new BigInteger(130, secureRandom).toString().substring(0, 5);
    }

    private void startRedis() {
        VelocityEngine ve = new VelocityEngine();
        ve.init(VE_PROPERTIES);

        Template template = ve.getTemplate("marathon/redis.json.vm");

        VelocityContext context = new VelocityContext();
        String id = "/redis-" + testId;
        context.put("ID", id);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        String redisJson = sw.toString();

        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), redisJson.getBytes());
        String url = marathonUrl + "/v2/apps";
        System.out.println("Start Redis on url " + url);
        Request request = new Request.Builder().post(body).url(url).build();
        try {
            Response response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
            response.body().close();
            List<Service> servicesResponse = waitForAppAvailable(id);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            assertThat(servicesResponse).isNotEmpty();
            services.addAll(servicesResponse);
            redisService = servicesResponse.get(0);
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            redisUserManager = new RedisUserManager(kg.generateKey(), redisService.getHost(), redisService.getPort());
        } catch (NoSuchAlgorithmException | IOException e) {
            fail("Unable to start Redis", e);
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


        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(2181);
        portBinding.bind(exposedPort, Ports.Binding(null));
        CreateContainerResponse zookeeperContainer = dockerClient.createContainerCmd("jplock/zookeeper")
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort).exec();

        dockerClient.startContainerCmd(zookeeperContainer.getId()).exec();
        dockerTestSupport.addContainerIdToClean(zookeeperContainer.getId());
        return zookeeperContainer.getId();
    }

    private String startMesosMaster(DockerClient dockerClient, String zookeeperId) {

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(5050);
        portBinding.bind(exposedPort, Ports.Binding(5050));

        int zookeeperPort = dockerTestSupport.getExposedPort(zookeeperId, 2181);
        String serverIp = dockerTestSupport.getServerIp();

        CreateContainerResponse mesosMasterContainer = dockerClient.createContainerCmd("mesosphere/mesos-master:0.27.1-2.0.226.ubuntu1404")
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

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(5051);
        portBinding.bind(exposedPort, Ports.Binding(5051));

        int mesosMasterPort = dockerTestSupport.getExposedPort(mesosMasterId, 5050);
        String serverIp = dockerTestSupport.getServerIp();

        ArrayList<Bind> bind = new ArrayList<>(Arrays.asList(
                new Bind("/usr/local/bin/docker", new Volume("/usr/local/bin/docker")),
                new Bind("/var/run/docker.sock", new Volume("/var/run/docker.sock"))
        ));

        CreateContainerResponse mesosSlaveContainer = dockerClient.createContainerCmd("mesosphere/mesos-slave:0.27.1-2.0.226.ubuntu1404")
                .withCmd("--master=" + serverIp + ":" + mesosMasterPort,
                        "--containerizers=docker,mesos",
                        "--docker=/usr/local/bin/docker",
                        "--advertise_ip=" + serverIp,
                        "--no-hostname_lookup"
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

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(8080);
        portBinding.bind(exposedPort, Ports.Binding(8080));

        int zookeeperPort = dockerTestSupport.getExposedPort(zookeeperId, 2181);
        String serverIp = dockerTestSupport.getServerIp();

        CreateContainerResponse marathonContainer = dockerClient.createContainerCmd("mesosphere/marathon")
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

    private List<Service> waitForAppAvailable(String appId) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().get().url(marathonUrl + "/v2/apps/" + appId).build();
        List<Service> res = null;
        int nbMaxTry = 1000;
        int nbTry = 0;
        while (res == null && nbTry < nbMaxTry) {
            nbTry++;
            try {
                Response response = httpClient.newCall(request).execute();
                if (response.code() == 200) {
                    JsonParser parser = new JsonParser();
                    String body = response.body().string();
                    JsonObject rootJson = (JsonObject) parser.parse(body);
                    JsonObject app = rootJson.getAsJsonObject("app");
                    JsonArray tasks = app.getAsJsonArray("tasks");
                    for (int i = 0; i < tasks.size(); i++) {
                        JsonObject task = (JsonObject) tasks.get(i);
                        String host = task.getAsJsonPrimitive("host").getAsString();
                        JsonArray ports = task.getAsJsonArray("ports");
                        List<Service> tmp = new ArrayList<>(ports.size());
                        for (int j = 0; j < ports.size(); j++) {
                            tmp.add(new Service(appId, host, ports.get(j).getAsInt()));
                        }
                        res = new ArrayList<>(tmp);
                    }
                }
                response.body().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (res == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return res;
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

    @AfterScenario
    public void tearDown() {
        if (restEntrypoint != null) {
            restEntrypoint.stop();
        }
        for (Service service : services) {
            killApp(service.getName());
        }
    }

    private void startKodokojo() {

        ProjectManager projectManager = mock(ProjectManager.class); // Change this by launching a Mesos master/slave, Zookeeper and Marathon.
        restEntrypoint = new RestEntrypoint(8080, redisUserManager, new SimpleUserAuthenticator(redisUserManager), projectManager);
        restEntrypoint.start();

    }

    interface MarathonConfiguration {
        String getMarathonUrl();
    }

}
