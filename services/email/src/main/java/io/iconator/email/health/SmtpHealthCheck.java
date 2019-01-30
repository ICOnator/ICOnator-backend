package io.iconator.email.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Health check that checks if the configured SMTP server is still online
 */
@Component
public class SmtpHealthCheck implements HealthIndicator {

    @Value("${io.iconator.commons.mail.service.host}")
    private String host;

    @Value("${io.iconator.commons.mail.service.port}")
    private int port;

    @Override
    public Health health() {
        InetSocketAddress sa = new InetSocketAddress(host, port);

        long latency;
        String info;

        try(Socket s = new Socket()) {
            long start = System.currentTimeMillis();
            s.connect(sa, 1000);
            latency = System.currentTimeMillis() - start;

            PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            // wait for 220 response with server info
            info = in.readLine();
            if(!info.startsWith("220")) {
                return Health.down().withDetail("smtpResponse", info).build();
            }

            out.println("HELO " + sa.getHostName());

            // wait for 250 Hello response
            String welcome = in.readLine();
            if(!welcome.startsWith("250")) {
                return Health.down().withDetail("smtpResponse", welcome).build();
            }

            out.println("QUIT");
        } catch(IOException e) {
            return Health.down(e).build();
        }

        return Health.up()
                .withDetail("smtpInfo", info)
                .withDetail("latency", latency + "ms")
                .build();

    }
}
