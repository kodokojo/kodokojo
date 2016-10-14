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
package io.kodokojo.bdd;

import io.kodokojo.brick.BrickUrlFactory;
import io.kodokojo.brick.DefaultBrickUrlFactory;
import io.kodokojo.model.Service;
import io.kodokojo.service.marathon.MarathonServiceLocator;
import org.apache.commons.collections4.CollectionUtils;

import javax.inject.Inject;

import java.util.Set;

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

    public MarathonBrickUrlFactory(io.kodokojo.config.MarathonConfig marathonConfig) {
        if (marathonConfig == null) {
            throw new IllegalArgumentException("marathonConfig must be defined.");
        }
        marathonServiceLocator = new MarathonServiceLocator(marathonConfig);
    }

    @Override
    public String forgeUrl(String entity, String projectName, String stackName, String brickType, String brickName) {
        Set<Service> services = marathonServiceLocator.getService(brickType, projectName);
        if (CollectionUtils.isEmpty(services)) {
            return fallBack.forgeUrl(entity, projectName, stackName, brickName, brickName);
        } else {
            Service service = services.iterator().next();
            return service.getHost() + ":" + service.getPortDefinition().getContainerPort();
        }
    }
}
