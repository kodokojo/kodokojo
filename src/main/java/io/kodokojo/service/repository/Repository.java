package io.kodokojo.service.repository;

import io.kodokojo.model.*;
import io.kodokojo.service.repository.store.EntityStore;
import io.kodokojo.service.repository.store.EntityStoreModel;
import io.kodokojo.service.repository.store.ProjectConfigurationStoreModel;
import io.kodokojo.service.repository.store.ProjectStore;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public String getEntityIdOfUserId(String userIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        return entityStore.getEntityIdOfUserId(userIdentifier);
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
    public void setContextToBrickConfiguration(String projectConfigurationId, BrickConfiguration brickConfiguration, Map<String, Serializable> context) {
        if (isBlank(projectConfigurationId)) {
            throw new IllegalArgumentException("projectConfigurationId must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        if (context == null) {
            throw new IllegalArgumentException("context must be defined.");
        }
        projectStore.setContextToBrickConfiguration(projectConfigurationId, brickConfiguration, context);
    }

    @Override
    public ProjectConfiguration getProjectConfigurationById(String identifier) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        return convertToProjectConfiguration(projectStore.getProjectConfigurationById(identifier));
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
    public boolean addUserService(UserService userService) {
        return userRepository.addUserService(userService);
    }

    @Override
    public boolean userIsAdminOfProjectConfiguration(String username, ProjectConfiguration projectConfiguration) {
        return false;
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
    public UserService getUserServiceByName(String name) {
        return userRepository.getUserServiceByName(name);
    }

    private ProjectConfiguration convertToProjectConfiguration(ProjectConfigurationStoreModel model) {
        List<User> adminsProjectConfig = model.getAdmins().stream().map(userFetcher::getUserByIdentifier).collect(Collectors.toList());
        List<User> usersProjectConfig = model.getUsers().stream().map(userFetcher::getUserByIdentifier).collect(Collectors.toList());
        return new ProjectConfiguration(model.getEntityIdentifier(), model.getIdentifier(), model.getName(), adminsProjectConfig,model.getStackConfigurations(),usersProjectConfig);
    }
}
