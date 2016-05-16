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
package io.kodokojo.service.actor;

import akka.actor.ActorRef;
import io.kodokojo.brick.BrickConfigurationStarter;
import io.kodokojo.brick.BrickStartContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class BrickConfigurationStarterActorAdapter implements BrickConfigurationStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickConfigurationStarterActorAdapter.class);

    private final ActorRef starter;

    @Inject
    public BrickConfigurationStarterActorAdapter(ActorRef starter) {
        if (starter == null) {
            throw new IllegalArgumentException("starter must be defined.");
        }
        this.starter = starter;

    }

    @Override
    public void start(BrickStartContext brickStartContext) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Tell to actor to start brick {} for project {}", brickStartContext.getBrickConfiguration().getBrick().getName(), brickStartContext.getProjectConfiguration().getName());
        }
        starter.tell(brickStartContext, ActorRef.noSender());
    }
}

