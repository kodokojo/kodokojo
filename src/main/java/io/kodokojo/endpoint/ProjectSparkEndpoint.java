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

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.name.Named;
import io.kodokojo.endpoint.dto.ProjectConfigDto;
import io.kodokojo.endpoint.dto.ProjectCreationDto;
import io.kodokojo.endpoint.dto.ProjectDto;
import io.kodokojo.model.Project;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.User;
import io.kodokojo.service.ProjectManager;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.project.*;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.UserFetcher;
import io.kodokojo.service.repository.UserRepository;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static spark.Spark.*;

public class ProjectSparkEndpoint extends AbstractSparkEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectSparkEndpoint.class);

    private final ActorRef akkaEndpoint;

    private final ProjectManager projectManager;

    private final UserFetcher userFetcher;

    private final ProjectRepository projectFetcher;

    @Inject
    public ProjectSparkEndpoint(UserAuthenticator<SimpleCredential> userAuthenticator, @Named(EndpointActor.NAME) ActorRef akkaEndpoint, ProjectManager projectManager, UserRepository userFetcher, ProjectRepository projectFetcher) {
        super(userAuthenticator);
        if (userFetcher == null) {
            throw new IllegalArgumentException("userFetcher must be defined.");
        }
        if (akkaEndpoint == null) {
            throw new IllegalArgumentException("akkaEndpoint must be defined.");
        }
        if (projectManager == null) {
            throw new IllegalArgumentException("projectManager must be defined.");
        }
        if (projectFetcher == null) {
            throw new IllegalArgumentException("projectFetcher must be defined.");
        }
        this.userFetcher = userFetcher;
        this.projectManager = projectManager;
        this.projectFetcher = projectFetcher;
        this.akkaEndpoint = akkaEndpoint;
    }

    @Override
    public void configure() {
        post(BASE_API + "/projectconfig", JSON_CONTENT_TYPE, (request, response) -> {
            User requester = getRequester(request);

            String body = request.body();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to create project configuration {}", body);
            }
            Gson gson = localGson.get();
            ProjectCreationDto dto = gson.fromJson(body, ProjectCreationDto.class);
            if (dto == null) {
                halt(400);
                return "";
            }

            FiniteDuration duration = Duration.apply(30, TimeUnit.SECONDS);

            Future<Object> future = ask(akkaEndpoint, new ProjectConfigurationDtoCreatorActor.ProjectConfigurationDtoCreateMsg(requester, dto), new Timeout(duration));
            ProjectConfigurationDtoCreatorActor.ProjectConfigurationDtoCreateResultMsg result = (ProjectConfigurationDtoCreatorActor.ProjectConfigurationDtoCreateResultMsg) Await.result(future, duration);
            String projectConfigIdentifier = result.getProjectConfigurationId();

            response.status(201);
            response.header("Location", "/projectconfig/" + projectConfigIdentifier);
            return projectConfigIdentifier;
        });


        get(BASE_API + "/projectconfig/:id", JSON_CONTENT_TYPE, (request, response) -> {
            String identifier = request.params(":id");
            ProjectConfiguration projectConfiguration = projectFetcher.getProjectConfigurationById(identifier);
            if (projectConfiguration == null) {
                halt(404);
                return "";
            }
            User requester = getRequester(request);

            if (userIsAdmin(requester, projectConfiguration)) {
                return new ProjectConfigDto(projectConfiguration);
            }

            halt(403, "Your are not admin of project configuration '" + projectConfiguration.getName() + "'.");
            return "";
        }, jsonResponseTransformer);

        put(BASE_API + "/projectconfig/:id/user", JSON_CONTENT_TYPE, ((request, response) -> {
            User requester = getRequester(request);

            String identifier = request.params(":id");

            ProjectConfiguration projectConfiguration = projectFetcher.getProjectConfigurationById(identifier);

            if (projectConfiguration == null) {
                halt(404);
                return "";
            }

            if (userIsAdmin(requester, projectConfiguration)) {

                List<String> userIdsToAdd = new ArrayList<>();
                JsonParser parser = new JsonParser();
                JsonArray root = (JsonArray) parser.parse(request.body());
                for (JsonElement el : root) {
                    String userToAddId = el.getAsJsonPrimitive().getAsString();
                    userIdsToAdd.add(userToAddId);
                }
                akkaEndpoint.tell(new ProjectConfigurationChangeUserActor.ProjectConfigurationChangeUserMsg(requester, TypeChange.ADD, identifier, userIdsToAdd), ActorRef.noSender());
/*
            projectConfiguration.setUsers(users);
            projectFetcher.updateProjectConfiguration(projectConfiguration);
            LOGGER.debug("Adding user {} to projectConfig {}", usersToAdd, projectConfiguration);
            projectManager.addUsersToProject(projectConfiguration, usersToAdd);
*/
            } else {
                halt(403, "You have not right to add user to project configuration id " + identifier + ".");

            }
            return "";
        }), jsonResponseTransformer);

        delete(BASE_API + "/projectconfig/:id/user", JSON_CONTENT_TYPE, ((request, response) -> {
            User requester = getRequester(request);

            String identifier = request.params(":id");

            ProjectConfiguration projectConfiguration = projectFetcher.getProjectConfigurationById(identifier);
            if (projectConfiguration == null) {
                halt(404);
                return "";
            }

            if (userIsAdmin(requester, projectConfiguration)) {

                List<String> userIdsToAdd = new ArrayList<>();
                JsonParser parser = new JsonParser();
                JsonArray root = (JsonArray) parser.parse(request.body());
                for (JsonElement el : root) {
                    String userToAddId = el.getAsJsonPrimitive().getAsString();
                    userIdsToAdd.add(userToAddId);
                }
                akkaEndpoint.tell(new ProjectConfigurationChangeUserActor.ProjectConfigurationChangeUserMsg(requester, TypeChange.REMOVE, identifier, userIdsToAdd), ActorRef.noSender());

            } else {
                halt(403, "You have not right to add user to project configuration id " + identifier + ".");

            }
            return "";
        }), jsonResponseTransformer);

        //  -- Project

        //  Start project
        post(BASE_API + "/project/:id", JSON_CONTENT_TYPE, ((request, response) -> {
            User requester = getRequester(request);
            String projectConfigurationId = request.params(":id");
            ProjectConfiguration projectConfiguration = projectFetcher.getProjectConfigurationById(projectConfigurationId);
            if (projectConfiguration == null) {
                halt(404, "Project configuration not found.");
                return "";
            }
            if (userIsAdmin(requester, projectConfiguration)) {
                String projectId = projectFetcher.getProjectIdByProjectConfigurationId(projectConfigurationId);
                if (StringUtils.isBlank(projectId)) {
                    //   projectManager.bootstrapStack(projectConfiguration.getName(), projectConfiguration.getDefaultStackConfiguration().getName(), projectConfiguration.getDefaultStackConfiguration().getType());
                    //   Project project = projectManager.start(projectConfiguration);
                    Future<Object> locahost = Patterns.ask(akkaEndpoint, new ProjectConfigurationStarterActor.ProjectConfigurationStartMsg(requester, projectConfiguration), Timeout.apply(6, TimeUnit.MINUTES));
                    ProjectCreatorActor.ProjectCreateResultMsg result = (ProjectCreatorActor.ProjectCreateResultMsg) Await.result(locahost, Duration.apply(6, TimeUnit.MINUTES));

                    response.status(201);
                    String projectIdStarted = result.getProject().getIdentifier();
                    return projectIdStarted;
                } else {
                    halt(409, "Project already exist.");
                }
            } else {
                halt(403, "You have not right to start project configuration id " + projectConfigurationId + ".");
            }
            return "";
        }));

        get(BASE_API + "/project/:id", JSON_CONTENT_TYPE, ((request, response) -> {

            User requester = getRequester(request);
            String projectId = request.params(":id");
            Project project = projectFetcher.getProjectByIdentifier(projectId);
            if (project == null) {
                halt(404);
                return "";
            }
            ProjectConfiguration projectConfiguration = projectFetcher.getProjectConfigurationById(project.getProjectConfigurationIdentifier());
            if (userIsAdmin(requester, projectConfiguration)) {
                //LOGGER.debug("Retrieve project following project {}", project);
                ProjectDto projectDto = new ProjectDto(project);
                //LOGGER.debug("Sending project Ddto {}", projectDto);
                return projectDto;
            } else {
                halt(403, "You have not right to lookup project id " + projectId + ".");
            }

            return "";
        }), jsonResponseTransformer);
    }

    private static boolean userIsAdmin(User user, ProjectConfiguration projectConfiguration) {
        List<User> users = IteratorUtils.toList(projectConfiguration.getAdmins());
        return users.stream().filter(u -> u.getIdentifier().equals(user.getIdentifier())).findFirst().isPresent();
    }


}

