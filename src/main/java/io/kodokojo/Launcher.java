package io.kodokojo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.kodokojo.config.module.PropertyModule;
import io.kodokojo.config.module.RedisModule;
import io.kodokojo.config.module.SecurityModule;
import io.kodokojo.config.module.ServiceModule;
import io.kodokojo.entrypoint.RestEntrypoint;
import io.kodokojo.lifecycle.ApplicationLifeCycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {

        LOGGER.info("Starting Kodo Kojo.");

        Injector injector = Guice.createInjector(new PropertyModule(args), new SecurityModule(), new RedisModule(), new ServiceModule());

        ApplicationLifeCycleManager applicationLifeCycleManager = injector.getInstance(ApplicationLifeCycleManager.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                LOGGER.info("Stopping services.");
                applicationLifeCycleManager.stop();
                LOGGER.info("All services stopped.");
            }
        });

        RestEntrypoint restEntrypoint = injector.getInstance(RestEntrypoint.class);
        restEntrypoint.start();

        LOGGER.info("Kodo Kojo started.");

    }

}
