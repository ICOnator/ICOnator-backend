package io.iconator.rates.config;

import io.iconator.rates.client.blockr.BlockrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockrClientConfig {

    @Bean
    public BlockrClient blockr() {
        return new BlockrClient();
    }

}
