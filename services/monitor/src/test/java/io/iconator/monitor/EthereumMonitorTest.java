package io.iconator.monitor;


import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.EthereumMonitorTestConfig;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenConversionService;
import io.iconator.monitor.utils.MockICOnatorMessageService;
import io.iconator.testrpcj.TestBlockchain;
import io.iconator.testrpcj.jsonrpc.TypeConverter;
import org.ethereum.crypto.ECKey;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
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
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EthereumMonitorTestConfig.class})
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
    private InvestorRepository investorRepository;

    @MockBean
    private FxService fxService;

    @Autowired
    private MockICOnatorMessageService mockICOnatorMessageService;

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    private TokenConversionService tokenConversionService;

    private static TestBlockchain testBlockchain;

    @BeforeClass
    public static void setup() throws Exception {
        testBlockchain = new TestBlockchain().start();
    }

    @Before
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // Persist directly to DB without transactions.
    public void setUp() {
        createAndSaveTier();
        createAndSaveInvestor(TestBlockchain.ACCOUNT_1);
    }

    @After
    public void deleteTiers() {
        saleTierRepository.deleteAll();
    }

    @Test
    public void testPayment() throws Exception {

        BigDecimal fundsAmountToSendInETH = BigDecimal.ONE;
        BigDecimal usdPricePerETH = BigDecimal.ONE;
        BigDecimal fundsAmountToSendInUSD = fundsAmountToSendInETH.multiply(usdPricePerETH);
        BigInteger tomicsAmountToBeReceived = tokenConversionService.convertUsdToTomics(
                fundsAmountToSendInUSD, BigDecimal.ZERO).toBigInteger();
        CurrencyType currencyType = CurrencyType.ETH;
        Long ethBlockNumber = new Long(2);

        String account1PubKey = Numeric.prependHexPrefix(Hex.toHexString(Keys.getAddress(TestBlockchain.ACCOUNT_1.getPubKey())));

        when(fxService.getUSDperETH(eq(ethBlockNumber)))
                .thenReturn(usdPricePerETH);

        ethereumMonitor.addMonitoredEtherAddress(account1PubKey);
        ethereumMonitor.start((long) 0);

        Credentials credentials = Credentials.create(ECKeyPair.create(TestBlockchain.ACCOUNT_0.getPrivKeyBytes()));

        TransactionReceipt r = Transfer.sendFunds(
                web3j,
                credentials,
                TypeConverter.toJsonHex(TestBlockchain.ACCOUNT_1.getAddress()),
                fundsAmountToSendInETH,
                Convert.Unit.ETHER).send();

        // TODO:
        // The blocks are generated every 15seconds by testrpcj.
        // Make this configurable through the TestBlockchain class, and
        // then remove the thread sleep.
        Thread.sleep(20000);

        List<FundsReceivedEmailMessage> messages = mockICOnatorMessageService.getFundsReceivedEmailMessages();

        assertEquals(1, messages.size());

        assertTrue(matchReceivedMessage(messages, isTokenAmountReceivedEqualToCurrencyTypeSent(tomicsAmountToBeReceived)));
        assertTrue(matchReceivedMessage(messages, isCurrencyTypeReceivedEqualToCurrencyTypeSent(currencyType)));
        assertTrue(matchReceivedMessage(messages, isAmountFundsReceivedEqualToFundsSent(fundsAmountToSendInETH)));
    }

    private Predicate<FundsReceivedEmailMessage> isTokenAmountReceivedEqualToCurrencyTypeSent(BigInteger tomicsAmountSent) {
        BigDecimal tokens = tokenConversionService.convertTomicsToTokens(tomicsAmountSent);
        return p -> p.getTokenAmount().compareTo(tokens) == 0;
    }


    public Predicate<FundsReceivedEmailMessage> isCurrencyTypeReceivedEqualToCurrencyTypeSent(CurrencyType currencySent) {
        return p -> p.getCurrencyType() == currencySent;
    }

    public Predicate<FundsReceivedEmailMessage> isAmountFundsReceivedEqualToFundsSent(BigDecimal fundsSent) {
        return p -> p.getAmountFundsReceived().compareTo(fundsSent) == 0;
    }

    private boolean matchReceivedMessage(List<FundsReceivedEmailMessage> messages, Predicate<FundsReceivedEmailMessage> predicate) {
        return messages.stream().allMatch(predicate);
    }

    private Investor createAndSaveInvestor(ECKey key) {
        return investorRepository.saveAndFlush(buildInvestor(key));
    }

    private Investor buildInvestor(ECKey key) {
        return new Investor(
                new Date(),
                "email@email.com",
                "emailConfirmationToken",
                "walletAddress",
                Hex.toHexString(key.getPubKey()),
                "payInBitcoinAddress",
                "refundEtherAddress",
                "refundBitcoinAddress",
                "127.0.0.1"


        );
    }

    private void createAndSaveTier() {
        Date from = Date.from(Instant.EPOCH);
        Date to = new Date();
        BigInteger tomics = tokenConversionService.convertTokensToTomics(new BigDecimal(1000L))
                .toBigInteger();
        saleTierRepository.saveAndFlush(
                new SaleTier(4, "4", from, to, new BigDecimal("0.0"), BigInteger.ZERO, tomics, true, false));
    }

}
