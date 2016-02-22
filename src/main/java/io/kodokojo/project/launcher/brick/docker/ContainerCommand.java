package io.kodokojo.project.launcher.brick.docker;

/*
 * #%L
 * project-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPorts;
import io.kodokojo.commons.docker.model.ImageName;
import io.kodokojo.commons.utils.docker.DockerSupport;

public abstract class ContainerCommand {

    protected final ImageName imageName;

    protected final CreateContainerCmd createContainerCmd;

    protected final ExposedPorts exposedPorts;

    protected final long startTimeout;

    public ContainerCommand(ImageName imageName, CreateContainerCmd createContainerCmd, ExposedPorts exposedPorts,  long startTimeout) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        if (createContainerCmd == null) {
            throw new IllegalArgumentException("createContainerCmd must be defined.");
        }
        this.imageName = imageName;
        this.createContainerCmd = createContainerCmd;
        this.exposedPorts = exposedPorts;
        this.startTimeout = startTimeout;
    }

    public CreateContainerCmd getCreateContainerCmd() {
        return createContainerCmd;
    }

    public abstract String createHealthUrl(DockerSupport dockerSupport, String containerId);

    public ImageName getImageName() {
        return imageName;
    }

    public long getStartTimeout() {
        return startTimeout;
    }

    public ExposedPorts getExposedPorts() {
        return exposedPorts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContainerCommand that = (ContainerCommand) o;

        if (!imageName.equals(that.imageName)) return false;
        if (!createContainerCmd.equals(that.createContainerCmd)) return false;
        return exposedPorts.equals(that.exposedPorts);

    }

    @Override
    public int hashCode() {
        int result = imageName.hashCode();
        result = 31 * result + createContainerCmd.hashCode();
        result = 31 * result + exposedPorts.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ContainerCommand{" +
                "imageName=" + imageName +
                ", createContainerCmd=" + createContainerCmd +
                ", exposedPorts=" + exposedPorts +
                ", startTimeout=" + startTimeout +
                '}';
    }
}
