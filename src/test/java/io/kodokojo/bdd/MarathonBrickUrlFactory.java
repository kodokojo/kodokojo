package io.kodokojo.bdd;

import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.brick.DefaultBrickUrlFactory;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.servicelocator.marathon.MarathonServiceLocator;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;

import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class MarathonBrickUrlFactory implements BrickUrlFactory {

    private final BrickUrlFactory fallBack = new DefaultBrickUrlFactory("kodokojo.dev");

    private final MarathonServiceLocator marathonServiceLocator;

    @Inject
    public MarathonBrickUrlFactory(MarathonServiceLocator marathonServiceLocator) {
        if (marathonServiceLocator == null) {
            throw new IllegalArgumentException("marathonServiceLocator must be defined.");
        }
        this.marathonServiceLocator = marathonServiceLocator;
    }

    public MarathonBrickUrlFactory(String marathonUrl) {
        if (isBlank(marathonUrl)) {
            throw new IllegalArgumentException("marathonUrl must be defined.");
        }
        marathonServiceLocator = new MarathonServiceLocator(marathonUrl);
    }

    @Override
    public String forgeUrl(String entity, String projectName, String stackName, String brickType, String brickName) {
        Set<Service> services = marathonServiceLocator.getService(brickType, projectName);
        if (CollectionUtils.isEmpty(services)) {
            return fallBack.forgeUrl(entity, projectName, stackName, brickName, brickName);
        } else {
            Service service = services.iterator().next();
            return service.getHost() + ":" + service.getPort();
        }
    }
}
