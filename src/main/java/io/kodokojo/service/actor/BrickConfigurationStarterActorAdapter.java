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

