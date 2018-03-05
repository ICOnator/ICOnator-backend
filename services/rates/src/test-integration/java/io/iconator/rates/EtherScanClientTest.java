package io.iconator.rates;

import com.github.rholder.retry.RetryException;
import io.iconator.rates.client.etherscan.EtherScanClient;
import io.iconator.rates.config.Beans;
import io.iconator.rates.config.EtherScanClientConfig;
import io.iconator.rates.config.RatesAppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EtherScanClientConfig.class, RatesAppConfig.class, Beans.class})
@TestPropertySource("classpath:application.properties")
public class EtherScanClientTest {

    private final static Logger LOG = LoggerFactory.getLogger(EtherScanClientTest.class);

    @Autowired
    private EtherScanClient etherScanClient;

    @Test
    public void testGetBalance() throws ExecutionException, RetryException {
        String balance = etherScanClient.getBalance("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae").toString();
        LOG.info("Balance: " + balance);
        assertNotNull(balance);
        assertThat(!balance.isEmpty());
    }

    @Test
    public void testGet20Balances() throws ExecutionException, RetryException {
        String balance = etherScanClient.get20Balances("0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae", "0x25d96310cd6694d88b9c6803be09511597c0a630").toString();
        LOG.info("Balance: " + balance);
        assertNotNull(balance);
        assertThat(!balance.isEmpty());
    }

    @Test
    public void testBlockNr() throws ExecutionException, RetryException {
        long blockNr = etherScanClient.getCurrentBlockNr();
        LOG.info("Current block number: " + blockNr);
        assertThat(blockNr > 0);
    }

}