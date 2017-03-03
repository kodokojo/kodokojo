package io.kodokojo.commons.service.repository.search;

import io.kodokojo.commons.model.ProjectConfiguration;
import io.kodokojo.commons.service.elasticsearch.DataIdProvider;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ProjectSearchDto implements DataIdProvider {

    private static final String PROJECT = "project";

    private String identifier;

    private String name;

    public ProjectSearchDto() {
        super();
    }

    public static ProjectSearchDto convert(ProjectConfiguration projectConfiguration) {
        requireNonNull(projectConfiguration, "projectConfiguration must be defined.");
        return converter().apply(projectConfiguration);
    }

    public static Function<ProjectConfiguration, ProjectSearchDto> converter() {
        return projectConfiguration -> {
            ProjectSearchDto res = new ProjectSearchDto();
            res.setIdentifier(projectConfiguration.getIdentifier());
            res.setName(projectConfiguration.getName());
            return res;
        };
    }


    @Override
    public String getId() {
        return identifier;
    }

    @Override
    public String getType() {
        return PROJECT;
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

}
