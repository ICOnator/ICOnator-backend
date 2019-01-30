package io.iconator.monitor.health;

import io.iconator.monitor.config.MonitorAppConfigHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

/**
 * A health indicator that checks the connection and latency to the Ethereum node
 */
@Component
public class EtherFullNodeHealthCheck implements HealthIndicator {

    @Autowired
    private MonitorAppConfigHolder monitorAppConfigHolder;

    @Override
    public Health health() {
        URL url;
        long latency;

        try {
            url = new URL(this.monitorAppConfigHolder.getEthereumNodeUrl());
        } catch (MalformedURLException mue) {
            return Health.down(mue).withDetail("URL", this.monitorAppConfigHolder.getEthereumNodeUrl()).build();
        }

        InetSocketAddress sa = new InetSocketAddress(url.getHost(), url.getPort());

        long start = System.currentTimeMillis();

        try(Socket s = new Socket()) {
            s.connect(sa, 1000);
            latency = System.currentTimeMillis() - start;
        } catch (IOException ioe) {
            return Health.down(ioe).build();
        }

        return Health.up().withDetail("latency", latency + "ms").build();
    }
}
