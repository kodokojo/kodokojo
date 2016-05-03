package io.kodokojo.brick;

import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.ProjectConfiguration;

import static org.apache.commons.lang.StringUtils.isBlank;

public interface BrickUrlFactory {

    String forgeUrl(String entity, String projectName, String brickName);

    default String forgeUrl(String projectName, String brickName) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(brickName)) {
            throw new IllegalArgumentException("brickName must be defined.");
        }
        return forgeUrl(null, projectName.toLowerCase(), brickName.toLowerCase());
    }

    default String forgeUrl(ProjectConfiguration projectConfiguration, BrickConfiguration brickConfiguration) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        return forgeUrl(projectConfiguration.getName().toLowerCase(), brickConfiguration.getType().name().toLowerCase());
    }

}
