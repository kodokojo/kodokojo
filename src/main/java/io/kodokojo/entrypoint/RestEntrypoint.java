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
import io.kodokojo.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.model.User;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.user.SimpleCredential;
import io.kodokojo.user.UserAuthenticator;
import io.kodokojo.user.UserCreationDto;
import io.kodokojo.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static spark.Spark.*;

public class RestEntrypoint implements ApplicationLifeCycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntrypoint.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private static final String TEXT_CONTENT_TYPE = "text/plain";

    private static final String API_VERSION = "v1";

    private static final String BASE_API = "/api/" + API_VERSION;

    private final int port;

    private final UserManager userManager;

    private final UserAuthenticator<SimpleCredential> userAuthenticator;

    private final ResponseTransformer jsonResponseTransformer;

    public RestEntrypoint(int port, UserManager userManager, UserAuthenticator<SimpleCredential> userAuthenticator) {
        this.port = port;
        this.userManager = userManager;
        this.userAuthenticator = userAuthenticator;
        jsonResponseTransformer = new JsonTransformer();
    }

    @Override
    public void start() {

        Spark.port(port);

        staticFileLocation("webapp");

        before((request, response) -> {
            boolean authenticationRequired = true;
            // White list of url which not require to have an identifier.
            if (requestMatch("POST", BASE_API + "/user", request) ||
                    requestMatch("GET", BASE_API, request) ||
                    requestMatch("GET", BASE_API + "/doc(/)?.*", request) ||
                    requestMatch("PUT", BASE_API + "/user/[^/]*", request)) {
                authenticationRequired = false;
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Authentication is {}require for request {} {}.", authenticationRequired ? "": "NOT ", request.requestMethod(), request.pathInfo());
            }
            if (authenticationRequired) {
                Authenticator authenticator = new Authenticator();
                authenticator.handle(request, response);
                if (authenticator.isProvideCredentials()) {
                    User user = userAuthenticator.authenticate(new SimpleCredential(authenticator.getUsername(), authenticator.getPassword()));
                    if (user == null) {
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
                String username = email.substring(0, email.lastIndexOf("@"));
                User userByUsername = userManager.getUserByUsername(username);
                if (userByUsername != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Trying to create user {} from email '{}' who already exist.", username, email);
                    }
                    halt(409);
                    return "";
                }

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

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The UserManager not abel to add following user {}.", user.toString());
                }
                halt(428);
                return "";
            } else {
                halt(412);
                return "";
            }
        }), jsonResponseTransformer);

        get(BASE_API + "/user/:id", JSON_CONTENT_TYPE, (request, response) -> {
            SimpleCredential credential = extractCredential(request);
            if (credential != null) {
                String identifier = request.params(":id");
                User user = userManager.getUserByIdentifier(identifier);
                if (user.getUsername().equals(credential.getUsername())) {
                    return user;
                } else {
                    return new User(user.getIdentifier(), user.getName(), user.getUsername(), "", "", "");
                }
            }
            halt(500);
            return "";
        }, jsonResponseTransformer);

        Spark.awaitInitialization();

    }

    public int getPort() {
        return port;
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping RestEntryPoint.");
        Spark.stop();
    }

    private static void authorizationRequiered(Response response) {
        response.header("WWW-Authenticate", "Basic realm=\"Kodokojo\"");
        halt(401);
    }

    private static boolean requestMatch(String methodName, String regexpPath, Request request) {
        boolean matchMethod = methodName.equals(request.requestMethod());
        boolean pathMatch = request.pathInfo().matches(regexpPath);
        return matchMethod && pathMatch;
    }

    private static SimpleCredential extractCredential(Request request) {
        Authenticator authenticator = new Authenticator();
        try {
            authenticator.handle(request, null);
            if (authenticator.isProvideCredentials()) {
                return new SimpleCredential(authenticator.getUsername(), authenticator.getPassword());
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to retrieve credentials", e);
        }
        return null;
    }

}
