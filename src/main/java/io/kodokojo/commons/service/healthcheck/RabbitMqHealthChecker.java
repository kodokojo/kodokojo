package io.kodokojo.commons.service.healthcheck;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.config.RabbitMqConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class RabbitMqHealthChecker implements HealthChecker{

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqHealthChecker.class);

    private final RabbitMqConfig rabbitMqConfig;

    @Inject
    public RabbitMqHealthChecker(RabbitMqConfig rabbitMqConfig) {
        requireNonNull(rabbitMqConfig, "rabbitMqConfig must be defined.");
        this.rabbitMqConfig = rabbitMqConfig;
    }

    @Override
    public HealthCheck check() {
        ConnectionFactory connectionFactory = createConnectionFactoryFromConfig();
        HealthCheck.Builder builder = new HealthCheck.Builder();
        builder.setName("RabbitMq");
        Connection connection = null;
        try {
            connection = connectionFactory.newConnection();
            builder.setState(connection.isOpen() ? HealthCheck.State.OK : HealthCheck.State.FAIL);
        } catch (Exception e) {
            builder.setState(HealthCheck.State.FAIL);
            builder.setDetail(e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close connection to RabbitMq.", e);
                }
            }
        }
        return builder.build();
    }
    // For test
    protected ConnectionFactory createConnectionFactoryFromConfig() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMqConfig.host());
        connectionFactory.setPort(rabbitMqConfig.port());
        connectionFactory.setVirtualHost(rabbitMqConfig.virtualHost());
        if (StringUtils.isNotBlank(rabbitMqConfig.login())) {
            connectionFactory.setUsername(rabbitMqConfig.login());
            connectionFactory.setPassword(rabbitMqConfig.password());
        }
        return connectionFactory;
    }


}
