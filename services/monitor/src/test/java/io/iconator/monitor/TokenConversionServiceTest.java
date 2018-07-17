package io.iconator.monitor;

import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.config.MonitorTestConfig;
import io.iconator.monitor.service.TokenConversionService;
import io.iconator.monitor.service.TokenConversionService.TokenDistributionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MonitorTestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class TokenConversionServiceTest {

    @Autowired
    private TokenConversionService tokenConversionService;

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    private SaleTierService saleTierService;

    @MockBean
    private MonitorAppConfig appConfig;


    @Before
    public void setUp() {
        when(appConfig.getTotalTokenAmount())
                .thenReturn(new BigDecimal("100000000"));
        when(appConfig.getUsdPerToken())
                .thenReturn(new BigDecimal("0.1"));
        when(appConfig.getAtomicUnitFactor())
                .thenReturn(BigInteger.TEN.pow(18));
    }

    @After
    public void cleanUp() {
        saleTierRepository.deleteAll();
    }


    @Test
    public void testConvertUsdToTomics() {
        BigDecimal usd = new BigDecimal("1");
        BigDecimal discount = new BigDecimal("0.25", new MathContext(6, RoundingMode.HALF_EVEN));

        BigDecimal tomics = tokenConversionService.convertUsdToTomics(usd, discount);
        BigDecimal expectedResult = new BigDecimal("40").multiply(new BigDecimal(tomicsFactor()))
                .divide(new BigDecimal("3"), new MathContext(34, RoundingMode.DOWN));

        assertEquals(0, tomics.compareTo(expectedResult));
    }

    @Test
    public void testConvertTomicsToUsd() {
        BigDecimal tomics = new BigDecimal("3.333").multiply(new BigDecimal(tomicsFactor()));
        BigDecimal discount = new BigDecimal("0.333333", new MathContext(6, RoundingMode.HALF_EVEN));

        BigDecimal usd = tokenConversionService.convertTomicsToUsd(tomics, discount);
        BigDecimal expectedResult = new BigDecimal("3.333")
                .multiply(BigDecimal.ONE.subtract(discount), MathContext.DECIMAL128)
                .multiply(appConfig.getUsdPerToken());
        assertEquals(0, usd.compareTo(expectedResult));
    }

    @Test
    public void testNoTierAvailableAtDate() {
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(BigDecimal.TEN, Date.valueOf("1970-01-01"));
        if (!r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(BigInteger.ZERO));
        assertEquals(0, r.getOverflow().compareTo(BigDecimal.TEN));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testDistributeToSingleTier() {
        // setup
        TestTier tt = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.2"),
                new BigInteger("1000").multiply(tomicsFactor()), true, false);
        Date blockTime = Date.valueOf("1970-01-02");
        final BigInteger tomicsToSell = tt.getTomicsMax().divide(BigInteger.valueOf(2));
        tt.tomicsSoldMustBe(tomicsToSell);
        final BigDecimal payment = tokenConversionService.convertTomicsToUsd(tomicsToSell, tt.getDiscount());

        // test
        TokenDistributionResult result = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (result.hasOverflow()) fail();
        assertEquals(0, result.getDistributedTomics().compareTo(tomicsToSell));
        tt.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowWithSingleTier() {
        // setup
        TestTier tt = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), true, false);
        Date blockTime = Date.valueOf("1970-01-02");
        tt.newEndDateMustBe(blockTime);
        tt.mustBeFull();
        final BigDecimal overflow = BigDecimal.TEN;
        final BigDecimal payment = tokenConversionService.convertTomicsToUsd(tt.getTomicsMax(), tt.getDiscount())
                .add(overflow);

        // test
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (!r.hasOverflow()) fail();
        // rounding the resulting USD overflow because that will be the actual precision with which the overflow
        // will be stored for refunds.
        assertEquals(0, r.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        assertEquals(0, r.getDistributedTomics().compareTo(tt.getTomicsMax()));
        tt.assertTier();
    }


    /**
     * Two tiers, one payment that spills into the second tier. First tier has dynamic duration behavior and therefore
     * adapts his end date and the next tier's start and end dates.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowIntoSecondTierDynamicDuration() {
        // setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), true, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.newEndDateMustBe(blockTime);
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        BigInteger tomicsToSellFromTier2 = tt2.getTomicsMax().divide(BigInteger.valueOf(2));
        tt2.datesMustBeShiftedBy(tt1.getTier().getEndDate().getTime() - blockTime.getTime());
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);

        final BigDecimal paymentToTier1 = tokenConversionService.convertTomicsToUsd(tomicsToSellFromTier1, tt1.getDiscount());
        final BigDecimal paymentToTier2 = tokenConversionService.convertTomicsToUsd(tomicsToSellFromTier2, tt2.getDiscount());
        BigDecimal payment = paymentToTier1.add(paymentToTier2);

        // test
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        makeAndConvertPaymentFailingOnOverflow(tt2, blockTime, 4);
        tt1.assertTier();
        tt2.assertTier();
    }


    /**
     * Two tiers, one payment that spills into the second tier. First tier has static duration behavior and therefore
     * doesn't change his end date nore the dates of the next tiers.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowIntoSecondTierStaticDuration() {
        // setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        final BigInteger tomicsToSellFromTier2 = tt2.getTomicsMax().divide(BigInteger.valueOf(2));
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);

        final BigDecimal paymentToTier1 = tokenConversionService.convertTomicsToUsd(tomicsToSellFromTier1, tt1.getDiscount());
        final BigDecimal paymentToTier2 = tokenConversionService.convertTomicsToUsd(tomicsToSellFromTier2, tt2.getDiscount());
        BigDecimal payment = paymentToTier1.add(paymentToTier2);

        // test
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        makeAndConvertPaymentFailingOnOverflow(tt2, blockTime, 4);
        tt1.assertTier();
        tt2.assertTier();
    }

    /**
     * Two tiers, one payment that spills into the second tier. Second tier has dynamic token max.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowIntoSecondTierDynamicMax() {
        // setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), BigInteger.ZERO, false, true);
        BigInteger tomicsMax = totalTomicsAmount().subtract(tomicsToSellFromTier1);
        final BigInteger tomicsToSellFromTier2 = tomicsMax.divide(BigInteger.valueOf(2));
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);
        tt2.tomicsMaxMustBe(tomicsMax);

        final BigDecimal paymentToTier1 = tokenConversionService.convertTomicsToUsd(tomicsToSellFromTier1, tt1.getDiscount());
        final BigDecimal paymentToTier2 = tokenConversionService.convertTomicsToUsd(tomicsToSellFromTier2, tt2.getDiscount());
        BigDecimal payment = paymentToTier1.add(paymentToTier2);

        // test
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        makeAndConvertPaymentFailingOnOverflow(tt2, blockTime, 4);
        tt1.assertTier();
        tt2.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowMultipleStaticTiers() {
        // setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(tomicsFactor()), false, false);
        tt1.mustBeFull();

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1500").multiply(tomicsFactor()), false, false);
        tt2.mustBeFull();

        TestTier tt3 = new TestTier(3, "1970-01-05", "1970-01-07", BigDecimal.ZERO, new BigInteger("2000").multiply(tomicsFactor()), false, false);
        tt3.mustBeFull();

        final BigDecimal overflow = BigDecimal.TEN;
        final BigDecimal payment = tokenConversionService.convertTomicsToUsd(tt1.getTomicsMax(), tt1.getDiscount())
                .add(tokenConversionService.convertTomicsToUsd(tt2.getTomicsMax(), tt2.getDiscount()))
                .add(tokenConversionService.convertTomicsToUsd(tt3.getTomicsMax(), tt3.getDiscount()))
                .add(overflow);

        // test
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (!r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(tt1.getTomicsMax().add(tt2.getTomicsMax()).add(tt3.getTomicsMax())));
        assertEquals(0, r.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        tt1.assertTier();
        tt2.assertTier();
        tt3.assertTier();

        r = tokenConversionService.convertAndDistributeToTiers(BigDecimal.TEN, Date.valueOf("1970-01-06"));
        if (!r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(BigInteger.ZERO));
        assertEquals(0, r.getOverflow().compareTo(BigDecimal.TEN));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowTotalTokenAmountAndTier() {
        // setup
        final Date blockTime = Date.valueOf("1970-01-02");
        // tier with a capacity of total token amount plus one token.
        final TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), totalTomicsAmount().add(tomicsFactor()), true, false);
        tt1.tomicsSoldMustBe(totalTomicsAmount());

        // only the total amount of tokens can be disttributed/converted.
        final BigDecimal usdAmountConverted = tokenConversionService.convertTomicsToUsd(totalTomicsAmount(), tt1.getDiscount());
        final BigDecimal overflowOverTier = BigDecimal.TEN;
        final BigDecimal payment = tokenConversionService.convertTomicsToUsd(tt1.getTomicsMax(), tt1.getDiscount())
                .add(overflowOverTier);
        final BigDecimal overflow = payment.subtract(usdAmountConverted);

        // test
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (!r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(totalTomicsAmount()));
        assertEquals(0, r.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        tt1.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowTotalTokenAmountButNotTier() {
        // setup
        final Date blockTime = Date.valueOf("1970-01-02");
        // tier with a capacity of total token amount plus one token.
        final TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), totalTomicsAmount().add(tomicsFactor()), true, false);
        tt1.tomicsSoldMustBe(totalTomicsAmount());

        final BigDecimal overflow = appConfig.getUsdPerToken().divide(new BigDecimal("2"));
        final BigDecimal payment = tokenConversionService.convertTomicsToUsd(totalTomicsAmount(), tt1.getDiscount()).add(overflow);

        // test
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (!r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(totalTomicsAmount()));
        assertEquals(0, r.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        tt1.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowTotalTokenAmountInSecondTier() {

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
        final BigDecimal paymentToTier1 = tokenConversionService.convertTomicsToUsd(tt1.getTomicsMax(), tt1.getDiscount());
        final BigDecimal paymentToTier2 = tokenConversionService.convertTomicsToUsd(tomicsToTier2, tt2.getDiscount());
        final BigDecimal payment = paymentToTier1.add(paymentToTier2);
        final BigDecimal overflow = paymentToTier2.divide(new BigDecimal(2), new MathContext(6, RoundingMode.HALF_EVEN));

        // test
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (!r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(totalTomicsAmount()));
        assertEquals(0, r.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
        tt1.assertTier();
        tt2.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testWithFiveTiers() {

        when(appConfig.getTotalTokenAmount())
                .thenReturn(new BigDecimal("149303520"));

        List<TestTier> tiers = createFiveTiers();

        // Fill 3/4 of presale tier
        Date blockTime = Date.valueOf("2018-08-02");
        makeAndConvertPaymentFailingOnOverflow(tiers.get(0), blockTime, 2);
        blockTime = Date.valueOf("2018-08-31");
        makeAndConvertPaymentFailingOnOverflow(tiers.get(0), blockTime, 4);
        tiers.forEach(TestTier::assertTier);

        // Fill 3/4 of first tier
        blockTime = Date.valueOf("2018-09-01");
        makeAndConvertPaymentFailingOnOverflow(tiers.get(1), blockTime, 2);
        blockTime = Date.valueOf("2018-09-28");
        makeAndConvertPaymentFailingOnOverflow(tiers.get(1), blockTime, 4);
        tiers.forEach(TestTier::assertTier);

        // Fill first tier. Dates shifted two days after this test.
        blockTime = Date.valueOf("2018-09-29");
        BigInteger tomicsFromTier1 = tiers.get(1).getTier().getRemainingTomics();
        BigDecimal paymentToTier1 = tokenConversionService.convertTomicsToUsd(tomicsFromTier1, tiers.get(1).getDiscount());
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

        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(paymentToTier1, blockTime);
        assertEquals(0, tomicsFromTier1.compareTo(r.getDistributedTomics()));
        if (r.hasOverflow()) fail();
        tiers.forEach(TestTier::assertTier);

        // Skip second tier and add to third tier directly.
        blockTime = Date.valueOf("2018-10-03");
        BigInteger overallRemainingTomics = totalTomicsAmount().subtract(saleTierService.getTotalTomicsSold());
        BigInteger tomicsFromTier3 = overallRemainingTomics.divide(BigInteger.valueOf(4));
        BigDecimal paymentToTier3 = tokenConversionService.convertTomicsToUsd(tomicsFromTier3, tiers.get(3).getDiscount());
        tiers.get(3).tomicsMaxMustBe(overallRemainingTomics);
        tiers.get(3).tomicsSoldMustBe(tomicsFromTier3);
        r = tokenConversionService.convertAndDistributeToTiers(paymentToTier3, blockTime);
        assertEquals(0, tomicsFromTier3.compareTo(r.getDistributedTomics()));
        if (r.hasOverflow()) fail();
        tiers.forEach(TestTier::assertTier);

        // Distibute all remaining tokens and overflow to fourth tier.
        blockTime = Date.valueOf("2018-10-09");
        overallRemainingTomics = totalTomicsAmount().subtract(saleTierService.getTotalTomicsSold());
        BigDecimal paymentToTier4 = tokenConversionService.convertTomicsToUsd(overallRemainingTomics, tiers.get(4).getDiscount());
        BigDecimal payment = paymentToTier4.add(BigDecimal.TEN);
        tiers.get(4).tomicsMaxMustBe(overallRemainingTomics);
        tiers.get(4).mustBeFull();
        dateShift = tiers.get(4).getTier().getEndDate().getTime() - blockTime.getTime();
        tiers.get(4).newEndDateMustBe(blockTime);
        tiers.get(5).datesMustBeShiftedBy(dateShift);
        tiers.get(5).newEndDateMustBe(blockTime);

        r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        assertEquals(0, overallRemainingTomics.compareTo(r.getDistributedTomics()));
        if (!r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(overallRemainingTomics));
        assertEquals(0, r.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(BigDecimal.TEN));
        tiers.forEach(TestTier::assertTier);
        assertEquals(0, totalTomicsAmount().compareTo(saleTierService.getTotalTomicsSold()));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    protected List<TestTier> createFiveTiers() {
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
     * Fails if the conversion throws a TokenDistributionResult. Asserts the returned amount of tokens.
     * Sets the amount of tokens that have to be sold on the given test tier for later assertions.
     */
    private void makeAndConvertPaymentFailingOnOverflow(TestTier t, Date blockTime, int divisor) {
        BigInteger tomicsFromTier = t.getTomicsMax().divide(BigInteger.valueOf(divisor));
        BigDecimal payment = tokenConversionService.convertTomicsToUsd(tomicsFromTier, t.getDiscount());
        t.tomicsSoldMustBe(t.tomicsSold.add(tomicsFromTier));
        TokenDistributionResult r = tokenConversionService.convertAndDistributeToTiers(payment, blockTime);
        if (r.hasOverflow()) fail();
        assertEquals(0, r.getDistributedTomics().compareTo(tomicsFromTier));
    }

    private BigInteger tomicsFactor() {
        return appConfig.getAtomicUnitFactor();
    }

    private BigInteger totalTomicsAmount() {
        return tokenConversionService.convertTokensToTomics(appConfig.getTotalTokenAmount())
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
            if (newStartDate != null)
                assertEquals(0, newStartDate.compareTo(getTier().getStartDate()));
            else assertEquals(0, initialStartDate.compareTo(getTier().getStartDate()));

            if (newEndDate != null) assertEquals(0, newEndDate.compareTo(getTier().getEndDate()));
            else assertEquals(0, initialEndDate.compareTo(getTier().getEndDate()));

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
            return saleTierRepository.saveAndFlush(t);
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
