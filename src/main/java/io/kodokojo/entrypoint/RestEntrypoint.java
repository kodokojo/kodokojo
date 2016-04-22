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

import com.google.gson.*;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.entrypoint.dto.ProjectConfigDto;
import io.kodokojo.entrypoint.dto.ProjectCreationDto;
import io.kodokojo.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.model.*;
import io.kodokojo.service.*;
import io.kodokojo.service.user.SimpleCredential;
import io.kodokojo.service.user.UserCreationDto;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;

import javax.inject.Inject;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import static spark.Spark.*;

public class RestEntrypoint implements ApplicationLifeCycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntrypoint.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private static final String TEXT_CONTENT_TYPE = "text/plain";

    private static final String API_VERSION = "v1";

    public static final String BASE_API = "/api/" + API_VERSION;

    private final int port;

    private final UserManager userManager;

    private final UserAuthenticator<SimpleCredential> userAuthenticator;

    private final ProjectManager projectManager;

    private final ProjectStore projectStore;

    private final BrickFactory brickFactory;

    private final ThreadLocal<Gson> localGson = new ThreadLocal<Gson>() {
        @Override
        protected Gson initialValue() {
            return new GsonBuilder().create();
        }
    };

    private final ResponseTransformer jsonResponseTransformer;

    @Inject
    public RestEntrypoint(int port, UserManager userManager, UserAuthenticator<SimpleCredential> userAuthenticator, ProjectStore projectStore, ProjectManager projectManager, BrickFactory brickFactory) {
        if (userManager == null) {
            throw new IllegalArgumentException("userManager must be defined.");
        }
        if (userAuthenticator == null) {
            throw new IllegalArgumentException("userAuthenticator must be defined.");
        }
        if (projectStore == null) {
            throw new IllegalArgumentException("projectStore must be defined.");
        }
        if (projectManager == null) {
            throw new IllegalArgumentException("projectManager must be defined.");
        }
        if (brickFactory == null) {
            throw new IllegalArgumentException("brickFactory must be defined.");
        }
        this.port = port;
        this.userManager = userManager;
        this.userAuthenticator = userAuthenticator;
        this.projectStore = projectStore;
        this.projectManager = projectManager;
        this.brickFactory = brickFactory;
        jsonResponseTransformer = new JsonTransformer();
    }

    @Override
    public void start() {

        Spark.port(port);

        webSocket(BASE_API + "/event", WebSocketEntrypoint.class);

        staticFileLocation("webapp");

        before((request, response) -> {
            boolean authenticationRequired = true;
            // White list of url which not require to have an identifier.
            if (requestMatch("POST", BASE_API + "/user", request) ||
                    requestMatch("GET", BASE_API, request) ||
                    //requestMatch("GET", BASE_API + "/event", request) ||
                    requestMatch("GET", BASE_API + "/doc(/)?.*", request) ||
                    requestMatch("POST", BASE_API + "/user/[^/]*", request)
                    ) {
                authenticationRequired = false;
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Authentication is {}require for request {} {}.", authenticationRequired ? "" : "NOT ", request.requestMethod(), request.pathInfo());
            }
            if (authenticationRequired) {
                BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
                basicAuthenticator.handle(request, response);
                if (basicAuthenticator.isProvideCredentials()) {
                    User user = userAuthenticator.authenticate(new SimpleCredential(basicAuthenticator.getUsername(), basicAuthenticator.getPassword()));
                    if (user == null) {
                        authorizationRequiered(response);

                    }
                } else {
                    authorizationRequiered(response);
                }
            }
        });


        //  User --

        post(BASE_API + "/user/:id", JSON_CONTENT_TYPE, ((request, response) -> {
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
                    response.header("Location", "/user/" + user.getIdentifier());
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

        post(BASE_API + "/user", JSON_CONTENT_TYPE, (request, response) -> {
            String res = userManager.generateId();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Generate id : {}", res);
            }
            return res;
        });

        get(BASE_API + "/user", JSON_CONTENT_TYPE, (request, response) -> {
            SimpleCredential credential = extractCredential(request);
            if (credential != null) {
                User user = userManager.getUserByUsername(credential.getUsername());
                return user;
            }
            halt(401);
            return "";
        }, jsonResponseTransformer);

        get(BASE_API + "/user/:id", JSON_CONTENT_TYPE, (request, response) -> {
            SimpleCredential credential = extractCredential(request);
            String identifier = request.params(":id");
            User user = userManager.getUserByIdentifier(identifier);
            if (user != null && credential != null) {
                if (user.getUsername().equals(credential.getUsername())) {
                    return user;
                } else {
                    return new User(user.getIdentifier(), user.getName(), user.getUsername(), "", "", "");
                }
            }
            halt(404);
            return "";
        }, jsonResponseTransformer);

        //  ProjectConfiguration --

        //  Create
        post(BASE_API + "/projectconfig", JSON_CONTENT_TYPE, (request, response) -> {
            String body = request.body();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to create project {}", body);
            }
            Gson gson = localGson.get();
            ProjectCreationDto dto = gson.fromJson(body, ProjectCreationDto.class);
            User owner = userManager.getUserByIdentifier(dto.getOwnerIdentifier());
            Set<StackConfiguration> stackConfigurations = createDefaultStackConfiguration(dto.getName());
            List<User> users = new ArrayList<>();
            users.add(owner);
            if (CollectionUtils.isNotEmpty(dto.getUserIdentifiers())) {
                for (String userId : dto.getUserIdentifiers()) {
                    User user = userManager.getUserByIdentifier(userId);
                    users.add(user);
                }
            }
            ProjectConfiguration projectConfiguration = new ProjectConfiguration(dto.getName(), owner, stackConfigurations, users);
            String projectConfigIdentifier = projectStore.addProjectConfiguration(projectConfiguration);

            /*
            //  Starting all bricks, may be done in an other endpoint with websocket...
            */


            response.status(201);
            response.header("Location", "/projectconfig/" + projectConfigIdentifier);
            return projectConfigIdentifier;
        });


        get(BASE_API + "/projectconfig/:id", JSON_CONTENT_TYPE, (request, response) -> {
            String identifier = request.params(":id");
            ProjectConfiguration projectConfiguration = projectStore.getProjectConfigurationById(identifier);
            if (projectConfiguration == null) {
                halt(404);
                return "";
            }
            SimpleCredential credential = extractCredential(request);
            User user = userManager.getUserByUsername(credential.getUsername());
            if (user.getIdentifier().equals(projectConfiguration.getOwner().getIdentifier())) {
                return new ProjectConfigDto(projectConfiguration);
            }
            halt(403);
            return "";
        }, jsonResponseTransformer);

        put(BASE_API + "/projectconfig/:id/user", JSON_CONTENT_TYPE, ((request, response) -> {
            SimpleCredential credential = extractCredential(request);

            String identifier = request.params(":id");
            ProjectConfiguration projectConfiguration = projectStore.getProjectConfigurationById(identifier);
            if (projectConfiguration == null) {
                halt(404);
                return "";
            }
            User user = userManager.getUserByUsername(credential.getUsername());
            if (user.getIdentifier().equals(projectConfiguration.getOwner().getIdentifier())) {
                JsonParser parser = new JsonParser();
                JsonArray root = (JsonArray) parser.parse(request.body());
                for (JsonElement el : root) {
                    String userToAddId = el.getAsJsonPrimitive().getAsString();
                    User userToAdd = userManager.getUserByIdentifier(userToAddId);
                    if (userToAdd != null) {
                        projectConfiguration.getUsers().add(userToAdd);
                    }
                }
                projectStore.updateProjectConfiguration(projectConfiguration);
            } else {
                halt(403);
            }

            return "";
        }), jsonResponseTransformer);

        delete(BASE_API + "/projectconfig/:id/user", JSON_CONTENT_TYPE, ((request, response) -> {
            SimpleCredential credential = extractCredential(request);
            if (credential != null) {
                String identifier = request.params(":id");
                ProjectConfiguration projectConfiguration = projectStore.getProjectConfigurationById(identifier);
                if (projectConfiguration == null) {
                    halt(404);
                    return "";
                }
                User user = userManager.getUserByUsername(credential.getUsername());
                if (user.getIdentifier().equals(projectConfiguration.getOwner().getIdentifier())) {
                    JsonParser parser = new JsonParser();
                    JsonArray root = (JsonArray) parser.parse(request.body());
                    for (JsonElement el : root) {
                        String userToDeleteId = el.getAsJsonPrimitive().getAsString();
                        User userToDelete = userManager.getUserByIdentifier(userToDeleteId);
                        if (userToDelete != null) {
                            projectConfiguration.getUsers().remove(userToDelete);
                        }
                    }
                    projectStore.updateProjectConfiguration(projectConfiguration);
                } else {
                    halt(403);
                }
            }
            return "";
        }), jsonResponseTransformer);

        //  -- Project

        //  Start project
        post(BASE_API + "/project/:id", JSON_CONTENT_TYPE, ((request, response) -> {
            SimpleCredential credential = extractCredential(request);
            if (credential != null) {
                User currentUser = userManager.getUserByUsername(credential.getUsername());
                String projectConfigurationId = request.params(":id");
                ProjectConfiguration projectConfiguration = projectStore.getProjectConfigurationById(projectConfigurationId);
                if (projectConfiguration == null) {
                    halt(404, "Project configuration not found.");
                    return "";
                }
                if (projectConfiguration.getOwner().getIdentifier().equals(currentUser.getIdentifier())) {
                    Project project = projectStore.getProjectByName(projectConfiguration.getName());
                    if (project == null) {
                        projectManager.bootstrapStack(projectConfiguration.getName(), projectConfiguration.getDefaultStackConfiguration().getName(), projectConfiguration.getDefaultStackConfiguration().getType());
                        project = projectManager.start(projectConfiguration);
                        return projectStore.addProject(project);
                    } else {
                        halt(409, "Project already exist.");
                    }
                } else {
                    halt(403);
                }
            }
            return "";
        }), jsonResponseTransformer);


        get(BASE_API, JSON_CONTENT_TYPE, (request, response) -> {
            response.type(JSON_CONTENT_TYPE);
            return "{\"version\":\"1.0.0\"}";
        });

        Spark.awaitInitialization();
        LOGGER.info("Spark server started on port {}.", port);

    }

    private Set<StackConfiguration> createDefaultStackConfiguration(String projectName) {
        Set<BrickConfiguration> bricksConfigurations = new HashSet<>();
        bricksConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.HAPROXY), false));
        bricksConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.JENKINS)));
        bricksConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.NEXUS)));
        bricksConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.GITLAB)));
        String stackName = "build-A";
        BootstrapStackData bootstrapStackData = projectManager.bootstrapStack(projectName, stackName, StackType.BUILD);
        StackConfiguration stackConfiguration = new StackConfiguration(stackName, StackType.BUILD, bricksConfigurations, bootstrapStackData.getLoadBalancerIp(), bootstrapStackData.getSshPort());
        return Collections.singleton(stackConfiguration);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Current request required an authentication which not currently provide.");
        }
        response.header("WWW-Authenticate", "Basic realm=\"Kodokojo\"");
        response.status(401);
        halt(401);
    }

    private static boolean requestMatch(String methodName, String regexpPath, Request request) {
        boolean matchMethod = methodName.equals(request.requestMethod());
        boolean pathMatch = request.pathInfo().matches(regexpPath);
        return matchMethod && pathMatch;
    }

    private static SimpleCredential extractCredential(Request request) {
        BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
        try {
            basicAuthenticator.handle(request, null);
            if (basicAuthenticator.isProvideCredentials()) {
                return new SimpleCredential(basicAuthenticator.getUsername(), basicAuthenticator.getPassword());
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to retrieve credentials", e);
        }
        return null;
    }


}
