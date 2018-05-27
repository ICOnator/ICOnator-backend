package io.iconator.monitor;


import io.iconator.monitor.config.TestConfig;
import io.iconator.testrpcj.TestBlockchain;
import io.iconator.testrpcj.jsonrpc.TypeConverter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class EthereumMonitorTest {

    private final static Logger LOG = LoggerFactory.getLogger(EthereumMonitorTest.class);

    @Autowired
    private EthereumMonitor ethereumMonitor;

    private static TestBlockchain testBlockchain;

    @BeforeClass
    public static void setup() {
        testBlockchain = new TestBlockchain().start();
    }

    @Test
    public void connect() throws Exception {
        String addr = Hex.toHexString(TestBlockchain.ACCOUNT_1.getPubKey());
        LOG.info("Public Key: " + addr);

        //ethereumMonitor.addMonitoredEtherPublicKey(addr);
        //ethereumMonitor.start((long) 0);

        Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:8545/rpc"));

        web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(
                new DefaultBlockParameterNumber(0))
                .subscribe(tx -> {

                    LOG.info("To:" + tx.getTo());
                    LOG.info("From:" + tx.getHash());

                }, throwable -> {
                    LOG.info("Error during scanning of txs: " + throwable);
                });

        Credentials credentials = Credentials.create(ECKeyPair.create(TestBlockchain.ACCOUNT_0.getPrivKeyBytes()));

        TransactionReceipt r = Transfer.sendFunds(
                web3j,
                credentials,
                TypeConverter.toJsonHex(TestBlockchain.ACCOUNT_1.getAddress()),
                BigDecimal.valueOf(0.22222),
                Convert.Unit.ETHER).send();

        LOG.info("output: " + r.isStatusOK());

        Thread.sleep(20000);
    }
}
