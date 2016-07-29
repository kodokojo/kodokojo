package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import io.kodokojo.service.repository.ProjectRepository;

public class ProjectEndpointActor extends AbstractActor {

    public static Props PROPS(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        return Props.create(ProjectEndpointActor.class, projectRepository);
    }

    private final ProjectRepository projectRepository;

    public ProjectEndpointActor(ProjectRepository projectRepository) {
        if (projectRepository == null) {
            throw new IllegalArgumentException("projectRepository must be defined.");
        }
        this.projectRepository = projectRepository;
        
    }

}
