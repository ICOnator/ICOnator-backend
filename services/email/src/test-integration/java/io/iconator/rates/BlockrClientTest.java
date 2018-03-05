package io.iconator.rates;

import com.github.rholder.retry.RetryException;
import io.iconator.rates.client.blockr.BlockrClient;
import io.iconator.rates.client.etherscan.EtherScanClient;
import io.iconator.rates.config.Beans;
import io.iconator.rates.config.BlockrClientConfig;
import io.iconator.rates.config.EtherScanClientConfig;
import io.iconator.rates.config.RatesAppConfig;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockrClientConfig.class, RatesAppConfig.class, Beans.class})
@TestPropertySource("classpath:application.properties")
public class BlockrClientTest {

    private final static Logger LOG = LoggerFactory.getLogger(BlockrClientTest.class);

    @Autowired
    private BlockrClient blockrClient;

    @Test
    @Ignore
    public void testGetTxBtc() throws ExecutionException, RetryException {
        List<Triple<Date, Long, Long>> txBtc = blockrClient.getTxBtc("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa");
        LOG.info("Amount of transactions: " + txBtc.size());
        assertNotNull(txBtc);
        assertThat(txBtc.size() > 0);
    }

    @Test
    @Ignore
    public void testGetBlockNr() throws ExecutionException, RetryException {
        long blockNr = blockrClient.getBlockNr("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
        LOG.info("Block number for the tx: " + blockNr);
        assertThat(blockNr > 0);
    }

    @Test
    @Ignore
    public void testBlockNr() throws ExecutionException, RetryException {
        long blockNr = blockrClient.getCurrentBlockNr();
        LOG.info("Current block number: " + blockNr);
        assertThat(blockNr > 0);
    }

}