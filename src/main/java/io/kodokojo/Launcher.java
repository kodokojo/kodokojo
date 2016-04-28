package io.kodokojo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.kodokojo.config.module.*;
import io.kodokojo.entrypoint.RestEntryPoint;
import io.kodokojo.service.lifecycle.ApplicationLifeCycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    //  WebSocket is built by Spark but we are not able to get the instance :/ .
    //  See : https://github.com/perwendel/spark/pull/383
    public static Injector INJECTOR;

    public static void main(String[] args) {

        LOGGER.info("Starting Kodo Kojo.");

        INJECTOR = Guice.createInjector(new PropertyModule(args),
                new SecurityModule(),
                new RedisModule(),
                new ServiceModule(),
                new ActorModule(),
                new AwsModule(),
                new MarathonModule(),
                new RestEntryPointModule()
        );

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

        RestEntryPoint restEntryPoint = INJECTOR.getInstance(RestEntryPoint.class);
        restEntryPoint.start();

        LOGGER.info("Kodo Kojo started.");

    }

}
