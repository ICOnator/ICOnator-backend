package io.iconator.rates.config;

import io.iconator.rates.consumer.BlockNrBitcoinConsumer;
import io.iconator.rates.consumer.BlockNrEthereumConsumer;
import io.iconator.rates.service.ExchangeRateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockNrServiceConfig {

    @Bean
    public BlockNrBitcoinConsumer blockNrBitcoinConsumer() {
        BlockNrBitcoinConsumer b = new BlockNrBitcoinConsumer();
        b.setCurrentBlockNr(1l);
        return b;
    }

    @Bean
    public BlockNrEthereumConsumer blockNrEthereumConsumer() {
        BlockNrEthereumConsumer b = new BlockNrEthereumConsumer();
        b.setCurrentBlockNr(1l);
        return b;
    }

}
