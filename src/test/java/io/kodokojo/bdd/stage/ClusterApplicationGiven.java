package io.kodokojo.bdd.stage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.*;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import io.kodokojo.bdd.MarathonIsPresent;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.entrypoint.RestEntrypoint;
import io.kodokojo.model.User;
import io.kodokojo.user.SimpleUserAuthenticator;
import io.kodokojo.user.redis.RedisUserManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ClusterApplicationGiven<SELF extends ClusterApplicationGiven<?>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterApplicationGiven.class);

    private static final Properties VE_PROPERTIES = new Properties();

    static {
        VE_PROPERTIES.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        VE_PROPERTIES.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VE_PROPERTIES.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
    }

    @ProvidedScenarioState
    RestEntrypoint restEntrypoint;

    @ProvidedScenarioState
    MarathonIsPresent marathon;

    @ProvidedScenarioState
    Service redisService;

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
        marathon = marathonIsPresent;
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
            currentUser= new User(identifier, username, username, email, username, RSAUtils.encodePublicKey((RSAPublicKey) userKeyPair.getPublic(), email) );
            redisUserManager.addUser(currentUser);
            LOGGER.info("Current user {} with password {}", currentUser, currentUser.getPassword());
        } catch (NoSuchAlgorithmException e) {
            fail("Unable to generate a new RSA key pair for user " + username, e);
        }
        return self();
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
        Request request = new Request.Builder().post(body).url(marathon.getMarathonUrl() + "/v2/apps").build();
        try {
            Response response = httpClient.newCall(request).execute();
            assertThat(response.code()).isEqualTo(201);
            response.body().close();
            List<Service> servicesResponse = waitForAppAvailable(id);
            try {
                Thread.sleep(1000);
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

    private List<Service> waitForAppAvailable(String appId) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().get().url(marathon.getMarathonUrl() + "/v2/apps/" + appId).build();
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
        Request request = new Request.Builder().delete().url(marathon.getMarathonUrl() + "/v2/apps/" + appId).build();
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
        for(Service service : services) {
            killApp(service.getName());
        }
    }

    private void startKodokojo() {

        restEntrypoint = new RestEntrypoint(8080, redisUserManager, new SimpleUserAuthenticator(redisUserManager));
        restEntrypoint.start();
        /*
        VelocityEngine ve = new VelocityEngine();
        ve.init(VE_PROPERTIES);

        Template template = ve.getTemplate("marathon/kodokojo.json.vm");

        VelocityContext context = new VelocityContext();
        context.put("REDISHOST", redisService.getHost());
        context.put("REDISPORT", redisService.getPort());

        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        String redisJson = sw.toString();

        OkHttpClient httpClient = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), redisJson.getBytes());
        Request request = new Request.Builder().post(body).url(marathon.getMarathonUrl() + "/v2/apps").build();
        try {
            Response response = httpClient.newCall(request).execute();
            System.out.println(response.body().string());
            assertThat(response.code()).isIn(201, 409);
            response.body().close();
            List<Service> services = waitForAppAvailable("/kodokojo");
            assertThat(services).isNotEmpty();
            redisService = services.get(0);
        } catch (IOException e) {
            fail("Unable to start Redis", e);
        }
        */
    }

}
