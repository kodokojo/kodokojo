/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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

    private final String domain;

    private final String lbIp;

    public BrickStartContext(ProjectConfiguration projectConfiguration, StackConfiguration stackConfiguration, BrickConfiguration brickConfiguration, String domain, String lbIp) {
        if (projectConfiguration == null) {
            throw new IllegalArgumentException("projectConfiguration must be defined.");
        }
        if (brickConfiguration == null) {
            throw new IllegalArgumentException("brickConfiguration must be defined.");
        }
        if (stackConfiguration == null) {
            throw new IllegalArgumentException("stackConfiguration must be defined.");
        }
        if (isBlank(domain)) {
            throw new IllegalArgumentException("domaine must be defined.");
        }
        if (isBlank(lbIp)) {
            throw new IllegalArgumentException("lbIp must be defined.");
        }
        this.projectConfiguration = projectConfiguration;
        this.stackConfiguration = stackConfiguration;
        this.brickConfiguration = brickConfiguration;
        this.domain = domain;
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

    public String getDomain() {
        return domain;
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
                ", domain='" + domain + '\'' +
                ", lbIp='" + lbIp + '\'' +
                '}';
    }
}
