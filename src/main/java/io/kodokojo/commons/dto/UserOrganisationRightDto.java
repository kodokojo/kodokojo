package io.kodokojo.commons.dto;

import io.kodokojo.commons.model.Organisation;
import io.kodokojo.commons.model.User;
import io.kodokojo.commons.service.repository.OrganisationFetcher;
import io.kodokojo.commons.service.repository.ProjectFetcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class UserOrganisationRightDto implements Serializable {

    private String identifier;

    private String name;

    private Right right;

    private List<UserProjectConfigurationRightDto> projectConfigurations;

    private int nbUserTotal;

    private int nbProjectTotal;

    public enum Right {
        ADMIN,
        USER
    }

    public UserOrganisationRightDto() {
        super();
        projectConfigurations = new ArrayList<>();
    }

    public UserOrganisationRightDto(String identifier, String name, Right right, List<UserProjectConfigurationRightDto> projectConfigurations) {
        this.identifier = identifier;
        this.name = name;
        this.right = right;
        this.projectConfigurations = projectConfigurations;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Right getRight() {
        return right;
    }

    public void setRight(Right right) {
        this.right = right;
    }

    public List<UserProjectConfigurationRightDto> getProjectConfigurations() {
        return projectConfigurations;
    }

    public void setProjectConfigurations(List<UserProjectConfigurationRightDto> projectConfigurations) {
        this.projectConfigurations = projectConfigurations;
    }

    public int getNbUserTotal() {
        return nbUserTotal;
    }

    public void setNbUserTotal(int nbUserTotal) {
        this.nbUserTotal = nbUserTotal;
    }

    public int getNbProjectTotal() {
        return nbProjectTotal;
    }

    public void setNbProjectTotal(int nbProjectTotal) {
        this.nbProjectTotal = nbProjectTotal;
    }

    @Override
    public String toString() {
        return "UserOrganisationRightDto{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", right=" + right +
                ", nbUserTotal=" + nbUserTotal +
                ", nbProjectTotal=" + nbProjectTotal +
                ", projectConfigurations=" + projectConfigurations +
                '}';
    }

    public static List<UserOrganisationRightDto> computeUserOrganisationRights(User user, OrganisationFetcher organisationFetcher, ProjectFetcher projectFetcher) {
        return user.getOrganisationIds().stream()
                .map(organisationId -> computeUserOrganisationRightDto(user, organisationId, organisationFetcher, projectFetcher))
                .collect(Collectors.toList());
    }

    public static UserOrganisationRightDto computeUserOrganisationRightDto(User user, String entityId, OrganisationFetcher organisationFetcher, ProjectFetcher projectFetcher) {
        Organisation organisation = organisationFetcher.getOrganisationById(entityId);
        UserOrganisationRightDto organisationRightDto = new UserOrganisationRightDto();
        organisationRightDto.setIdentifier(entityId);
        organisationRightDto.setName(organisation.getName());
        if (organisation.userIsAdmin(user.getIdentifier())) {
            organisationRightDto.setRight(UserOrganisationRightDto.Right.ADMIN);
        } else {
            organisationRightDto.setRight(UserOrganisationRightDto.Right.USER);
        }
        final AtomicInteger nbUserTotal = new AtomicInteger(0);
        final AtomicInteger nbProjectTotal = new AtomicInteger(0);
        List<UserProjectConfigurationRightDto> softwareFactories = new ArrayList<>();
        organisation.getProjectConfigurations().forEachRemaining(projectConfiguration -> {
            nbProjectTotal.incrementAndGet();
            nbUserTotal.addAndGet(projectConfiguration.getNbUsers());
            if (projectConfiguration.containAsUser(user)) {
                UserProjectConfigurationRightDto softwareFactoryDto = new UserProjectConfigurationRightDto();
                softwareFactoryDto.setName(projectConfiguration.getName());
                softwareFactoryDto.setIdentifier(projectConfiguration.getIdentifier());
                softwareFactoryDto.setProjectId(projectFetcher.getProjectIdByProjectConfigurationId(projectConfiguration.getEntityIdentifier()));
                if (projectConfiguration.containAsTeamLeader(user)) {
                    softwareFactoryDto.setTeamLeader(true);
                }
                softwareFactories.add(softwareFactoryDto);
            }
        });
        organisationRightDto.setNbUserTotal(nbUserTotal.get());
        organisationRightDto.setNbProjectTotal(nbProjectTotal.get());
        organisationRightDto.setProjectConfigurations(softwareFactories);
        return organisationRightDto;
    }
}
