package io.kodokojo.commons.bdd.feature;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.kodokojo.commons.DockerIsRequire;
import io.kodokojo.commons.DockerPresentMethodRule;
import io.kodokojo.commons.utils.DockerTestApplicationBuilder;
import io.kodokojo.commons.utils.DockerService;
import javaslang.control.Try;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class EventIntTest implements DockerTestApplicationBuilder {

    private static final String QUEUE_NAME = "testQueueName";
    @Rule
    public DockerPresentMethodRule dockerPresentMethodRule = new DockerPresentMethodRule();

    @Test
    @DockerIsRequire
    public void startRabbitMq() {
        Try<DockerService> rabbitMq = startRabbitMq(dockerPresentMethodRule.getDockerTestSupport());
        ConnectionFactory factory = new ConnectionFactory();
        DockerService dockerService = rabbitMq.get();
        factory.setHost(dockerService.getHost());
        factory.setPort(dockerService.getPort());
        //factory.setHost("192.168.99.100");
        //factory.setPort(32777);
        LOGGER.info("Try to connect on rabbit {}:{}.", dockerService.getHost(), dockerService.getPort());
        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            ///channel.exchangeDeclare(QUEUE_NAME, "fanout");
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

            System.out.println(" [x] Sent '" + message + "'");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            try {
                channel.close();
                connection.close();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

}