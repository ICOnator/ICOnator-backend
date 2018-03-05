package io.iconator.commons.test.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class BaseMessageBrokerServiceTest {

    protected Connection connection;
    protected Channel channel;

    @BeforeClass
    public static void setUpBroker() throws Exception {
        BuiltInMessageBroker.start();
    }

    protected void setConnectionAndChannel(String connectionUri) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(connectionUri);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    @AfterClass
    public static void cleanUpBroker() throws Exception {
        BuiltInMessageBroker.stop();
    }

}
