/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.service.repository;

import io.kodokojo.commons.model.*;
import io.kodokojo.commons.service.elasticsearch.ElasticSearchSearcher;
import io.kodokojo.commons.service.repository.search.OrganisationSearchDto;
import io.kodokojo.commons.service.repository.search.SoftwareFactorySearchDto;
import io.kodokojo.commons.service.repository.search.UserSearchDto;
import io.kodokojo.commons.service.repository.store.OrganisationStore;
import io.kodokojo.commons.service.repository.store.OrganisationStoreModel;
import io.kodokojo.commons.service.repository.store.ProjectConfigurationStoreModel;
import io.kodokojo.commons.service.repository.store.ProjectStore;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Repository implements UserRepository, ProjectRepository, OrganisationRepository {

    private final UserRepository userRepository;

    private final UserFetcher userFetcher;

    private final OrganisationStore organisationStore;

    private final ProjectStore projectStore;

    private final ElasticSearchSearcher elasticSearchSearcher;

    @Inject
    public Repository(UserRepository userRepository,
                      UserFetcher userFetcher,
                      OrganisationStore organisationStore,
                      ProjectStore projectStore,
                      ElasticSearchSearcher elasticSearchSearcher
        ) {
        requireNonNull(userRepository, "userRepository must be defined.");
        requireNonNull(userFetcher, "userFetcher must be defined.");
        requireNonNull(organisationStore, "organisationStore must be defined.");
        requireNonNull(projectStore, "projectStore must be defined.");
        this.userRepository = userRepository;
        this.userFetcher = userFetcher;
        this.organisationStore = organisationStore;
        this.projectStore = projectStore;
        this.elasticSearchSearcher = elasticSearchSearcher;
    }

    public Repository(UserRepository userRepository,
                      UserFetcher userFetcher,
                      OrganisationStore organisationStore,
                      ProjectStore projectStore
    ) {
        this(userRepository, userFetcher, organisationStore, projectStore, null);
    }

    @Override
    public String addOrganisation(Organisation organisation) {
        if (organisation == null) {
            throw new IllegalArgumentException("organisation must be defined.");
        }
        OrganisationStoreModel organisationStoreModel = new OrganisationStoreModel(organisation);
        String res = organisationStore.addOrganisation(organisationStoreModel);
        if (elasticSearchSearcher != null) {
            OrganisationSearchDto dto = OrganisationSearchDto.convert(organisation);
            dto.setIdentifier(res);
            elasticSearchSearcher.addOrUpdate(dto);
        }
        return res;
    }

    @Override
    public void addUserToOrganisation(String userIdentifier, String entityIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        if (isBlank(entityIdentifier)) {
            throw new IllegalArgumentException("entityIdentifier must be defined.");
        }
        organisationStore.addUserToOrganisation(userIdentifier, entityIdentifier);
    }

    @Override
    public void addAdminToOrganisation(String userIdentifier, String organisationIdentifier) {
        if (isBlank(userIdentifier)) {
            throw new IllegalArgumentException("userIdentifier must be defined.");
        }
        if (isBlank(organisationIdentifier)) {
            throw new IllegalArgumentException("organisationIdentifier must be defined.");
        }
        organisationStore.addAdminToOrganisation(userIdentifier, organisationIdentifier);
    }

    @Override
    public Organisation getOrganisationById(String organisationIdentifier) {
        if (isBlank(organisationIdentifier)) {
            throw new IllegalArgumentException("organisationIdentifier must be defined.");
        }
        OrganisationStoreModel organisationStoreModel = organisationStore.getOrganisationById(organisationIdentifier);
        List<User> admins = organisationStoreModel.getAdmins().stream().map(userFetcher::getUserByIdentifier).collect(Collectors.toList());
        List<User> users = organisationStoreModel.getUsers().stream().map(userFetcher::getUserByIdentifier).collect(Collectors.toList());
        List<ProjectConfiguration> projectConfiguration = organisationStoreModel.getProjectConfigurations().stream().map(p -> {
            ProjectConfigurationStoreModel model = projectStore.getProjectConfigurationById(p);
            return convertToProjectConfiguration(model);
        }).collect(Collectors.toList());

        return new Organisation(organisationStoreModel.getIdentifier(), organisationStoreModel.getName(), organisationStoreModel.isConcrete(), projectConfiguration, admins, users);
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
        String res = projectStore.addProjectConfiguration(new ProjectConfigurationStoreModel(projectConfiguration));
        if (elasticSearchSearcher != null) {
            SoftwareFactorySearchDto dto = SoftwareFactorySearchDto.convert(projectConfiguration);
            dto.setIdentifier(res);
            elasticSearchSearcher.addOrUpdate(dto);
        }
        return res;
    }

    @Override
    public String addProject(Project project, String projectConfigurationIdentifier) {
        if (project == null) {
            throw new IllegalArgumentException("project must be defined.");
        }
        if (isBlank(projectConfigurationIdentifier)) {
            throw new IllegalArgumentException("projectConfigurationIdentifier must be defined.");
        }
        return projectStore.addProject(project, projectConfigurationIdentifier);
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
        if (elasticSearchSearcher != null) {
            SoftwareFactorySearchDto dto = SoftwareFactorySearchDto.convert(projectConfiguration);
            elasticSearchSearcher.addOrUpdate(dto);
        }
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
    public ProjectConfiguration getProjectConfigurationByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        ProjectConfigurationStoreModel projectConfigurationStoreModel = projectStore.getProjectConfigurationByName(name);
        if (projectConfigurationStoreModel == null) {
            return null;
        }
        return convertToProjectConfiguration(projectConfigurationStoreModel);
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
        boolean res = userRepository.addUser(user);
        if (elasticSearchSearcher != null) {
            UserSearchDto dto = UserSearchDto.convert(user);
            elasticSearchSearcher.addOrUpdate(dto);
        }
        return res;
    }

    @Override
    public boolean addUserToWaitingList(UserInWaitingList userInWaitingList) {
        requireNonNull(userInWaitingList, "userInWaitingList must be defined.");
        return userRepository.addUserToWaitingList(userInWaitingList);
    }

    @Override
    public boolean updateUser(User user) {
        requireNonNull(user, "user must be defined.");
        if (elasticSearchSearcher != null) {
            UserSearchDto dto = UserSearchDto.convert(user);
            elasticSearchSearcher.addOrUpdate(dto);
        }
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
        return new ProjectConfiguration(model.getEntityIdentifier(), model.getIdentifier(), model.getName(), userService, adminsProjectConfig, model.getStackConfigurations(), usersProjectConfig);
    }
}
