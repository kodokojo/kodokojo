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
package io.kodokojo.commons.config;

import io.kodokojo.commons.config.properties.Key;
import io.kodokojo.commons.config.properties.PropertyConfig;

public interface RabbitMqConfig extends PropertyConfig {

    String RABBITMQ_HOST = "rabbitmq.host";

    String RABBITMQ_PORT = "rabbitmq.port";

    @Key(value = RABBITMQ_HOST, defaultValue = "rabbitmq")
    String host();

    @Key(value = RABBITMQ_PORT, defaultValue = "5672")
    Integer port();

    @Key(value = "rabbitmq.business", defaultValue = "kodokojo.business")
    String businessExchangeName();

    @Key(value = "rabbitmq.service")
    String serviceQueueName();

    @Key(value = "rabbitmq.broadcast", defaultValue = "kodokojo.broadcast")
    String broadcastExchangeName();

    @Key(value = "rabbitmq.deadletter", defaultValue = "kodokojo.deadletter")
    String deadLetterExchangeName();

    @Key(value = "rabbitmq.deadletter.queue", defaultValue = "kodokojo.deadletterQueue")
    String deadLetterQueueName();

    @Key(value = "rabbitmq.login")
    String login();

    @Key(value = "rabbitmq.password")
    String password();

    @Key(value = "rabbitmq.maxRedeliveryMessageCount", defaultValue = "3")
    Integer maxRedeliveryMessageCount();


}
