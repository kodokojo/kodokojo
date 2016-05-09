package io.kodokojo.brick;

import io.kodokojo.commons.utils.ssl.SSLKeyPair;
import io.kodokojo.model.BrickConfiguration;
import io.kodokojo.model.ProjectConfiguration;
import io.kodokojo.model.StackConfiguration;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BrickStartContext implements Serializable {

    private final ProjectConfiguration projectConfiguration;

    private final StackConfiguration stackConfiguration;

    private final BrickConfiguration brickConfiguration;

    private final String domaine;

    private final SSLKeyPair projectCaSSL;

    private final String lbIp;

    public BrickStartContext(ProjectConfiguration projectConfiguration, StackConfiguration stackConfiguration, BrickConfiguration brickConfiguration, String domaine, SSLKeyPair projectCaSSL, String lbIp) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        if (stackConfiguration == null) {
            throw new IllegalArgumentException("stackConfiguration must be defined.");
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
        this.stackConfiguration = stackConfiguration;
        this.brickConfiguration = brickConfiguration;
        this.domaine = domaine;
        this.projectCaSSL = projectCaSSL;
        this.lbIp = lbIp;
    }

    public ProjectConfiguration getProjectConfiguration() {
        return projectConfiguration;
    }

    public StackConfiguration getStackConfiguration() {
        return stackConfiguration;
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
                "stackConfiguration=" + stackConfiguration +
                ", brickConfiguration=" + brickConfiguration +
                ", domaine='" + domaine + '\'' +
                ", projectCaSSL=" + projectCaSSL +
                ", lbIp='" + lbIp + '\'' +
                '}';
    }
}
