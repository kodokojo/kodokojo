package io.kodokojo.commons.service;

import io.kodokojo.commons.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import static java.util.Objects.requireNonNull;

public class HttpHealCheckEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHealCheckEndpoint.class);
    private final ApplicationConfig applicationConfig;

    public HttpHealCheckEndpoint(ApplicationConfig applicationConfig) {
        requireNonNull(applicationConfig, "applicationConfig must be defined.");
        this.applicationConfig = applicationConfig;
    }

    public void start() {
        Spark.port(applicationConfig.port());
        configure();

        Spark.awaitInitialization();
        LOGGER.info("Spark server started on port {} for healthcheck.", applicationConfig.port());
    }

    public void configure() {
        Spark.get("/healthcheck", (request, response) -> {
            return "ok";
        });
    }
}
