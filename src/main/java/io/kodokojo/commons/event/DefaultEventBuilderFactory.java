/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
