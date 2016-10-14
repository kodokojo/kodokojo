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
package io.kodokojo.endpoint;

import akka.actor.ActorRef;
import akka.util.Timeout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.name.Named;
import io.kodokojo.endpoint.dto.UserCreationDto;
import io.kodokojo.endpoint.dto.UserDto;
import io.kodokojo.endpoint.dto.UserProjectConfigIdDto;
import io.kodokojo.model.User;
import io.kodokojo.model.UserBuilder;
import io.kodokojo.service.RSAUtils;
import io.kodokojo.service.ReCaptchaService;
import io.kodokojo.service.actor.EndpointActor;
import io.kodokojo.service.actor.user.UserCreatorActor;
import io.kodokojo.service.actor.user.UserEligibleActor;
import io.kodokojo.service.actor.user.UserGenerateIdentifierActor;
import io.kodokojo.service.actor.user.UserMessage;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.service.repository.ProjectFetcher;
import io.kodokojo.service.repository.ProjectRepository;
import io.kodokojo.service.repository.UserFetcher;
import io.kodokojo.service.repository.UserRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import spark.Request;

import javax.inject.Inject;
import java.io.StringWriter;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static spark.Spark.*;

public class UserSparkEndpoint extends AbstractSparkEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSparkEndpoint.class);

    private final ActorRef akkaEndpoint;

    private final UserFetcher userFetcher;

    private final ProjectFetcher projectFetcher;

    private final ReCaptchaService reCaptchaService;

    @Inject
    public UserSparkEndpoint(UserAuthenticator<SimpleCredential> userAuthenticator, @Named(EndpointActor.NAME) ActorRef akkaEndpoint, UserRepository userFetcher, ProjectRepository projectFetcher, ReCaptchaService reCaptchaService) {
        super(userAuthenticator);
        if (akkaEndpoint == null) {
            throw new IllegalArgumentException("akkaEndpoint must be defined.");
        }
        if (userFetcher == null) {
            throw new IllegalArgumentException("userFetcher must be defined.");
        }
        if (projectFetcher == null) {
            throw new IllegalArgumentException("projectFetcher must be defined.");
        }
        if (reCaptchaService == null) {
            throw new IllegalArgumentException("reCaptchaService must be defined.");
        }
        this.akkaEndpoint = akkaEndpoint;
        this.userFetcher = userFetcher;
        this.projectFetcher = projectFetcher;
        this.reCaptchaService = reCaptchaService;
    }

    @Override
    public void configure() {
        patch(BASE_API + "/user/:id", JSON_CONTENT_TYPE, ((request, response) -> {
            String identifier = request.params(":id");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to update user with id {}", identifier);
            }

            User requester = getRequester(request);
            User user = userFetcher.getUserByIdentifier(identifier);

            if (requester.getIdentifier().equals(user.getIdentifier())) {

                UserBuilder builder = new UserBuilder(user);

                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(request.body());
                String email = readStringFromJson(json, "email").orElse("");
                String name = readStringFromJson(json, "name").orElse("");
                String password = readStringFromJson(json, "password").orElse("");
                String sshPublicKey = readStringFromJson(json, "sshPublicKey").orElse("");

                builder.setPassword(password).setSshPublicKey(sshPublicKey);

                FiniteDuration duration = Duration.apply(2, TimeUnit.SECONDS);
                Future<Object> future = ask(akkaEndpoint, new UserMessage.UserUpdateMessage(requester, builder.build(), password, sshPublicKey), new Timeout(duration));
                Object result = Await.result(future, duration);
                if (result instanceof UserMessage.UserUpdateMessageResult) {
                    UserMessage.UserUpdateMessageResult msgResult = (UserMessage.UserUpdateMessageResult) result;
                    if (msgResult.isSuccess()) {
                        halt(200);
                    } else {
                        halt(500, "Unable to update user " + user.getUsername() + ".");
                    }
                } else {
                    halt(500, "Unable to update User");
                }

            } else {
                halt(403, "Your aren't allow to update this user.");
            }

            return "";
        }), jsonResponseTransformer);

        post(BASE_API + "/user/:id", JSON_CONTENT_TYPE, ((request, response) -> {
            String identifier = request.params(":id");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to create user with id {}", identifier);
            }

            User requester = getRequester(request);
            if (!validateThrowCaptcha(request, requester)) return "";


            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(request.body());
            String email = json.getAsJsonPrimitive("email").getAsString();
            String username = email.substring(0, email.lastIndexOf("@"));

            String entityId = "";
            if (requester != null) {
                entityId = requester.getEntityIdentifier();
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Create a new User with a new Entity");
            }


            FiniteDuration duration = Duration.apply(30, TimeUnit.SECONDS);
            Future<Object> userCreationFuture = ask(
                    akkaEndpoint,
                    new UserCreatorActor.UserCreateMsg(requester, identifier, email, username, entityId),
                    new Timeout(duration));
            Object result = Await.result(userCreationFuture, duration);

            if (result == null) {
                String format = "An unexpected error occur while trying to create user %s.";
                String errorMessage = String.format(format, username);
                LOGGER.error(errorMessage);
                halt(500, errorMessage);
            } else if (result instanceof UserEligibleActor.UserEligibleResultMsg) {
                UserEligibleActor.UserEligibleResultMsg msg = (UserEligibleActor.UserEligibleResultMsg) result;
                if (!msg.isValid) {
                    halt(428, "Identifier or username are not valid.");
                    return "";
                }
            } else if (result instanceof UserCreatorActor.UserCreateResultMsg) {

                UserCreatorActor.UserCreateResultMsg userCreateResultMsg = (UserCreatorActor.UserCreateResultMsg) result;

                User user = userCreateResultMsg.getUser();
                response.status(201);
                StringWriter sw = new StringWriter();
                response.header("Location", "/user/" + user.getIdentifier());
                RSAUtils.writeRsaPrivateKey((RSAPrivateKey) userCreateResultMsg.getKeyPair().getPrivate(), sw);
                UserCreationDto userCreationDto = new UserCreationDto(user, sw.toString());
                return userCreationDto;
            }
            halt(500, "An unexpected behaviour happened while trying to create user " + username + ".");
            return "";
        }), jsonResponseTransformer);

        post(BASE_API + "/user", JSON_CONTENT_TYPE, (request, response) -> {

            FiniteDuration duration = Duration.apply(30, TimeUnit.SECONDS);
            Future<Object> future = ask(akkaEndpoint, new UserGenerateIdentifierActor.UserGenerateIdentifierMsg(), new Timeout(duration));

            Object result = Await.result(future, duration);
            if (result instanceof UserGenerateIdentifierActor.UserGenerateIdentifierResultMsg) {
                UserGenerateIdentifierActor.UserGenerateIdentifierResultMsg msg = (UserGenerateIdentifierActor.UserGenerateIdentifierResultMsg) result;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Generate id : {}", msg.getGenerateId());
                }
                return msg.getGenerateId();
            }
            halt(500, "An unexpected error occur while trying to generate a new user Id.");
            return "";
        });

        get(BASE_API + "/user", JSON_CONTENT_TYPE, (request, response) -> {
            User requester = getRequester(request);
            return getUserDto(requester);
        }, jsonResponseTransformer);

        get(BASE_API + "/user/:id", JSON_CONTENT_TYPE, (request, response) -> {
            User requester = getRequester(request);
            String identifier = request.params(":id");
            User user = userFetcher.getUserByIdentifier(identifier);
            if (user != null) {
                if (user.getEntityIdentifier().equals(requester.getEntityIdentifier())) {
                    if (!user.equals(requester)) {
                        user = new User(user.getIdentifier(), user.getEntityIdentifier(), user.getName(), user.getUsername(), user.getEmail(), "", user.getSshPublicKey());
                    }
                    return getUserDto(user);
                }
                halt(403, "You aren't in same entity.");
                return "";
            }
            halt(404);
            return "";
        }, jsonResponseTransformer);
    }

    private boolean validateThrowCaptcha(Request request, User requester) {
        if (requester == null) {
            if (reCaptchaService.isConfigured()) {
                String captcha = request.headers("g-recaptcha-response");
                if (StringUtils.isBlank(captcha)) {
                    halt(428, "Unable to retrieve a valid user or Captcha.");
                    return false;
                } else if (reCaptchaService.validToken(captcha, request.ip())) {

                    return true;
                }
                halt(428, "Unable to retrieve a validate captcha.");
                return false;

            } else {
                LOGGER.warn("No Captcha configured, request not block until reCaptcha.secret isn't configured.");
                return true;
            }
        }
        return true;
    }

    private UserDto getUserDto(User user) {
        UserDto res = new UserDto(user);
        Set<String> projectConfigIds = projectFetcher.getProjectConfigIdsByUserIdentifier(user.getIdentifier());
        List<UserProjectConfigIdDto> userProjectConfigIdDtos = new ArrayList<>();
        projectConfigIds.forEach(id -> {
            String projectId = projectFetcher.getProjectIdByProjectConfigurationId(id);
            UserProjectConfigIdDto userProjectConfigIdDto = new UserProjectConfigIdDto(id);
            userProjectConfigIdDto.setProjectId(projectId);
            userProjectConfigIdDtos.add(userProjectConfigIdDto);
        });
        res.setProjectConfigurationIds(userProjectConfigIdDtos);
        return res;
    }
}
