package io.kodokojo.commons.dto;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class OrganisationLightDto implements Serializable {

    private final String identifier;

    private final String name;

    public OrganisationLightDto(String identifier, String name) {
        if (isBlank(identifier)) {
            throw new IllegalArgumentException("identifier must be defined.");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        this.identifier = identifier;
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "OrganisationLightDto{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
