package io.kodokojo.commons.service.healthcheck;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class CachedHealthChecker implements HealthChecker {

    private final HealthChecker delegate;

    private final long cachedTime;

    private HealthCheck previous;

    public CachedHealthChecker(HealthChecker delegate, long cachedTime) {
        requireNonNull(delegate, "delegate must be defined.");
        this.cachedTime = cachedTime;
        this.delegate = delegate;
    }

    public CachedHealthChecker(HealthChecker delegate) {
        this(delegate, TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
    }

    @Override
    public HealthCheck check() {
        if (previous == null ||
                (previous.getEndDate() + cachedTime) < System.currentTimeMillis()) {
            previous = delegate.check();
        }
        return previous;
    }
}
