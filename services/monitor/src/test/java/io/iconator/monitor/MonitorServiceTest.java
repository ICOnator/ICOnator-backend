package io.iconator.monitor;

import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.model.db.PaymentLog.TransactionStatus;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.commons.test.utils.ThreadTestUtils;
import io.iconator.monitor.config.MonitorAppConfigHolder;
import io.iconator.monitor.config.MonitorTestConfig;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.service.exceptions.NoTierAtDateException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MonitorTestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class MonitorServiceTest {

    private final static Logger LOG = LoggerFactory.getLogger(MonitorServiceTest.class);
    private final static BigDecimal USD_FX_RATE = new BigDecimal(400);

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    private SaleTierService saleTierService;

    @Autowired
    private PaymentLogService paymentLogService;

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private EligibleForRefundRepository eligibleForRefundRepository;

    @Autowired
    private InvestorService investorService;

    @Autowired
    private InvestorRepository investorRepository;

    @MockBean
    private MonitorAppConfigHolder appConfig;

    @Autowired
    private EthereumMonitor ethereumMonitor;

    private static final String INVESTOR_EMAIL = "email@mail.com";

    @Before
    public void setUp() {
        when(appConfig.getTotalTokenAmount())
                .thenReturn(new BigDecimal("100000000"));
        when(appConfig.getFiatBasePerToken())
                .thenReturn(new BigDecimal("0.1"));
        when(appConfig.getAtomicUnitFactor())
                .thenReturn(BigInteger.TEN.pow(18));
    }

    @After
    public void cleanUp() {
        saleTierRepository.deleteAll();
        paymentLogRepository.deleteAll();
        eligibleForRefundRepository.deleteAll();
        investorRepository.deleteAll();
    }


    @Test
    public void testConvertUsdToTomics() {
        BigDecimal usd = new BigDecimal("1");
        BigDecimal discount = new BigDecimal("0.25", new MathContext(6, RoundingMode.HALF_EVEN));

        BigDecimal tomics = monitorService.convertUsdToTomics(usd, discount);
        BigDecimal expectedResult = new BigDecimal("40").multiply(new BigDecimal(tomicsFactor()))
                .divide(new BigDecimal("3"), new MathContext(34, RoundingMode.DOWN));

        assertEquals(0, tomics.compareTo(expectedResult));
    }

    @Test
    public void testConvertTomicsToUsd() {
        BigDecimal tomics = new BigDecimal("3.333").multiply(new BigDecimal(tomicsFactor()));
        BigDecimal discount = new BigDecimal("0.333333", new MathContext(6, RoundingMode.HALF_EVEN));

        BigDecimal usd = monitorService.convertTomicsToUsd(tomics, discount);
        BigDecimal expectedResult = new BigDecimal("3.333")
                .multiply(BigDecimal.ONE.subtract(discount), MathContext.DECIMAL128)
                .multiply(appConfig.getFiatBasePerToken());
        assertEquals(0, usd.compareTo(expectedResult));
    }

    @Test
    public void testNoTierAvailableAtDate() {
        PaymentLog log = createPaymentLog(BigDecimal.TEN, Date.valueOf("1970-01-01"));
        try {
            monitorService.allocateTokens(log);
        } catch (NoTierAtDateException e) {
            return;
        }
        fail();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testDistributeToSingleTier() throws Throwable {
        // setup
        TestTier tt = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.2"),
                new BigInteger("1000").multiply(tomicsFactor()), true, false);
        Date blockTime = Date.valueOf("1970-01-02");
        final BigInteger tomicsToSell = tt.getTomicsMax().divide(BigInteger.valueOf(2));
        tt.tomicsSoldMustBe(tomicsToSell);
        final BigDecimal payment = monitorService.convertTomicsToUsd(tomicsToSell, tt.getDiscount());

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() != null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(tomicsToSell));
        tt.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowWithSingleTier() throws Throwable {
        // setup
        TestTier tt = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), true, false);
        Date blockTime = Date.valueOf("1970-01-02");
        tt.newEndDateMustBe(blockTime);
        tt.mustBeFull();
        final BigDecimal overflow = BigDecimal.TEN;
        final BigDecimal payment = monitorService.convertTomicsToUsd(tt.getTomicsMax(), tt.getDiscount())
                .add(overflow);

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() == null) fail();
        // rounding the resulting USD overflow because that will be the actual precision with which the overflow
        // will be stored for refunds.
        assertEquals(0, log.getEligibleForRefund().getUsdAmount().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        assertEquals(0, log.getAllocatedTomics().compareTo(tt.getTomicsMax()));
        tt.assertTier();
    }


    /**
     * Two tiers, one payment that spills into the second tier. First tier has dynamic duration behavior and therefore
     * adapts his end date and the next tier's start and end dates.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowIntoSecondTierDynamicDuration() throws Throwable {
        // setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), true, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.newEndDateMustBe(blockTime);
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        BigInteger tomicsToSellFromTier2 = tt2.getTomicsMax().divide(BigInteger.valueOf(3));
        tt2.datesMustBeShiftedBy(tt1.getTier().getEndDate().getTime() - blockTime.getTime());
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);

        final BigDecimal paymentToTier1 = monitorService.convertTomicsToUsd(tomicsToSellFromTier1, tt1.getDiscount());
        final BigDecimal paymentToTier2 = monitorService.convertTomicsToUsd(tomicsToSellFromTier2, tt2.getDiscount());
        BigDecimal payment = paymentToTier1.add(paymentToTier2);

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() != null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        makeAndConvertPaymentFailingOnOverflow(tt2, blockTime, 3);
        tt1.assertTier();
        tt2.assertTier();
    }

    /**
     * Two tiers, one payment that spills into the second tier. First tier has static duration behavior and therefore
     * doesn't change his end date nore the dates of the next tiers.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowIntoSecondTierStaticDuration() throws Throwable {
        // setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        final BigInteger tomicsToSellFromTier2 = tt2.getTomicsMax().divide(BigInteger.valueOf(3));
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);

        final BigDecimal paymentToTier1 = monitorService.convertTomicsToUsd(tomicsToSellFromTier1, tt1.getDiscount());
        final BigDecimal paymentToTier2 = monitorService.convertTomicsToUsd(tomicsToSellFromTier2, tt2.getDiscount());
        BigDecimal payment = paymentToTier1.add(paymentToTier2);

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() != null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        makeAndConvertPaymentFailingOnOverflow(tt2, blockTime, 3);
        tt1.assertTier();
        tt2.assertTier();
    }

    /**
     * Two tiers, one payment that spills into the second tier. Second tier has dynamic token max.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowIntoSecondTierDynamicMax() throws Throwable {
        // setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), BigInteger.ZERO, false, true);
        BigInteger tomicsMax = totalTomicsAmount().subtract(tomicsToSellFromTier1);
        final BigInteger tomicsToSellFromTier2 = tomicsMax.divide(BigInteger.valueOf(3));
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);
        tt2.tomicsMaxMustBe(tomicsMax);

        final BigDecimal paymentToTier1 = monitorService.convertTomicsToUsd(tomicsToSellFromTier1, tt1.getDiscount());
        final BigDecimal paymentToTier2 = monitorService.convertTomicsToUsd(tomicsToSellFromTier2, tt2.getDiscount());
        BigDecimal payment = paymentToTier1.add(paymentToTier2);

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() != null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        makeAndConvertPaymentFailingOnOverflow(tt2, blockTime, 3);
        tt1.assertTier();
        tt2.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowMultipleStaticTiers() throws Throwable {
        // setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        tt1.mustBeFull();

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1500").multiply(tomicsFactor()), false, false);
        tt2.mustBeFull();

        TestTier tt3 = new TestTier(3, "1970-01-05", "1970-01-07", BigDecimal.ZERO, new BigInteger("2000").multiply(tomicsFactor()), false, false);
        tt3.mustBeFull();

        final BigDecimal overflow = BigDecimal.TEN;
        final BigDecimal payment = monitorService.convertTomicsToUsd(tt1.getTomicsMax(), tt1.getDiscount())
                .add(monitorService.convertTomicsToUsd(tt2.getTomicsMax(), tt2.getDiscount()))
                .add(monitorService.convertTomicsToUsd(tt3.getTomicsMax(), tt3.getDiscount()))
                .add(overflow);

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() == null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(tt1.getTomicsMax().add(tt2.getTomicsMax()).add(tt3.getTomicsMax())));
        assertEquals(0, log.getEligibleForRefund().getUsdAmount().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        tt1.assertTier();
        tt2.assertTier();
        tt3.assertTier();

        log = createPaymentLog(BigDecimal.TEN, Date.valueOf("1970-01-06"));
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() == null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(BigInteger.ZERO));
        assertEquals(0, log.getEligibleForRefund().getUsdAmount().compareTo(BigDecimal.TEN));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowTotalTokenAmountAndTier() throws Throwable {
        // setup
        final Date blockTime = Date.valueOf("1970-01-02");
        // tier with a capacity of total token amount plus one token.
        final TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), totalTomicsAmount().add(tomicsFactor()), true, false);
        tt1.tomicsSoldMustBe(totalTomicsAmount());

        // only the total amount of tokens can be disttributed/converted.
        final BigDecimal usdAmountConverted = monitorService.convertTomicsToUsd(totalTomicsAmount(), tt1.getDiscount());
        final BigDecimal overflowOverTier = BigDecimal.TEN;
        final BigDecimal payment = monitorService.convertTomicsToUsd(tt1.getTomicsMax(), tt1.getDiscount())
                .add(overflowOverTier);
        final BigDecimal overflow = payment.subtract(usdAmountConverted);

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() == null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(totalTomicsAmount()));
        assertEquals(0, log.getEligibleForRefund().getUsdAmount().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        tt1.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowTotalTokenAmountButNotTier() throws Throwable {
        // setup
        final Date blockTime = Date.valueOf("1970-01-02");
        // tier with a capacity of total token amount plus one token.
        final TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), totalTomicsAmount().add(tomicsFactor()), true, false);
        tt1.tomicsSoldMustBe(totalTomicsAmount());

        final BigDecimal overflow = appConfig.getFiatBasePerToken().divide(new BigDecimal("2"));
        final BigDecimal payment = monitorService.convertTomicsToUsd(totalTomicsAmount(), tt1.getDiscount()).add(overflow);

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() == null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(totalTomicsAmount()));
        assertEquals(0, log.getEligibleForRefund().getUsdAmount().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        tt1.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowTotalTokenAmountWithHalfFullTier() throws Throwable {
        final Date blockTime = Date.valueOf("1970-01-02");
        // tier with a capacity of total token amount plus one token.
        final TestTier t = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), totalTomicsAmount().add(tomicsFactor()), true, false);

        BigInteger tomicsFromTier = totalTomicsAmount().divide(new BigInteger("2"));
        BigDecimal payment = monitorService.convertTomicsToUsd(tomicsFromTier, t.getDiscount());
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        t.tomicsSoldMustBe(tomicsFromTier);
        if (log.getEligibleForRefund() != null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(tomicsFromTier));
        t.assertTier();

        final BigDecimal overflow = appConfig.getFiatBasePerToken().divide(new BigDecimal("2"));
        payment = monitorService.convertTomicsToUsd(tomicsFromTier, t.getDiscount()).add(overflow);
        t.tomicsSoldMustBe(totalTomicsAmount());

        log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() == null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(tomicsFromTier));
        assertEquals(0, log.getEligibleForRefund().getUsdAmount().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        t.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowTotalTokenAmountInSecondTier() throws Throwable {

        final BigInteger tomicsOverflowOverTier1 = tomicsFactor();
        final Date blockTime = Date.valueOf("1970-01-02");
        final TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"),
                totalTomicsAmount().subtract(tomicsOverflowOverTier1), true, false);
        tt1.mustBeFull();
        tt1.newEndDateMustBe(blockTime);

        final TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.10"),
                totalTomicsAmount(), true, false);
        tt2.tomicsSoldMustBe(tomicsOverflowOverTier1);
        tt2.datesMustBeShiftedBy(tt1.getTier().getEndDate().getTime() - blockTime.getTime());

        // payment setup
        final BigInteger tomicsToTier2 = tomicsOverflowOverTier1.multiply(BigInteger.valueOf(2));
        final BigDecimal paymentToTier1 = monitorService.convertTomicsToUsd(tt1.getTomicsMax(), tt1.getDiscount());
        final BigDecimal paymentToTier2 = monitorService.convertTomicsToUsd(tomicsToTier2, tt2.getDiscount());
        final BigDecimal payment = paymentToTier1.add(paymentToTier2);
        final BigDecimal overflow = paymentToTier2.divide(new BigDecimal(2), new MathContext(6, RoundingMode.HALF_EVEN));

        // test
        PaymentLog log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() == null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(totalTomicsAmount()));
        assertEquals(0, log.getEligibleForRefund().getUsdAmount().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        tt1.assertTier();
        tt2.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testWithFiveTiers() throws Throwable {

        when(appConfig.getTotalTokenAmount())
                .thenReturn(new BigDecimal("149303520"));

        List<TestTier> tiers = createFiveTiers();

        // Fill 3/4 of presale tier
        Date blockTime = Date.valueOf("2018-08-02");
        makeAndConvertPaymentFailingOnOverflow(tiers.get(0), blockTime, 3);
        blockTime = Date.valueOf("2018-08-31");
        makeAndConvertPaymentFailingOnOverflow(tiers.get(0), blockTime, 5);
        tiers.forEach(TestTier::assertTier);

        // Fill 3/4 of first tier
        blockTime = Date.valueOf("2018-09-01");
        makeAndConvertPaymentFailingOnOverflow(tiers.get(1), blockTime, 3);
        blockTime = Date.valueOf("2018-09-28");
        makeAndConvertPaymentFailingOnOverflow(tiers.get(1), blockTime, 5);
        tiers.forEach(TestTier::assertTier);

        // Fill first tier. Dates shifted two days after this test.
        blockTime = Date.valueOf("2018-09-29");
        BigInteger tomicsFromTier1 = tiers.get(1).getTier().getRemainingTomics();
        BigDecimal paymentToTier1 = monitorService.convertTomicsToUsd(tomicsFromTier1, tiers.get(1).getDiscount());
        tiers.get(1).mustBeFull();
        long dateShift = tiers.get(1).getTier().getEndDate().getTime() - blockTime.getTime();
        tiers.get(1).newEndDateMustBe(blockTime);
        tiers.get(2).datesMustBeShiftedBy(dateShift);
        tiers.get(3).datesMustBeShiftedBy(dateShift);
        tiers.get(4).datesMustBeShiftedBy(dateShift);
        tiers.get(5).datesMustBeShiftedBy(dateShift);
        tiers.get(2).tomicsMaxMustBe(totalTomicsAmount()
                .subtract(tiers.get(0).getTier().getTomicsSold())
                .subtract(tiers.get(1).getTomicsMax()));

        PaymentLog log = createPaymentLog(paymentToTier1, blockTime);
        log = monitorService.allocateTokens(log);
        assertEquals(0, tomicsFromTier1.compareTo(log.getAllocatedTomics()));
        if (log.getEligibleForRefund() != null) fail();
        tiers.forEach(TestTier::assertTier);

        // Skip second tier and add to third tier directly.
        blockTime = Date.valueOf("2018-10-03");
        BigInteger overallRemainingTomics = totalTomicsAmount().subtract(saleTierService.getTotalTomicsSold());
        BigInteger tomicsFromTier3 = overallRemainingTomics.divide(BigInteger.valueOf(3));
        BigDecimal paymentToTier3 = monitorService.convertTomicsToUsd(tomicsFromTier3, tiers.get(3).getDiscount());
        tiers.get(3).tomicsMaxMustBe(overallRemainingTomics);
        tiers.get(3).tomicsSoldMustBe(tomicsFromTier3);
        log = createPaymentLog(paymentToTier3, blockTime);
        log = monitorService.allocateTokens(log);
        assertEquals(0, tomicsFromTier3.compareTo(log.getAllocatedTomics()));
        if (log.getEligibleForRefund() != null) fail();
        tiers.forEach(TestTier::assertTier);

        // Distibute all remaining tokens and overflow to fourth tier.
        blockTime = Date.valueOf("2018-10-09");
        overallRemainingTomics = totalTomicsAmount().subtract(saleTierService.getTotalTomicsSold());
        BigDecimal paymentToTier4 = monitorService.convertTomicsToUsd(overallRemainingTomics, tiers.get(4).getDiscount());
        BigDecimal payment = paymentToTier4.add(BigDecimal.TEN);
        tiers.get(4).tomicsMaxMustBe(overallRemainingTomics);
        tiers.get(4).mustBeFull();
        dateShift = tiers.get(4).getTier().getEndDate().getTime() - blockTime.getTime();
        tiers.get(4).newEndDateMustBe(blockTime);
        tiers.get(5).datesMustBeShiftedBy(dateShift);
        tiers.get(5).newEndDateMustBe(blockTime);

        log = createPaymentLog(payment, blockTime);
        log = monitorService.allocateTokens(log);
        assertEquals(0, overallRemainingTomics.compareTo(log.getAllocatedTomics()));
        if (log.getEligibleForRefund() == null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(overallRemainingTomics));
        assertEquals(0, log.getEligibleForRefund().getUsdAmount().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(BigDecimal.TEN));
        tiers.forEach(TestTier::assertTier);
        assertEquals(0, totalTomicsAmount().compareTo(saleTierService.getTotalTomicsSold()));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testConcurrentPaymentsWithoutOverflow() throws InterruptedException {
        final Date blockTime = Date.valueOf("1970-01-02");
        final TestTier t = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"),
                totalTomicsAmount().divide(new BigInteger("2")), true, false);

        int nrOfPayments = 10;
        BigDecimal singlePayment = BigDecimal.ONE;
        BigInteger singleSoldTomics = monitorService.convertUsdToTomics(singlePayment, t.getDiscount()).toBigInteger();
        t.tomicsSoldMustBe(singleSoldTomics.multiply(BigInteger.valueOf(nrOfPayments)));
        Investor investor = createInvestor();
        ThreadTestUtils.runMultiThread(
                () -> {
                    try {
                        PaymentLog log = createPaymentLog(singlePayment, blockTime, investor);
                        log = ethereumMonitor.allocateTokensWithRetries(log);
                        if (log.getEligibleForRefund() != null) fail();
                        assertEquals(0, log.getAllocatedTomics().compareTo(singleSoldTomics));
                        LOG.info("Distributed tomics: {}", log.getAllocatedTomics().toString());
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }, nrOfPayments
        );
        t.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testConcurrentPaymentsWithOverflowOverFirstTier() throws InterruptedException {
        final Date blockTime = Date.valueOf("1970-01-02");

        final TestTier t1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"),
                totalTomicsAmount().divide(new BigInteger("2")), true, false);

        final TestTier t2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"),
                totalTomicsAmount().divide(new BigInteger("2")), false, false);

        int nrOfPayments = 11;
        BigInteger singleSoldTomicsToTier1 = t1.getTomicsMax().divide(BigInteger.valueOf(nrOfPayments - 1));
        BigDecimal singlePayment = monitorService.convertTomicsToUsd(singleSoldTomicsToTier1,
                t1.getDiscount());
        BigInteger singleSoldTomicsToTier2 = monitorService.convertUsdToTomics(singlePayment, t2.getDiscount()).toBigInteger();

        t1.mustBeFull();
        t1.tomicsSoldMustBe(t1.getTomicsMax());
        t1.newEndDateMustBe(blockTime);
        t2.tomicsSoldMustBe(singleSoldTomicsToTier2);
        t2.datesMustBeShiftedBy(t1.initialEndDate.getTime() - blockTime.getTime());
        Investor investor = createInvestor();
        ThreadTestUtils.runMultiThread(
                () -> {
                    try {
                        PaymentLog log = createPaymentLog(singlePayment, blockTime, investor);
                        log = ethereumMonitor.allocateTokensWithRetries(log);
                        if (log.getEligibleForRefund() != null) fail();
                        LOG.info("Distributed tomics: {}", log.getAllocatedTomics().toString());
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }, nrOfPayments
        );
        t1.assertTier();
        t2.assertTier();
    }

    private List<TestTier> createFiveTiers() {
        List<TestTier> tiers = new ArrayList<>();
        tiers.add(new TestTier(0, "2018-08-01", "2018-09-01", new BigDecimal("0.25"),
                new BigInteger("47777126400000000000000000"), false, false));

        tiers.add(new TestTier(1, "2018-09-01", "2018-10-01", new BigDecimal("0.25"),
                new BigInteger("5972140800000000000000000"), true, false));

        tiers.add(new TestTier(2, "2018-10-01", "2018-10-02", new BigDecimal("0.20"),
                BigInteger.ZERO, true, true));

        tiers.add(new TestTier(3, "2018-10-02", "2018-10-09", new BigDecimal("0.15"),
                BigInteger.ZERO, true, true));

        tiers.add(new TestTier(4, "2018-10-09", "2018-10-16", new BigDecimal("0.10"),
                BigInteger.ZERO, true, true));

        tiers.add(new TestTier(5, "2018-10-16", "2018-10-23", BigDecimal.ZERO,
                BigInteger.ZERO, true, true));
        return tiers;
    }

    /**
     * Prepares and converts a payment in the amount of tokenMax (from the given tier) divided by the given divisor.
     * Fails if the conversion throws a TokenAllocationResult. Asserts the returned amount of tokens.
     * Sets the amount of tokens that have to be sold on the given test tier for later assertions.
     */
    private void makeAndConvertPaymentFailingOnOverflow(TestTier t, Date blockTime, int divisor) throws Throwable {
        BigInteger tomicsFromTier = t.getTomicsMax().divide(BigInteger.valueOf(divisor));
        BigDecimal payment = monitorService.convertTomicsToUsd(tomicsFromTier, t.getDiscount());
        t.tomicsSoldMustBe(t.tomicsSold.add(tomicsFromTier));
        PaymentLog log = createPaymentLog(payment, blockTime, createInvestor());
        log = monitorService.allocateTokens(log);
        if (log.getEligibleForRefund() != null) fail();
        assertEquals(0, log.getAllocatedTomics().compareTo(tomicsFromTier));
    }

    private Investor createInvestor() {
        try {
            return investorService.getInvestorByEmail(INVESTOR_EMAIL);
        } catch (InvestorNotFoundException e) {
            return investorService.saveRequireNewTransaction(
                    new Investor(new java.util.Date(), INVESTOR_EMAIL, "token",
                            "walletAddress", "payInEtherPublicKey", "payInBitcoinPublicKey",
                            "refundEtherAddress", "refundBitcoinAddress", "ipAddress"));
        }
    }

    private PaymentLog createPaymentLog(BigDecimal usdAmount, Date blockTime) {
        return createPaymentLog(usdAmount, blockTime, createInvestor());
    }

    private PaymentLog createPaymentLog(BigDecimal usdAmount, Date blockTime, Investor investor) {
        String txId = "txId0";
        java.util.Date creationDate = new java.util.Date();
        CurrencyType currency = CurrencyType.ETH;
        // Value doesn't matter, only the amount in usd matters.
        BigInteger weiAmount = BigInteger.ONE;
        BigInteger tomicsAmount = null;

        PaymentLog paymentLog = null;
        boolean succeeded = false;
        int i = 1;
        while (!succeeded) {
            try {
                paymentLog = paymentLogService.saveRequireNewTransaction(
                        new PaymentLog(txId, creationDate, currency, blockTime,
                                weiAmount, USD_FX_RATE, usdAmount, investor,
                                tomicsAmount, TransactionStatus.BUILDING));
                succeeded = true;
            } catch (DataIntegrityViolationException ignore) {
            }
            txId = "txId" + i++;
        }
        return paymentLog;
    }

    private BigInteger tomicsFactor() {
        return appConfig.getAtomicUnitFactor();
    }

    private BigInteger totalTomicsAmount() {
        return monitorService.convertTokensToTomics(appConfig.getTotalTokenAmount())
                .toBigInteger();
    }

    private class TestTier {

        private long tierNo;
        private Date initialStartDate;
        private Date initialEndDate;
        private Date newEndDate;
        private Date newStartDate;
        private boolean mustBeFull = false;
        private BigInteger tomicsSold = BigInteger.ZERO;
        private BigInteger initialTomicsMax;
        private BigInteger newTomicsMax;

        public TestTier(int tierNo, String start, String end, BigDecimal discount, BigInteger tomicsMax,
                        boolean hasDynamicDuration, boolean hasDynamicMax) {

            Date startDate = Date.valueOf(start);
            Date endDate = Date.valueOf(end);
            createAndSaveSaleTier(tierNo, startDate, endDate, discount, tomicsMax, hasDynamicDuration, hasDynamicMax);
            this.tierNo = tierNo;
            this.initialEndDate = endDate;
            this.initialStartDate = startDate;
            this.initialTomicsMax = tomicsMax;
        }

        public void assertTier() {
            LOG.info("Asserting Tier {}", tierNo);
            if (newStartDate != null)
                assertEquals(0, newStartDate.compareTo(getTier().getStartDate()));
            else assertEquals(0, initialStartDate.compareTo(getTier().getStartDate()));

            if (newEndDate != null) assertEquals(0, newEndDate.compareTo(getTier().getEndDate()));
            else assertEquals(0, initialEndDate.compareTo(getTier().getEndDate()));

            LOG.info("{} tomics sold on Tier {}", getTier().getTomicsSold(), tierNo);
            LOG.info("{} tomics should have been sold on Tier {}", tomicsSold, tierNo);
            if (tomicsSold != null)
                assertEquals(0, tomicsSold.compareTo(getTier().getTomicsSold()));

            if (mustBeFull) assertTrue(getTier().isFull());
            else assertFalse(getTier().isFull());

            if (newTomicsMax != null)
                assertEquals(0, newTomicsMax.compareTo(getTier().getTomicsMax()));
            else assertEquals(0, initialTomicsMax.compareTo(getTier().getTomicsMax()));
        }

        private SaleTier getTier() {
            return saleTierRepository.findById(this.tierNo)
                    .orElseThrow(() -> new IllegalStateException("Sale tier with number " + this.tierNo +
                            " does not exist."));
        }

        public BigDecimal getDiscount() {
            return getTier().getDiscount();
        }

        public BigInteger getTomicsMax() {
            return getTier().getTomicsMax();
        }

        private SaleTier createAndSaveSaleTier(int tierNo, Date startDate, Date endDate, BigDecimal discount, BigInteger tomicsMax,
                                               boolean hasDynamicDuration, boolean hasDynamicMax) {
            SaleTier t = new SaleTier(
                    tierNo,
                    "test tier " + tierNo,
                    startDate,
                    endDate,
                    discount,
                    BigInteger.ZERO,
                    tomicsMax,
                    hasDynamicDuration,
                    hasDynamicMax);
            return saleTierService.saveRequireTransaction(t);
        }

        public void newStartDateMustBe(Date date) {
            this.newStartDate = date;
        }

        public void newEndDateMustBe(Date date) {
            this.newEndDate = date;
        }

        public void mustBeFull() {
            this.mustBeFull = true;
            tomicsSoldMustBe(newTomicsMax);
        }

        public void datesMustBeShiftedBy(long dateShift) {
            newStartDateMustBe(new Date(getTier().getStartDate().getTime() - dateShift));
            newEndDateMustBe(new Date(getTier().getEndDate().getTime() - dateShift));
        }

        public void tomicsSoldMustBe(BigInteger tomicsToSell) {
            this.tomicsSold = tomicsToSell;
        }

        public void tomicsMaxMustBe(BigInteger tomicsMax) {
            newTomicsMax = tomicsMax;
        }
    }
}
