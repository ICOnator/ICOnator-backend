package io.iconator.rates.config;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import info.blockchain.api.blockexplorer.BlockExplorer;
import org.codehaus.jackson.map.ObjectMapper;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

;import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.StopStrategies.*;
import static com.github.rholder.retry.WaitStrategies.*;

@Configuration
public class Beans {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public BlockExplorer blockExplorer() {
        return new BlockExplorer();
    }

    @Bean
    public Retryer retryer(RatesAppConfig ratesAppConfig) {
        return RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(IOException.class)
                .retryIfRuntimeException()
                .withWaitStrategy(randomWait(ratesAppConfig.getMinTimeWait(), TimeUnit.MILLISECONDS, ratesAppConfig.getMaxTimeWait(), TimeUnit.MILLISECONDS))
                .withStopStrategy(stopAfterAttempt(ratesAppConfig.getMaxAttempts()))
                .build();
    }

    @Bean(name = "bitstampExchange")
    public Exchange bitstampExchange() {
        return ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName());
    }

    @Bean(name = "bitstampMarketDataService")
    public MarketDataService bitstampMarketDataService(@Qualifier("bitstampExchange") Exchange exchange) {
        return exchange.getMarketDataService();
    }

    @Bean(name = "bitfinexExchange")
    public Exchange bitfinexExchange() {
        return ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName());
    }

    @Bean(name = "bitfinexMarketDataService")
    public MarketDataService bitfinexMarketDataService(@Qualifier("bitfinexExchange") Exchange exchange) {
        return exchange.getMarketDataService();
    }

    @Bean(name = "krakenExchange")
    public Exchange krakenExchange() {
        return ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName());
    }

    @Bean(name = "krakenMarketDataService")
    public MarketDataService krakenMarketDataService(@Qualifier("krakenExchange") Exchange exchange) {
        return exchange.getMarketDataService();
    }

}
