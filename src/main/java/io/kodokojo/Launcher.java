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

    //  WebSocket is built by Spark but we are not able to get the instance :/ .
    //  See : https://github.com/perwendel/spark/pull/383
    public static Injector INJECTOR;

    public static void main(String[] args) {

        LOGGER.info("Starting Kodo Kojo.");

        INJECTOR = Guice.createInjector(new PropertyModule(args), new SecurityModule(), new RedisModule(), new ServiceModule());

        ApplicationLifeCycleManager applicationLifeCycleManager = INJECTOR.getInstance(ApplicationLifeCycleManager.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                LOGGER.info("Stopping services.");
                applicationLifeCycleManager.stop();
                LOGGER.info("All services stopped.");
            }
        });

        RestEntrypoint restEntrypoint = INJECTOR.getInstance(RestEntrypoint.class);
        restEntrypoint.start();

        LOGGER.info("Kodo Kojo started.");

    }

}
