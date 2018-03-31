package io.iconator.monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorAppConfig {

    @Value("${io.iconator.services.monitor.eth.node.enabled}")
    private Boolean ethereumNodeEnabled;

    @Value("${io.iconator.services.monitor.eth.node.start-block}")
    private Long ethereumNodeStartBlock;

    @Value("${io.iconator.services.monitor.eth.node.url}")
    private String ethereumNodeUrl;

    @Value("${io.iconator.services.monitor.btc.node.enabled}")
    private Boolean bitcoinNodeEnabled;

    public Boolean getEthereumNodeEnabled() {
        return ethereumNodeEnabled;
    }

    public Long getEthereumNodeStartBlock() {
        return ethereumNodeStartBlock;
    }

    public String getEthereumNodeUrl() {
        return ethereumNodeUrl;
    }

    public Boolean getBitcoinNodeEnabled() {
        return bitcoinNodeEnabled;
    }
}
