package io.kodokojo.commons.event;

import io.kodokojo.commons.config.MicroServiceConfig;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

public class DefaultEventBuilderFactory implements EventBuilderFactory {

    private static final String FROM_FORMAT = "%s@%s";

    private final String from;

    @Inject
    public DefaultEventBuilderFactory(MicroServiceConfig microServiceConfig) {
        requireNonNull(microServiceConfig, "microServiceConfig must be defined.");
        this.from = String.format(FROM_FORMAT, microServiceConfig.name(), microServiceConfig.uuid());
    }

    @Override
    public EventBuilder create() {
        EventBuilder eventBuilder = new EventBuilder();
        eventBuilder.setFrom(from);

        return eventBuilder;
    }
}
