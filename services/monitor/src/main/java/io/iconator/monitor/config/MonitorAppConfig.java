package io.iconator.monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorAppConfig {

    @Value("${io.iconator.monitor.startblock}")
    private Long startBlock;

    @Value("${io.iconator.monitor.etherfullnodeurl}")
    private String etherFullNodeUrl;

    @Value("${io.iconator.monitor.bitcoinnetwork}")
    private String bitcoinNetwork;

    public Long getStartBlock() {
        return startBlock;
    }

    public String getEtherFullNodeUrl() {
        return etherFullNodeUrl;
    }

    public String getBitcoinNetwork() {
        return bitcoinNetwork;
    }

}
