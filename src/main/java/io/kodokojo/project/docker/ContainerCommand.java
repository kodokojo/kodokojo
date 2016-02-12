package io.kodokojo.project.docker;

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
