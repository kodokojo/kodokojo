/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
package io.kodokojo.commons.model;

import io.kodokojo.commons.config.properties.Key;

import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;

public class ServiceInfo implements Serializable {

    private final String name;

    private final String uuid;

    private final String version;

    private final String gitSha1;

    private final String branch;

    public ServiceInfo(String name, String uuid, String version, String gitSha1, String branch) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(uuid)) {
            throw new IllegalArgumentException("uuid must be defined.");
        }
        if (isBlank(version)) {
            throw new IllegalArgumentException("version must be defined.");
        }
        if (isBlank(gitSha1)) {
            throw new IllegalArgumentException("gitSha1 must be defined.");
        }
        if (isBlank(branch)) {
            throw new IllegalArgumentException("branch must be defined.");
        }
        this.name = name;
        this.uuid = uuid;
        this.version = version;
        this.gitSha1 = gitSha1;
        this.branch = branch;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getVersion() {
        return version;
    }

    public String getGitSha1() {
        return gitSha1;
    }

    public String getBranch() {
        return branch;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", version='" + version + '\'' +
                ", gitSha1='" + gitSha1 + '\'' +
                ", branch='" + branch + '\'' +
                '}';
    }
}
