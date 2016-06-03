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
package io.kodokojo.model;

import static org.apache.commons.lang.StringUtils.isBlank;

public class BootstrapStackData {

    private final String projectName;

    private final String stackName;

    private final String loadBalancerHost;

    private final int sshPort;

    public BootstrapStackData(String projectName, String stackName, String loadBalancerHost, int sshPort) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(stackName)) {
            throw new IllegalArgumentException("stackName must be defined.");
        }
        if (isBlank(loadBalancerHost)) {
            throw new IllegalArgumentException("loadBalancerHost must be defined.");
        }
        this.projectName = projectName;
        this.stackName = stackName;
        this.loadBalancerHost = loadBalancerHost;
        this.sshPort = sshPort;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getStackName() {
        return stackName;
    }

    public String getLoadBalancerHost() {
        return loadBalancerHost;
    }

    public int getSshPort() {
        return sshPort;
    }

    @Override
    public String toString() {
        return "BootstrapStackData{" +
                "projectName='" + projectName + '\'' +
                ", stackName='" + stackName + '\'' +
                ", loadBalancerHost='" + loadBalancerHost + '\'' +
                ", sshPort=" + sshPort +
                '}';
    }
}
