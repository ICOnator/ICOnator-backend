package io.iconator.monitor;


import io.iconator.monitor.config.TestConfig;
import io.iconator.testrpcj.TestBlockchain;
import io.iconator.testrpcj.jsonrpc.TypeConverter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
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
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
//@ComponentScan("io.iconator.testrpcj")
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class EthereumMonitorTest {

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
        System.out.println("public key:" + addr);
        //ethereumMonitor.addMonitoredEtherPublicKey(addr);
        //ethereumMonitor.start((long) 0);
        Web3j web3j = Web3j.build(new HttpService("http://localhost:8545/rpc"));

        web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(
                new DefaultBlockParameterNumber(0))
                .subscribe(tx -> {

                    System.out.println("A:"+tx.getTo());
                    System.out.println("B:"+tx.getHash());

                }, throwable -> {
                    System.out.println("Error during scanning of txs: " + throwable);
                });

        Credentials credentials = Credentials.create(ECKeyPair.create(TestBlockchain.ACCOUNT_0.getPrivKeyBytes()));

        TransactionReceipt r = Transfer.sendFunds(
                web3j,
                credentials,
                TypeConverter.toJsonHex(TestBlockchain.ACCOUNT_1.getAddress()),
                BigDecimal.valueOf(0.22222),
                Convert.Unit.ETHER).send();
        System.out.println("output: " + r.isStatusOK());
        System.out.println("output: " + r.isStatusOK());
        Thread.sleep(20000);
    }
}
