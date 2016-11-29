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
package io.kodokojo.service.repository;

import io.kodokojo.commons.model.*;
import io.kodokojo.service.repository.store.EntityStore;
import io.kodokojo.service.repository.store.EntityStoreModel;
import io.kodokojo.service.repository.store.ProjectConfigurationStoreModel;
import io.kodokojo.service.repository.store.ProjectStore;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Repository implements UserRepository, ProjectRepository, EntityRepository {

    private final UserRepository userRepository;

    private final UserFetcher userFetcher;

    private final EntityStore entityStore;

    private final ProjectStore projectStore;

    @Inject
    public Repository(UserRepository userRepository, UserFetcher userFetcher, EntityStore entityStore, ProjectStore projectStore) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        if (userFetcher == null) {
            throw new IllegalArgumentException("userFetcher must be defined.");
        }
        if (entityStore == null) {
            throw new IllegalArgumentException("entityStore must be defined.");
        }
        if (projectStore == null) {
            throw new IllegalArgumentException("projectStore must be defined.");
        }
        this.userRepository = userRepository;
        this.userFetcher = userFetcher;
        this.entityStore = entityStore;
        this.projectStore = projectStore;
    }

    @Override
    public String addEntity(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must be defined.");
        }
        EntityStoreModel entityStoreModel = new EntityStoreModel(entity);
        return entityStore.addEntity(entityStoreModel);
    }

    @Override
    public void addUserToEntity(String userIdentifier, String entityIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        entityStore.addUserToEntity(userIdentifier,entityIdentifier);
    }

    @Override
    public Entity getEntityById(String entityIdentifier) {
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        EntityStoreModel entityStoreModel = entityStore.getEntityById(entityIdentifier);
        List<User> admins = entityStoreModel.getAdmins().stream().map(userFetcher::getUserByIdentifier).collect(Collectors.toList());
        List<User> users = entityStoreModel.getUsers().stream().map(userFetcher::getUserByIdentifier).collect(Collectors.toList());
        List<ProjectConfiguration> projectConfiguration = entityStoreModel.getProjectConfigurations().stream().map(p -> {
            ProjectConfigurationStoreModel model = projectStore.getProjectConfigurationById(p);
            return convertToProjectConfiguration(model);
        }).collect(Collectors.toList());

        return new Entity(entityStoreModel.getIdentifier(), entityStoreModel.getName(), entityStoreModel.isConcrete(),projectConfiguration, admins, users);
    }

    @Override
    public boolean projectNameIsValid(String projectName) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        return projectStore.projectNameIsValid(projectName);
    }

    @Override
    public String addProjectConfiguration(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        return projectStore.addProjectConfiguration(new ProjectConfigurationStoreModel(projectConfiguration));
    }

    @Override
    public String addProject(Project project, String projectConfigurationIdentifier) {
        if (project == null) {
            throw new IllegalArgumentException("project must be defined.");
        }
        if (isBlank(projectConfigurationIdentifier)) {
            throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
        }
        return projectStore.addProject(project,  projectConfigurationIdentifier);
    }

    @Override
    public void updateProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("project must be defined.");
        }
        projectStore.updateProject(project);
    }

    @Override
    public void updateProjectConfiguration(ProjectConfiguration projectConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        projectStore.updateProjectConfiguration(new ProjectConfigurationStoreModel(projectConfiguration));
    }

    @Override
    public ProjectConfiguration getProjectConfigurationById(String identifier) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        ProjectConfigurationStoreModel projectConfiguration = projectStore.getProjectConfigurationById(identifier);
        if (projectConfiguration == null) {
            return null;
        }
        return convertToProjectConfiguration(projectConfiguration);
    }

    @Override
    public Project getProjectByIdentifier(String identifier) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        return projectStore.getProjectByIdentifier(identifier);
    }

    @Override
    public Set<String> getProjectConfigIdsByUserIdentifier(String userIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        return projectStore.getProjectConfigIdsByUserIdentifier(userIdentifier);
    }

    @Override
    public String getProjectIdByProjectConfigurationId(String projectConfigurationId) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        return projectStore.getProjectIdByProjectConfigurationId(projectConfigurationId);
    }

    @Override
    public Project getProjectByProjectConfigurationId(String projectConfigurationId) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        return projectStore.getProjectByProjectConfigurationId(projectConfigurationId);
    }

    @Override
    public String generateId() {
        return userRepository.generateId();
    }

    @Override
    public boolean identifierExpectedNewUser(String generatedId) {
        if (isBlank(generatedId)) {
            throw new IllegalArgumentException("generatedId must be defined.");
        }
        return userRepository.identifierExpectedNewUser(generatedId);
    }

    @Override
    public boolean addUser(User user) {
        return userRepository.addUser(user);
    }

    @Override
    public boolean addUserToWaitingList( UserInWaitingList userInWaitingList ) {
        requireNonNull(userInWaitingList, "userInWaitingList must be defined.");
        return userRepository.addUserToWaitingList(userInWaitingList);
    }

    @Override
    public boolean updateUser(User user) {
        requireNonNull(user, "user must be defined.");
        return userRepository.updateUser(user);
    }

    @Override
    public boolean addUserService(UserService userService) {
        return userRepository.addUserService(userService);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    @Override
    public User getUserByIdentifier(String identifier) {
        return userRepository.getUserByIdentifier(identifier);
    }

    @Override
    public UserService getUserServiceByIdentifier(String identifier) {
        return userRepository.getUserServiceByIdentifier(identifier);
    }

    @Override
    public UserService getUserServiceByName(String name) {
        return userRepository.getUserServiceByName(name);
    }

    private ProjectConfiguration convertToProjectConfiguration(ProjectConfigurationStoreModel model) {
        UserService userService = userFetcher.getUserServiceByIdentifier(model.getUserService());
        List<User> adminsProjectConfig = model.getAdmins().stream().map(
                userFetcher::getUserByIdentifier
        ).collect(Collectors.toList());
        List<User> usersProjectConfig = model.getUsers().stream().map(userFetcher::getUserByIdentifier).collect(Collectors.toList());
        return new ProjectConfiguration(model.getEntityIdentifier(), model.getIdentifier(), model.getName(), userService, adminsProjectConfig,model.getStackConfigurations(),usersProjectConfig);
    }
}
