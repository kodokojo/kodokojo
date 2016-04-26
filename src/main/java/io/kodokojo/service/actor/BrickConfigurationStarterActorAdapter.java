package io.kodokojo.service.actor;

import akka.actor.ActorRef;
import io.kodokojo.service.BrickConfigurationStarter;
import io.kodokojo.service.BrickStartContext;

import javax.inject.Inject;

public class BrickConfigurationStarterActorAdapter implements BrickConfigurationStarter {

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
        starter.tell(brickStartContext, ActorRef.noSender());
    }
}
