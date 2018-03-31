package io.iconator.monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorAppConfig {

    @Value("${io.iconator.services.monitor.eth.node.start-block}")
    private Long ethereumNodeStartBlock;

    @Value("${io.iconator.services.monitor.eth.node.url}")
    private String ethereumNodeUrl;

    public Long getEthereumNodeStartBlock() {
        return ethereumNodeStartBlock;
    }

    public String getEthereumNodeUrl() {
        return ethereumNodeUrl;
    }

}
