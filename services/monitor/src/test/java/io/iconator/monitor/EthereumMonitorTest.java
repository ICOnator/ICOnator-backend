package io.iconator.monitor;


import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.config.EthereumMonitorTestConfig;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.utils.MockICOnatorMessageService;
import io.iconator.testrpcj.TestBlockchain;
import io.iconator.testrpcj.jsonrpc.TypeConverter;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EthereumMonitorTestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class EthereumMonitorTest {

    private final static Logger LOG = LoggerFactory.getLogger(EthereumMonitorTest.class);

    @Autowired
    private EthereumMonitor ethereumMonitor;

    @Autowired
    private Web3j web3j;

    @Autowired
    private InvestorRepository investorRepository;

    @MockBean
    private FxService fxService;

    @Autowired
    private MockICOnatorMessageService mockICOnatorMessageService;

    private static TestBlockchain testBlockchain;

    @BeforeClass
    public static void setup() throws Exception {
        testBlockchain = new TestBlockchain().start();
    }

    @Ignore
    @Test
    public void testPayment() throws Exception {

        String account1PubKey = Hex.toHexString(TestBlockchain.ACCOUNT_1.getPubKey());
        Long ethBlockNumber = new Long(2);
        BigDecimal usdPerETH = new BigDecimal(1);

        when(fxService.getUSDperETH(eq(ethBlockNumber)))
                .thenReturn(usdPerETH);

        ethereumMonitor.addMonitoredEtherPublicKey(account1PubKey);
        ethereumMonitor.start((long) 0);

        Credentials credentials = Credentials.create(ECKeyPair.create(TestBlockchain.ACCOUNT_0.getPrivKeyBytes()));

        TransactionReceipt r = Transfer.sendFunds(
                web3j,
                credentials,
                TypeConverter.toJsonHex(TestBlockchain.ACCOUNT_1.getAddress()),
                BigDecimal.valueOf(1.0),
                Convert.Unit.ETHER).send();

        // TODO:
        // The blocks are generated every 15seconds by testrpcj.
        // Make this configurable through the TestBlockchain class, and
        // then remove the thread sleep.
        Thread.sleep(20000);

        assertEquals(1, mockICOnatorMessageService.getFundsReceivedEmailMessages().size());
    }

}
