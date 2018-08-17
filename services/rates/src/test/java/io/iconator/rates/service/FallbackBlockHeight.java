package io.iconator.rates.service;

import io.iconator.rates.config.AggregationServiceConfig;
import io.iconator.rates.config.BlockNrServiceConfig;
import io.iconator.rates.config.ExchangeRateServiceConfig;
import io.iconator.rates.config.RatesAppConfig;
import io.iconator.rates.config.RatesAppConfigHolder;
import io.iconator.rates.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        TestConfig.class,
        AggregationServiceConfig.class,
        ExchangeRateServiceConfig.class,
        RatesAppConfigHolder.class,
        BlockNrServiceConfig.class,
        RatesAppConfig.class,
        BlockchainInfoService.class,
        EtherscanService.class
})
@DataJpaTest
@TestPropertySource({"classpath:rates.application.properties", "classpath:application-test.properties"})
public class FallbackBlockHeight {

    @Autowired
    private BlockchainInfoService blockchainInfoService;

    @Autowired
    private EtherscanService etherscanService;

    @Test
    public void testFallbackBitcoin() throws IOException {
        Assert.assertTrue(blockchainInfoService.getLatestBitcoinHeight() > 0);
        Assert.assertTrue(etherscanService.getLatestEthereumHeight() > 0);

    }
}
