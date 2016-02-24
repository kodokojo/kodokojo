package io.kodokojo.entrypoint;

/*
 * #%L
 * project-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.commons.project.model.User;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.user.*;
import io.kodokojo.user.redis.RedisUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static spark.Spark.*;

public class RestEntrypoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntrypoint.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private static final String TEXT_CONTENT_TYPE = "text/plain";

    private static final String API_VERSION = "v1";

    private static final String BASE_API = "/api/" +API_VERSION ;

    private final int port;

    private final UserManager userManager;

    private final UserAuthentificator<SimpleCredential> userAuthentificator;


    private final ResponseTransformer jsonResponseTransformer;

    public RestEntrypoint(int port, UserManager userManager, UserAuthentificator<SimpleCredential> userAuthentificator) {
        this.port = port;
        this.userManager = userManager;
        this.userAuthentificator = userAuthentificator;
        jsonResponseTransformer = new JsonTransformer();
    }

    public void start() {

        Spark.port(port);

        staticFileLocation("webapp");

        before((request, response) -> {
            boolean authenticationRequired = true ;
            if (requestMatch("POST", BASE_API + "/user", request) ||
                    requestMatch("GET", BASE_API, request) ||
                    requestMatch("GET", BASE_API + "/doc/.*", request) ||
                    requestMatch("PUT", BASE_API+ "/user/[^/]*", request)) {
                authenticationRequired = false;
            }


            if (authenticationRequired) {
                Authenticator authenticator = new Authenticator();
                authenticator.handle(request, response);
                if (authenticator.isProvideCredentials()) {
                    User user = userAuthentificator.authenticate(new SimpleCredential(authenticator.getUsername(), authenticator.getPassword()));
                    if (user != null) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("User is user under user {}.", user);
                        }
                    } else {
                        authorizationRequiered(response);
                    }
                } else {
                    authorizationRequiered(response);
                }
            }
        });

        get(BASE_API, JSON_CONTENT_TYPE, (request, response) -> {
            response.type(JSON_CONTENT_TYPE);
            return "{\"version\":\"1.0.0\"}";
        });

        post(BASE_API + "/user", JSON_CONTENT_TYPE, (request, response) -> {
            String res = userManager.generateId();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Generate id : {}", res);
            }
            return res;
        });

        put(BASE_API + "/user/:id", JSON_CONTENT_TYPE, ((request, response) -> {
            String identifier = request.params(":id");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to create user with id {}", identifier);
            }
            if (userManager.identifierExpectedNewUser(identifier)) {
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(request.body());
                String email = json.getAsJsonPrimitive("email").getAsString();
                String username = email.substring(0,email.lastIndexOf("@"));

                String password = new BigInteger(130, new SecureRandom()).toString(32);
                KeyPair keyPair = RSAUtils.generateRsaKeyPair();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                User user = new User(identifier, username, username, email, password, RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), email));
                if (userManager.addUser(user)) {
                    response.status(201);
                    StringWriter sw = new StringWriter();
                    RSAUtils.writeRsaPrivateKey(privateKey, sw);
                    return new UserCreationDto(user, sw.toString());
                }
                halt(409);
                return "";
            } else {
                halt(412);
                return "";
            }
        }), jsonResponseTransformer);


        Spark.awaitInitialization();

    }

    public void stop() {
        Spark.stop();
    }

    private void authorizationRequiered(Response response) {
        response.header("WWW-Authenticate", "Basic realm=\"Kodokojo\"");
        halt(401);
    }

    private static boolean requestMatch(String methodName, String path, Request request) {
        boolean matchMethod = methodName.equals(request.requestMethod());
        boolean pathMatch = request.pathInfo().matches(path);
        return matchMethod && pathMatch;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        SecretKey aesKey = generator.generateKey();

        RedisUserManager redisUserManager = new RedisUserManager(aesKey, "192.168.99.100", 6379);
        redisUserManager.addUser(new User(redisUserManager.generateId(), "Jean-Pascal THIERY", "jpthiery", "jpthiery@xebia.fr", "jpascal", "SSHPublic key"));
        RestEntrypoint restEntrypoint = new RestEntrypoint(8080, redisUserManager, redisUserManager);
        restEntrypoint.start();
        ;
    }

}
