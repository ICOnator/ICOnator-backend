package io.iconator.rates.config;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import info.blockchain.api.blockexplorer.BlockExplorer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.coinmarketcap.CoinMarketCapExchange;
import org.knowm.xchange.gdax.GDAXExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.StopStrategies.stopAfterAttempt;
import static com.github.rholder.retry.WaitStrategies.randomWait;

;

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

    @Bean(name = "gdaxExchange")
    public Exchange gdaxExchange() {
        return ExchangeFactory.INSTANCE.createExchange(GDAXExchange.class.getName());
    }

    @Bean(name = "gdaxMarketDataService")
    public MarketDataService gdaxMarketDataService(@Qualifier("gdaxExchange") Exchange exchange) {
        return exchange.getMarketDataService();
    }

    @Bean(name = "coinMarketCapExchange")
    public Exchange coinMarketCapExchange() {
        return ExchangeFactory.INSTANCE.createExchange(CoinMarketCapExchange.class.getName());
    }

    @Bean(name = "coinMarketCapMarketDataService")
    public MarketDataService coinMarketCapMarketDataService(@Qualifier("coinMarketCapExchange") Exchange exchange) {
        return exchange.getMarketDataService();
    }

}
