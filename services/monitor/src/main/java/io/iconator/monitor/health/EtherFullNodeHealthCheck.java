package io.iconator.monitor.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;

/**
 * A health indicator that checks the connection and latency to the Ethereum node
 */
@Component
public class EtherFullNodeHealthCheck implements HealthIndicator {

    @Value("${io.iconator.monitor.etherfullnodeurl}")
    private String etherNodeUrl;

    private URL url;

    @Override
    public Health health() {
        try {
            url = new URL(etherNodeUrl);
        } catch(MalformedURLException mue) {
            return Health.down(mue).withDetail("URL", etherNodeUrl).build();
        }

        InetSocketAddress sa = new InetSocketAddress(url.getHost(), url.getPort());
        Socket s = new Socket();

        long start = System.currentTimeMillis();
        try {
            s.connect(sa, 1000);
        } catch(Exception e) {
            return Health.down(e).build();
        }
        long latency = System.currentTimeMillis() - start;

        try {
            s.close();
        } catch(IOException ioe) {
            return Health.down(ioe).build();
        }

        return Health.up().withDetail("latency", latency + "ms").build();
    }
}
