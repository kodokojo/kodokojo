package io.kodokojo.commons.service.healthcheck;

import io.kodokojo.commons.config.ApplicationConfig;
import io.kodokojo.commons.service.lifecycle.ApplicationLifeCycleListener;
import io.kodokojo.commons.spark.JsonTransformer;
import io.kodokojo.commons.spark.SparkEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static spark.Spark.get;

public class HttpHealthCheckEndpoint implements SparkEndpoint, ApplicationLifeCycleListener {

    public static final String HEALTHCHECK_PATH = "/healthcheck";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHealthCheckEndpoint.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final ApplicationConfig applicationConfig;

    private final Set<CachedHealthChecker> healthCheckers;

    @Inject
    public HttpHealthCheckEndpoint(ApplicationConfig applicationConfig, Set<HealthChecker> healthCheckers) {
        requireNonNull(applicationConfig, "applicationConfig must be defined.");
        requireNonNull(healthCheckers, "healthCheckers must be defined.");
        this.applicationConfig = applicationConfig;
        this.healthCheckers = healthCheckers.stream()
                .map(CachedHealthChecker::new)
                .collect(Collectors.toSet());
    }

    @Override
    public void start() {
        Spark.port(applicationConfig.port());
        configure();

        Spark.awaitInitialization();
        LOGGER.info("Spark server started on port {} for healthCheck.", applicationConfig.port());
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping HttpHealthCheckEndpoint.");
        Spark.stop();
    }

    @Override
    public void configure() {
        get(HEALTHCHECK_PATH, JSON_CONTENT_TYPE, (request, response) -> {
            response.type(JSON_CONTENT_TYPE);
            boolean healthy = true;
            Set<HealthCheck> healthChecks = new HashSet<>();
            for (HealthChecker healthChecker : healthCheckers) {
                HealthCheck check = healthChecker.check();
                healthChecks.add(check);
                if (healthy && check.getState() != HealthCheck.State.OK) {
                    healthy = false;
                }
            }
            if (!healthy) {
                response.status(500);
            }
            return healthChecks;
        }, new JsonTransformer());
    }

}
