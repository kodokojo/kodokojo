package io.kodokojo.service;

import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.ProjectConfiguration;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickStartContext implements Serializable {

    private final ProjectConfiguration projectConfiguration;

    private final BrickConfiguration brickConfiguration;

    private final String domaine;

    private final SSLKeyPair projectCaSSL;

    private final String lbIp;

    public BrickStartContext(ProjectConfiguration projectConfiguration, BrickConfiguration brickConfiguration, String domaine, SSLKeyPair projectCaSSL, String lbIp) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        if (isBlank(domaine)) {
            throw new IllegalArgumentException("domaine must be defined.");
        }
        if (projectCaSSL == null) {
            throw new IllegalArgumentException("projectCaSSL must be defined.");
        }
        if (isBlank(lbIp)) {
            throw new IllegalArgumentException("lbIp must be defined.");
        }
        this.projectConfiguration = projectConfiguration;
        this.brickConfiguration = brickConfiguration;
        this.domaine = domaine;
        this.projectCaSSL = projectCaSSL;
        this.lbIp = lbIp;
    }

    public ProjectConfiguration getProjectConfiguration() {
        return projectConfiguration;
    }

    public BrickConfiguration getBrickConfiguration() {
        return brickConfiguration;
    }

    public String getDomaine() {
        return domaine;
    }

    public SSLKeyPair getProjectCaSSL() {
        return projectCaSSL;
    }

    public String getLbIp() {
        return lbIp;
    }

    @Override
    public String toString() {
        return "BrickStartContext{" +
                "projectConfiguration=" + projectConfiguration +
                ", brickConfiguration=" + brickConfiguration +
                ", domaine='" + domaine + '\'' +
                ", projectCaSSL=" + projectCaSSL +
                ", lbIp='" + lbIp + '\'' +
                '}';
    }
}
