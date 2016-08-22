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
package io.kodokojo.service.actor.project;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
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
        receive(ReceiveBuilder.matchAny(this::unhandled).build());
    }

}
