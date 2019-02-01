package io.iconator.monitor;

import io.iconator.commons.amqp.model.TokensAllocatedEmailMessage;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorTestConfig;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.utils.MockICOnatorMessageService;
import io.iconator.testonator.TestBlockchain;
import io.iconator.testonator.jsonrpc.TypeConverter;
import org.ethereum.crypto.ECKey;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MonitorTestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional(propagation = Propagation.SUPPORTS)
public class EthereumMonitorTest {

    private final static Logger LOG = LoggerFactory.getLogger(EthereumMonitorTest.class);

    @Autowired
    private EthereumMonitor ethereumMonitor;

    @Autowired
    private Web3j web3j;

    @Autowired
    private InvestorService investorService;

    @Autowired
    private SaleTierService saleTierService;

    @MockBean
    private FxService fxService;

    @Autowired
    private MockICOnatorMessageService mockICOnatorMessageService;

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    private MonitorService monitorService;

    private static TestBlockchain testBlockchain;

    @BeforeClass
    public static void setup() throws Exception {
        testBlockchain = new TestBlockchain().start();
    }

    @Before
    public void setUp() {
        createAndSaveTier();
    }

    @After
    public void deleteTiers() {
        saleTierRepository.deleteAll();
    }

    /*
     * TODO make independent of properties values.
     * This test depends on multiple values from the {@code monitor.application.properties} to
     * be set just right so that the test runs successful. Either go back to a mock of the
     * MonitorAppConfigHolder or create a application-test.properties.
     */
    @Test
    public void testPayment() throws Exception {
        BigDecimal fundsAmountToSendInETH = BigDecimal.ONE;
        BigDecimal usdPricePerETH = BigDecimal.ONE;
        BigDecimal fundsAmountToSendInUSD = fundsAmountToSendInETH.multiply(usdPricePerETH);
        BigInteger tomicsAmountToBeReceived = monitorService.convertUsdToTomics(
                fundsAmountToSendInUSD, BigDecimal.ZERO).toBigInteger();
        CurrencyType currencyType = CurrencyType.ETH;

        given(fxService.getUSDExchangeRate(any(), any(CurrencyType.class)))
                .willReturn(Optional.of(usdPricePerETH));

        Investor investor = createAndSaveInvestor(TestBlockchain.ACCOUNT_1);
        ethereumMonitor.addPaymentAddressesForMonitoring(investor.getPayInEtherAddress(), null);
        ethereumMonitor.start();

        Credentials credentials = Credentials.create(
                ECKeyPair.create(TestBlockchain.ACCOUNT_0.getPrivKeyBytes()));
        Transfer.sendFunds(
                web3j,
                credentials,
                investor.getPayInEtherAddress(),
                fundsAmountToSendInETH,
                Convert.Unit.ETHER).send();

        // TODO:
        // The blocks are generated every 15seconds by testrpcj.
        // Make this configurable through the TestBlockchain class, and
        // then remove the thread sleep.
        Thread.sleep(20000);

        List<TokensAllocatedEmailMessage> tokenAllocatedEmailMessage = mockICOnatorMessageService.getTokensAllocatedEmailMessages();

        assertEquals(1, tokenAllocatedEmailMessage.size());

        // TODO: 30.08.18 Guil
        // We need to test if the transaction was received! However, we need supported from the TestBlockchain implementation
        assertTrue(matchReceivedMessage(tokenAllocatedEmailMessage, isTokenAmountReceivedEqualToTokenAmountSent(tomicsAmountToBeReceived)));
        assertTrue(matchReceivedMessage(tokenAllocatedEmailMessage, isCurrencyTypeReceivedEqualToCurrencyTypeSent(currencyType)));
        assertTrue(matchReceivedMessage(tokenAllocatedEmailMessage, isAmountFundsReceivedEqualToFundsSent(fundsAmountToSendInETH)));
    }

    private Predicate<TokensAllocatedEmailMessage> isTokenAmountReceivedEqualToTokenAmountSent(BigInteger tomicsAmountSent) {
        BigDecimal tokens = monitorService.convertTomicsToTokens(tomicsAmountSent);
        return p -> p.getTokenAmount().compareTo(tokens) == 0;
    }

    public Predicate<TokensAllocatedEmailMessage> isCurrencyTypeReceivedEqualToCurrencyTypeSent(CurrencyType currencySent) {
        return p -> p.getCurrencyType() == currencySent;
    }

    public Predicate<TokensAllocatedEmailMessage> isAmountFundsReceivedEqualToFundsSent(BigDecimal fundsSent) {
        return p -> p.getAmountFundsReceived().compareTo(fundsSent) == 0;
    }

    private boolean matchReceivedMessage(List<TokensAllocatedEmailMessage> messages, Predicate<TokensAllocatedEmailMessage> predicate) {
        return messages.stream().allMatch(predicate);
    }

    private Investor createAndSaveInvestor(ECKey key) {
        return investorService.saveRequireNewTransaction(buildInvestor(key));
    }

    private Investor buildInvestor(ECKey key) {
        return new Investor(
                new Date(),
                "email@email.com",
                "emailConfirmationToken",
                "walletAddress",
                TypeConverter.toJsonHex(key.getAddress()),
                "payInBitcoinAddress",
                "refundEtherAddress",
                "refundBitcoinAddress",
                "127.0.0.1"
        );
    }

    private void createAndSaveTier() {
        Date from = Date.from(Instant.EPOCH);
        Date to = new Date();
        BigInteger tomics = monitorService.convertTokensToTomics(new BigDecimal(1000L))
                .toBigInteger();

        saleTierService.saveRequireTransaction(
                new SaleTier(4, "4", from, to, new BigDecimal("0.0"),
                        BigInteger.ZERO, tomics, true, false));
    }

}

