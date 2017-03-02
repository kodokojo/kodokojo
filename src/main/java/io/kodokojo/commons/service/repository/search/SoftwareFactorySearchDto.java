package io.kodokojo.commons.service.repository.search;

import io.kodokojo.commons.model.ProjectConfiguration;
import io.kodokojo.commons.service.elasticsearch.DataIdProvider;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class SoftwareFactorySearchDto implements DataIdProvider {

    private static final String SOFTWAREFACTORY = "softwarefactory";

    private String identifier;

    private String name;

    public SoftwareFactorySearchDto() {
        super();
    }

    public static SoftwareFactorySearchDto convert(ProjectConfiguration projectConfiguration) {
        requireNonNull(projectConfiguration, "projectConfiguration must be defined.");
        return converter().apply(projectConfiguration);
    }

    public static Function<ProjectConfiguration, SoftwareFactorySearchDto> converter() {
        return projectConfiguration -> {
            SoftwareFactorySearchDto res = new SoftwareFactorySearchDto();
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
        return SOFTWAREFACTORY;
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
