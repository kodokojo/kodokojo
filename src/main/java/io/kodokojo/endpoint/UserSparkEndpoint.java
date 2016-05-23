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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.commons.utils.RSAUtils;
import io.kodokojo.endpoint.dto.UserDto;
import io.kodokojo.endpoint.dto.UserProjectConfigIdDto;
import io.kodokojo.model.Entity;
import io.kodokojo.model.User;
import io.kodokojo.service.EmailSender;
import io.kodokojo.service.store.EntityStore;
import io.kodokojo.service.store.ProjectStore;
import io.kodokojo.service.store.UserStore;
import io.kodokojo.service.authentification.SimpleCredential;
import io.kodokojo.endpoint.dto.UserCreationDto;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static spark.Spark.*;

public class UserSparkEndpoint extends AbstractSparkEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSparkEndpoint.class);

    private final UserStore userStore;

    private final ProjectStore projectStore;

    private final EntityStore entityStore;

    private final EmailSender emailSender;

    @Inject
    public UserSparkEndpoint(UserAuthenticator<SimpleCredential> userAuthenticator, EntityStore entityStore, UserStore userStore, ProjectStore projectStore, EmailSender emailSender) {
        super(userAuthenticator);
        if (userStore == null) {
            throw new IllegalArgumentException("userStore must be defined.");
        }
        if (entityStore == null) {
            throw new IllegalArgumentException("entityStore must be defined.");
        }
        if (projectStore == null) {
            throw new IllegalArgumentException("projectStore must be defined.");
        }
        this.userStore = userStore;
        this.entityStore = entityStore;
        this.projectStore = projectStore;
        this.emailSender = emailSender;
    }

    @Override
    public void configure() {
        post(BASE_API + "/user/:id", JSON_CONTENT_TYPE, ((request, response) -> {
            String identifier = request.params(":id");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Try to create user with id {}", identifier);
            }
            if (userStore.identifierExpectedNewUser(identifier)) {
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(request.body());
                String email = json.getAsJsonPrimitive("email").getAsString();

                String username = email.substring(0, email.lastIndexOf("@"));
                User userByUsername = userStore.getUserByUsername(username);
                if (userByUsername != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Trying to create user {} from email '{}' who already exist.", username, email);
                    }
                    halt(409);
                    return "";
                }

                String entityName = email;
                if (json.has("entity") && StringUtils.isNotBlank(json.getAsJsonPrimitive("entity").getAsString())) {
                    entityName = json.getAsJsonPrimitive("entity").getAsString();
                }

                String password = new BigInteger(130, new SecureRandom()).toString(32);
                KeyPair keyPair = RSAUtils.generateRsaKeyPair();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

                User user = new User(identifier, username, username, email, password, RSAUtils.encodePublicKey((RSAPublicKey) keyPair.getPublic(), email));

                String entityId = null;
                SimpleCredential credential = extractCredential(request);
                if (credential != null) {
                    User userRequester = userAuthenticator.authenticate(credential);
                    if (userRequester != null) {
                        entityId = entityStore.getEntityIdOfUserId(userRequester.getIdentifier());
                    }
                }
                if (entityId == null) {
                    Entity entity = new Entity(entityName, user);
                    entityId = entityStore.addEntity(entity);
                }
                entityStore.addUserToEntity(identifier, entityId);

                user = new User(identifier, entityId, username, username, email, password, user.getSshPublicKey());

                if (userStore.addUser(user)) {

                    response.status(201);
                    StringWriter sw = new StringWriter();
                    RSAUtils.writeRsaPrivateKey(privateKey, sw);
                    response.header("Location", "/user/" + user.getIdentifier());
                    UserCreationDto userCreationDto = new UserCreationDto(user, sw.toString());

                    if (emailSender != null) {
                        List<String> cc = null;
                        if (credential != null) {
                            User userRequester = userAuthenticator.authenticate(credential);
                            if (userRequester != null) {
                                cc = Collections.singletonList(userRequester.getEmail());
                            }
                        }
                        String content = "<h1>Welcome on Kodo Kojo</h1>\n" +
                                "<p>You will find all information which is bind to your account '" + userCreationDto.getUsername() + "'.</p>\n" +
                                "\n" +
                                "<p>Password : <b>" + userCreationDto.getPassword() + "</b></p>\n" +
                                "<p>Your SSH private key generated:\n" +
                                "<br />\n" +
                                userCreationDto.getPrivateKey() + "\n" +
                                "</p>\n" +
                                "<p>Your SSH public key generated:\n" +
                                "<br />\n" +
                                userCreationDto.getSshPublicKey() + "\n" +
                                "</p>";
                        emailSender.send(Collections.singletonList(userCreationDto.getEmail()), null, cc, "User creation on Kodo Kojo " + userCreationDto.getName(), content, true);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Mail with user data send to {}.", userCreationDto.getEmail());
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace("Email to {} content : \n {}", userCreationDto.getEmail(), content);
                            }
                        }
                    }

                    return userCreationDto;
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The UserStore not abel to add following user {}.", user.toString());
                }
                halt(428);
                return "";
            } else {
                halt(412);
                return "";
            }
        }), jsonResponseTransformer);

        post(BASE_API + "/user", JSON_CONTENT_TYPE, (request, response) -> {
            String res = userStore.generateId();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Generate id : {}", res);
            }
            return res;
        });

        get(BASE_API + "/user", JSON_CONTENT_TYPE, (request, response) -> {
            SimpleCredential credential = extractCredential(request);
            if (credential != null) {
                User user = userStore.getUserByUsername(credential.getUsername());
                if (user == null) {
                    halt(404);
                    return "";
                }
                return getUserDto(user);
            }
            halt(401);
            return "";
        }, jsonResponseTransformer);

        get(BASE_API + "/user/:id", JSON_CONTENT_TYPE, (request, response) -> {
            SimpleCredential credential = extractCredential(request);
            String identifier = request.params(":id");
            User requestUser = userStore.getUserByUsername(credential.getUsername());
            User user = userStore.getUserByIdentifier(identifier);
            if (user != null) {
                if (user.getEntityIdentifier().equals(requestUser.getEntityIdentifier())) {
                    if (!user.getUsername().equals(credential.getUsername())) {
                        user = new User(user.getIdentifier(), user.getName(), user.getUsername(), user.getEmail(), "", user.getSshPublicKey());
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

    private UserDto getUserDto(User user) {
        UserDto res = new UserDto(user);
        Set<String> projectConfigIds = projectStore.getProjectConfigIdsByUserIdentifier(user.getIdentifier());
        List<UserProjectConfigIdDto> userProjectConfigIdDtos = new ArrayList<>();
        projectConfigIds.forEach(id -> {
            String projectId = projectStore.getProjectIdByProjectConfigurationId(id);
            UserProjectConfigIdDto userProjectConfigIdDto = new UserProjectConfigIdDto(id);
            userProjectConfigIdDto.setProjectId(projectId);
            userProjectConfigIdDtos.add(userProjectConfigIdDto);
        });
        res.setProjectConfigurationIds(userProjectConfigIdDtos);
        return res;
    }
}
