package io.iconator.rates.config;

import io.iconator.rates.client.blockchaininfo.BlockchainInfoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockchainInfoClientConfig {

    @Bean
    public BlockchainInfoClient blockr() {
        return new BlockchainInfoClient();
    }

}
