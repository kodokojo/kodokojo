package io.kodokojo.commons.service.healthcheck;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CachedHealthCheckerTest {

    private HealthChecker healthChecker;

    @Before
    public void setup() {
        healthChecker = () -> {
            HealthCheck.Builder builder = new HealthCheck.Builder();
            builder.setName("Health")
                    .setState(HealthCheck.State.OK);
            return builder.build();
        };
    }

    @Test
    public void check_when_cached() throws InterruptedException {

        CachedHealthChecker cachedHealthChecker = new CachedHealthChecker(healthChecker);

        HealthCheck healthCheck = cachedHealthChecker.check();

        Thread.sleep(100);

        HealthCheck healthCheckSecond = cachedHealthChecker.check();

        assertThat(healthCheck).isEqualTo(healthCheckSecond);

    }

    @Test
    public void check_when_cached_then_update() throws InterruptedException {

        CachedHealthChecker cachedHealthChecker = new CachedHealthChecker(healthChecker, 100);

        HealthCheck healthCheck = cachedHealthChecker.check();

        Thread.sleep(10);

        HealthCheck healthCheckSecond = cachedHealthChecker.check();

        assertThat(healthCheck).isEqualTo(healthCheckSecond);

        Thread.sleep(100);

        HealthCheck healthCheckThird = cachedHealthChecker.check();

        assertThat(healthCheckThird.getEndDate()).isGreaterThan(healthCheckSecond.getEndDate());

    }

}