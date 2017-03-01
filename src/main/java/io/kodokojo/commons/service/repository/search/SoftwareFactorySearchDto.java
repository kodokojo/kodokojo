package io.kodokojo.commons.service.repository.search;

import io.kodokojo.commons.model.Organisation;
import io.kodokojo.commons.model.ProjectConfiguration;
import io.kodokojo.commons.service.elasticsearch.DataIdProvider;

import java.io.Serializable;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class SoftwareFactorySearchDto implements DataIdProvider {

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
