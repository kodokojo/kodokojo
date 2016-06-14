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
package io.kodokojo.endpoint;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.kodokojo.brick.BrickFactory;
import io.kodokojo.brick.DefaultBrickFactory;
import io.kodokojo.endpoint.dto.ProjectConfigDto;
import io.kodokojo.endpoint.dto.ProjectCreationDto;
import io.kodokojo.endpoint.dto.ProjectDto;
import io.kodokojo.model.*;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.store.ProjectStore;
import io.kodokojo.service.store.UserStore;
import io.kodokojo.service.authentification.SimpleCredential;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class ProjectSparkEndpoint extends AbstractSparkEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectSparkEndpoint.class);

    private final ProjectManager projectManager;

    private final UserStore userStore;

    private final ProjectStore projectStore;

    private final BrickFactory brickFactory;

    @Inject
    public ProjectSparkEndpoint(UserAuthenticator<SimpleCredential> userAuthenticator, UserStore userStore, ProjectStore projectStore, ProjectManager projectManager, BrickFactory brickFactory) {
        super(userAuthenticator);
        if (userStore == null) {
            throw new IllegalArgumentException("userStore must be defined.");
        }
        if (projectManager == null) {
            throw new IllegalArgumentException("projectManager must be defined.");
        }
        if (projectStore == null) {
            throw new IllegalArgumentException("projectStore must be defined.");
        }
        if (brickFactory == null) {
            throw new IllegalArgumentException("brickFactory must be defined.");
        }
        this.userStore = userStore;
        this.projectStore = projectStore;
        this.projectManager = projectManager;
        this.brickFactory = brickFactory;
    }

    @Override
    public void configure() {
        post(BASE_API + "/projectconfig", JSON_CONTENT_TYPE, (request, response) -> {
            String body = request.body();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to create project {}", body);
            }
            Gson gson = localGson.get();
            ProjectCreationDto dto = gson.fromJson(body, ProjectCreationDto.class);
            if (dto == null) {
                halt(400);
                return "";
            }
            User owner = userStore.getUserByIdentifier(dto.getOwnerIdentifier());
            String entityId = owner.getEntityIdentifier();
            if (StringUtils.isBlank(entityId)) {
                halt(400);
                return "";
            }

            Set<StackConfiguration> stackConfigurations = createDefaultStackConfiguration(dto.getName());
            if (CollectionUtils.isNotEmpty(dto.getStackConfigs())) {
                stackConfigurations = dto.getStackConfigs().stream().map(stack -> {
                    Set<BrickConfiguration> brickConfigurations = stack.getBrickConfigs().stream().map(b -> {
                        Brick brick = brickFactory.createBrick(b.getName());
                        return new BrickConfiguration(brick);
                    }).collect(Collectors.toSet());
                    StackType stackType = StackType.valueOf(stack.getType());
                    BootstrapStackData bootstrapStackData = projectManager.bootstrapStack(dto.getName(), stack.getName(), stackType);
                    return new StackConfiguration(stack.getName(), stackType, brickConfigurations, bootstrapStackData.getLoadBalancerHost(), bootstrapStackData.getSshPort());
                }).collect(Collectors.toSet());
            }

            List<User> users = new ArrayList<>();
            users.add(owner);
            if (CollectionUtils.isNotEmpty(dto.getUserIdentifiers())) {
                for (String userId : dto.getUserIdentifiers()) {
                    User user = userStore.getUserByIdentifier(userId);
                    users.add(user);
                }
            }
            ProjectConfiguration projectConfiguration = new ProjectConfiguration(entityId, dto.getName(), Collections.singletonList(owner), stackConfigurations, users);
            String projectConfigIdentifier = projectStore.addProjectConfiguration(projectConfiguration);

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
            if (userStore.userIsAdminOfProjectConfiguration(credential.getUsername(), projectConfiguration)) {
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
            if (userStore.userIsAdminOfProjectConfiguration(credential.getUsername(), projectConfiguration)) {
                JsonParser parser = new JsonParser();
                JsonArray root = (JsonArray) parser.parse(request.body());
                List<User> users = IteratorUtils.toList(projectConfiguration.getUsers());
                List<User> usersToAdd = new ArrayList<>();
                for (JsonElement el : root) {
                    String userToAddId = el.getAsJsonPrimitive().getAsString();
                    User userToAdd = userStore.getUserByIdentifier(userToAddId);
                    if (userToAdd != null && !users.contains(userToAdd)) {
                        users.add(userToAdd);
                        usersToAdd.add(userToAdd);
                    }
                }

                projectConfiguration.setUsers(users);
                projectStore.updateProjectConfiguration(projectConfiguration);
                projectManager.addUsersToProject(projectConfiguration, usersToAdd);
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
                if (userStore.userIsAdminOfProjectConfiguration(credential.getUsername(), projectConfiguration)) {
                    JsonParser parser = new JsonParser();
                    JsonArray root = (JsonArray) parser.parse(request.body());
                    List<User> users = IteratorUtils.toList(projectConfiguration.getUsers());
                    for (JsonElement el : root) {
                        String userToDeleteId = el.getAsJsonPrimitive().getAsString();
                        User userToDelete = userStore.getUserByIdentifier(userToDeleteId);
                        if (userToDelete != null) {
                            users.remove(userToDelete);
                        }
                    }
                    projectConfiguration.setUsers(users);
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
                User currentUser = userStore.getUserByUsername(credential.getUsername());
                String projectConfigurationId = request.params(":id");
                ProjectConfiguration projectConfiguration = projectStore.getProjectConfigurationById(projectConfigurationId);
                if (projectConfiguration == null) {
                    halt(404, "Project configuration not found.");
                    return "";
                }
                if (userStore.userIsAdminOfProjectConfiguration(credential.getUsername(), projectConfiguration)) {
                    String projectId = projectStore.getProjectIdByProjectConfigurationId(projectConfigurationId);
                    if (StringUtils.isBlank(projectId)) {
                     //   projectManager.bootstrapStack(projectConfiguration.getName(), projectConfiguration.getDefaultStackConfiguration().getName(), projectConfiguration.getDefaultStackConfiguration().getType());
                        Project project = projectManager.start(projectConfiguration);
                        response.status(201);
                        String projectIdStarted = projectStore.addProject(project, projectConfigurationId);
                        return projectIdStarted;
                    } else {
                        halt(409, "Project already exist.");
                    }
                } else {
                    halt(403);
                }
            }
            return "";
        }));

        get(BASE_API + "/project/:id", JSON_CONTENT_TYPE, ((request, response) -> {
            SimpleCredential credential = extractCredential(request);
            if (credential != null) {
                User currentUser = userStore.getUserByUsername(credential.getUsername());
                String projectId = request.params(":id");
                Project project = projectStore.getProjectByIdentifier(projectId);
                if (project == null) {
                    halt(404);
                    return "";
                }
                ProjectConfiguration projectConfiguration = projectStore.getProjectConfigurationById(project.getProjectConfigurationIdentifier());
                if (userStore.userIsAdminOfProjectConfiguration(currentUser.getUsername(), projectConfiguration)) {
                    return new ProjectDto(project);
                } else {
                    halt(403);
                }
            }
            return "";
        }), jsonResponseTransformer);
    }

    private Set<StackConfiguration> createDefaultStackConfiguration(String projectName) {
        Set<BrickConfiguration> bricksConfigurations = new HashSet<>();
        //bricksConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.HAPROXY), false));
        bricksConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.JENKINS)));
        bricksConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.NEXUS)));
        bricksConfigurations.add(new BrickConfiguration(brickFactory.createBrick(DefaultBrickFactory.GITLAB)));
        String stackName = "build-A";
        BootstrapStackData bootstrapStackData = projectManager.bootstrapStack(projectName, stackName, StackType.BUILD);
        StackConfiguration stackConfiguration = new StackConfiguration(stackName, StackType.BUILD, bricksConfigurations, bootstrapStackData.getLoadBalancerHost(), bootstrapStackData.getSshPort());
        return Collections.singleton(stackConfiguration);
    }

}

