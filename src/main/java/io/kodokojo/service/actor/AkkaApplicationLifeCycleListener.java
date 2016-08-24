package io.kodokojo.service.actor;

import akka.actor.ActorSystem;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AkkaApplicationLifeCycleListener implements ApplicationLifeCycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaApplicationLifeCycleListener.class);

    private final ActorSystem system;

    public AkkaApplicationLifeCycleListener(ActorSystem system) {
        if (system == null) {
            throw new IllegalArgumentException("system must be defined.");
        }
        this.system = system;
    }

    @Override
    public void start() {
        //  Nothing to do.
    }

    @Override
    public void stop() {
        LOGGER.info("Shutdown Akka system.");
        system.shutdown();
    }
}
