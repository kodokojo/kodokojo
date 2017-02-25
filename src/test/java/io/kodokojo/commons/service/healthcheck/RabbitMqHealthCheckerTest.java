package io.kodokojo.commons.service.healthcheck;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.config.RabbitMqConfig;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RabbitMqHealthCheckerTest {

    private ConnectionFactory connectionFactory;

    private Connection connection;

    private RabbitMqConfig rabbitMqConfig;

    @Before
    public void setup() {

        rabbitMqConfig = mock(RabbitMqConfig.class);
        connectionFactory = mock(ConnectionFactory.class);
        connection = mock(Connection.class);

    }

    @Test
    public void valid_connection() {

        RabbitMqHealthChecker rabbitMqHealthChecker = new RabbitMqHealthChecker(rabbitMqConfig) {
            @Override
            protected ConnectionFactory createConnectionFactoryFromConfig() {
                return connectionFactory;
            }
        };

        try {
            when(connectionFactory.newConnection()).thenReturn(connection);
            when(connection.isOpen()).thenReturn(true);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        HealthCheck healthCheck = rabbitMqHealthChecker.check();

        assertThat(healthCheck).isNotNull();
        assertThat(healthCheck.getState()).isEqualTo(HealthCheck.State.OK);

    }

    @Test
    public void connection_closed() {

        RabbitMqHealthChecker rabbitMqHealthChecker = new RabbitMqHealthChecker(rabbitMqConfig) {
            @Override
            protected ConnectionFactory createConnectionFactoryFromConfig() {
                return connectionFactory;
            }
        };

        try {
            when(connectionFactory.newConnection()).thenReturn(connection);
            when(connection.isOpen()).thenReturn(false);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        HealthCheck healthCheck = rabbitMqHealthChecker.check();

        assertThat(healthCheck).isNotNull();
        assertThat(healthCheck.getState()).isEqualTo(HealthCheck.State.FAIL);

    }
    @Test
    public void connection_failed() {

        RabbitMqHealthChecker rabbitMqHealthChecker = new RabbitMqHealthChecker(rabbitMqConfig) {
            @Override
            protected ConnectionFactory createConnectionFactoryFromConfig() {
                return connectionFactory;
            }
        };

        try {
            when(connectionFactory.newConnection()).thenReturn(connection);
            when(connection.isOpen()).thenThrow(new RuntimeException("Fake fail connection"));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        HealthCheck healthCheck = rabbitMqHealthChecker.check();

        assertThat(healthCheck).isNotNull();
        assertThat(healthCheck.getState()).isEqualTo(HealthCheck.State.FAIL);

    }

}