package io.iconator.email.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;

@Component
public class SmtpHealthCheck implements HealthIndicator {

    @Value("${io.iconator.commons.mail.service.host}")
    private String host;

    @Value("${io.iconator.commons.mail.service.port}")
    private int port;

    @Override
    public Health health() {
        try{
            InetSocketAddress sa = new InetSocketAddress(host, port);
            Socket ss = new Socket();
            ss.connect(sa, 1000);
            ss.close();
        } catch(Exception e) {
            return Health.down(e).build();
        }

        return Health.up().build();

    }
}
