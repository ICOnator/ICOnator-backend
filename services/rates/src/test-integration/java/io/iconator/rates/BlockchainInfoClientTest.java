package io.iconator.rates;

import com.github.rholder.retry.RetryException;
import io.iconator.rates.client.blockchaininfo.BlockchainInfoClient;
import io.iconator.rates.config.Beans;
import io.iconator.rates.config.BlockchainInfoClientConfig;
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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockchainInfoClientConfig.class, RatesAppConfig.class, Beans.class})
@TestPropertySource("classpath:application.properties")
public class BlockchainInfoClientTest {

    private final static Logger LOG = LoggerFactory.getLogger(BlockchainInfoClientTest.class);

    @Autowired
    private BlockchainInfoClient blockchainInfoClient;

    @Test
    public void testBlockNr() throws ExecutionException, RetryException {
        long blockNr = blockchainInfoClient.getCurrentBlockNr();
        LOG.info("Current block number: " + blockNr);
        assertThat(blockNr > 0);
    }

}