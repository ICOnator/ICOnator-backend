package io.iconator.monitor;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.BaseMonitorTestConfig;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.TokenConversionService;
import io.iconator.monitor.service.exceptions.TokenCapOverflowException;
import io.iconator.monitor.token.TokenUnit;
import io.iconator.monitor.token.TokenUnitConverter;
import io.iconator.monitor.token.TokenUtils;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseMonitorTestConfig.class, TokenConversionService.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class TokenConversionServiceTest {

    @Autowired
    private TokenConversionService tokenConversionService;

    @Autowired
    private SaleTierRepository saleTierRepository;

    @MockBean
    private MonitorAppConfig appConfig;


    @Before
    public void setUp() {
        when(appConfig.getOverallTokenAmount())
                .thenReturn(new BigInteger("100000000"));
        when(appConfig.getUsdPerToken())
                .thenReturn(new BigDecimal("0.1"));
    }

    @After
    public void cleanUp() {
        saleTierRepository.deleteAll();
    }

    @Test
    public void testNoTierAvailableAtDate() {
        try {
            tokenConversionService.convert(BigDecimal.TEN, Date.valueOf("1970-01-01"));
        } catch (TokenCapOverflowException e) {
            assertEquals(0, e.getConvertedTokens().compareTo(BigInteger.ZERO));
            assertEquals(0, e.getOverflow().compareTo(BigDecimal.TEN));
            return;
        }
        fail();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    // Persist directly to DB without transactions.
    public void testDistributeToSingleTier() {
        // tier setup
        TestTier tt = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.2"),
                new BigInteger("1000").multiply(unitFactor()), true, false);
        Date blockTime = Date.valueOf("1970-01-02");
        final BigInteger tomicsToSell = tt.getTomicsMax().divide(BigInteger.valueOf(2));
        tt.tomicsSoldMustBe(tomicsToSell);

        // payment setup
        final BigDecimal payment = TokenUtils.convertTokensToUsd(tomicsToSell,
                usdPerToken(), tt.getDiscount());

        // test
        BigInteger tomicsSold = BigInteger.ZERO;
        try {
            tomicsSold = tokenConversionService.convert(payment, blockTime);
        } catch (TokenCapOverflowException e) {
            fail();
        }
        assertEquals(0, tomicsSold.compareTo(tomicsToSell));
        tt.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    // Persist directly to DB without transactions.
    public void testOverflowWithSingleTier() {
        // tier setup
        TestTier tt = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(unitFactor()), true, false);
        Date blockTime = Date.valueOf("1970-01-02");
        tt.newEndDateMustBe(blockTime);
        tt.mustBeFull();

        // payment setup
        final BigDecimal overflow = BigDecimal.TEN;
        final BigDecimal paymentToTier = TokenUtils.convertTokensToUsd(tt.getTomicsMax(), usdPerToken(), tt.getDiscount())
                .add(overflow);

        // test
        try {
            tokenConversionService.convert(paymentToTier, blockTime);
        } catch (TokenCapOverflowException e) {
            // rounding the resulting USD overflow because that will be the actual precision with which the overflow
            // will be stored for refunds.
            assertEquals(0, e.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
            assertEquals(0, e.getConvertedTokens().compareTo(tt.getTomicsMax()));
            tt.assertTier();
            return;
        }
        fail();
    }


    /**
     * Two tiers, one payment that spills into the second tier. First tier has dynamic duration behavior and therefore
     * adapts his end date and the next tier's start and end dates.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowIntoSecondTierDynamicDuration() {
        // tier setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(unitFactor()), true, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.newEndDateMustBe(blockTime);
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1000").multiply(unitFactor()), false, false);
        BigInteger tomicsToSellFromTier2 = tt2.getTomicsMax().divide(BigInteger.valueOf(2));
        tt2.datesMustBeShiftedBy(tt1.getTier().getEndDate().getTime() - blockTime.getTime());
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);

        // first payment setup
        final BigDecimal paymentToTier1 = TokenUtils.convertTokensToUsd(tomicsToSellFromTier1, usdPerToken(), tt1.getDiscount());
        final BigDecimal paymentToTier2 = TokenUtils.convertTokensToUsd(tomicsToSellFromTier2, usdPerToken(), tt2.getDiscount());

        // test
        BigInteger tomicsSold = BigInteger.ZERO;
        try {
            tomicsSold = tokenConversionService.convert(paymentToTier1.add(paymentToTier2), blockTime);
        } catch (TokenCapOverflowException e) {
            fail();
        }
        assertEquals(0, tomicsSold.compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        // second payment
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
        // tier setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(unitFactor()), false, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1000").multiply(unitFactor()), false, false);
        final BigInteger tomicsToSellFromTier2 = tt2.getTomicsMax().divide(BigInteger.valueOf(2));
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);

        // payment setup
        final BigDecimal paymentToTier1 = TokenUtils.convertTokensToUsd(tomicsToSellFromTier1, usdPerToken(), tt1.getDiscount());
        final BigDecimal paymentToTier2 = TokenUtils.convertTokensToUsd(tomicsToSellFromTier2, usdPerToken(), tt2.getDiscount());

        // test
        BigInteger tomicsSold = BigInteger.ZERO;
        try {
            tomicsSold = tokenConversionService.convert(paymentToTier1.add(paymentToTier2), blockTime);
        } catch (TokenCapOverflowException e) {
            fail();
        }
        assertEquals(0, tomicsSold.compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        // second payment
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
        // tier setup
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(unitFactor()), false, false);
        final BigInteger tomicsToSellFromTier1 = tt1.getTomicsMax();
        tt1.mustBeFull();
        tt1.tomicsSoldMustBe(tomicsToSellFromTier1);

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), BigInteger.ZERO, false, true);
        BigInteger tomicsMax = overallTomicsAmount().subtract(tomicsToSellFromTier1);
        final BigInteger tomicsToSellFromTier2 = tomicsMax.divide(BigInteger.valueOf(2));
        tt2.tomicsSoldMustBe(tomicsToSellFromTier2);
        tt2.tomicsMaxMustBe(tomicsMax);

        // payment setup
        final BigDecimal paymentToTier1 = TokenUtils.convertTokensToUsd(tomicsToSellFromTier1, usdPerToken(), tt1.getDiscount());
        final BigDecimal paymentToTier2 = TokenUtils.convertTokensToUsd(tomicsToSellFromTier2, usdPerToken(), tt2.getDiscount());

        // test
        BigInteger tomicsSold = BigInteger.ZERO;
        try {
            tomicsSold = tokenConversionService.convert(paymentToTier1.add(paymentToTier2), blockTime);
        } catch (TokenCapOverflowException e) {
            fail();
        }
        assertEquals(0, tomicsSold.compareTo(tomicsToSellFromTier1.add(tomicsToSellFromTier2)));
        tt1.assertTier();
        tt2.assertTier();

        // second payment
        makeAndConvertPaymentFailingOnOverflow(tt2, blockTime, 4);
        tt1.assertTier();
        tt2.assertTier();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowMultipleStaticTiers() {
        Date blockTime = Date.valueOf("1970-01-02");

        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(unitFactor()), false, false);
        tt1.mustBeFull();

        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1500").multiply(unitFactor()), false, false);
        tt2.mustBeFull();

        TestTier tt3 = new TestTier(3, "1970-01-05", "1970-01-07", BigDecimal.ZERO, new BigInteger("2000").multiply(unitFactor()), false, false);
        tt3.mustBeFull();

        // payment setup
        final BigDecimal overflow = BigDecimal.TEN;
        final BigDecimal payment = TokenUtils.convertTokensToUsd(tt1.getTomicsMax(), usdPerToken(), tt1.getDiscount())
                .add(TokenUtils.convertTokensToUsd(tt2.getTomicsMax(), usdPerToken(), tt2.getDiscount()))
                .add(TokenUtils.convertTokensToUsd(tt3.getTomicsMax(), usdPerToken(), tt3.getDiscount()))
                .add(overflow);

        // test
        BigInteger result = null;
        try {
            result = tokenConversionService.convert(payment, blockTime);
        } catch (TokenCapOverflowException e) {
            assertEquals(0, e.getConvertedTokens().compareTo(
                    tt1.getTomicsMax().add(tt2.getTomicsMax()).add(tt3.getTomicsMax())));
            assertEquals(0, e.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
            tt1.assertTier();
            tt2.assertTier();
            tt3.assertTier();
        }
        if (result != null) fail();

        // second payment
        try {
            tokenConversionService.convert(BigDecimal.TEN, Date.valueOf("1970-01-06"));
        } catch (TokenCapOverflowException e) {
            assertEquals(0, e.getConvertedTokens().compareTo(BigInteger.ZERO));
            assertEquals(0, e.getOverflow().compareTo(BigDecimal.TEN));
            return;
        }
        fail();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowOverallTokenAmountAndTier() {

        final Date blockTime = Date.valueOf("1970-01-02");
        final TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"),
                overallTomicsAmount().add(TokenUnit.MAIN.getUnitFactor().toBigInteger()), true, false);
        tt1.tomicsSoldMustBe(overallTomicsAmount());

        // payment setup
        final BigDecimal usdAmountConverted = TokenUtils.convertTokensToUsd(overallTomicsAmount(), usdPerToken(), tt1.getDiscount());
        final BigDecimal overflowOverTier = BigDecimal.TEN;
        final BigDecimal payment = TokenUtils.convertTokensToUsd(tt1.getTomicsMax(), usdPerToken(), tt1.getDiscount())
                .add(overflowOverTier);
        final BigDecimal overflow = payment.subtract(usdAmountConverted);

        try {
            tokenConversionService.convert(payment, blockTime);
        } catch (TokenCapOverflowException e) {
            assertEquals(0, e.getConvertedTokens().compareTo(overallTomicsAmount()));
            assertEquals(0, e.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
            tt1.assertTier();
            return;
        }
        fail();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowOverallTokenAmountButNotTier() {

        final Date blockTime = Date.valueOf("1970-01-02");
        final TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"),
                overallTomicsAmount().add(TokenUnit.MAIN.getUnitFactor().toBigInteger()), true, false);
        tt1.tomicsSoldMustBe(overallTomicsAmount());

        // payment setup

        BigInteger tomicsOverflow = TokenUnit.MAIN.getUnitFactor().divide(new BigDecimal(2)).toBigInteger();
        final BigDecimal usdAmountConverted = TokenUtils.convertTokensToUsd(overallTomicsAmount(), usdPerToken(), tt1.getDiscount());
        final BigDecimal payment = TokenUtils.convertTokensToUsd(
                overallTomicsAmount().add(tomicsOverflow), usdPerToken(), tt1.getDiscount());
        final BigDecimal overflow = payment.subtract(usdAmountConverted);

        // test
        try {
            tokenConversionService.convert(payment, blockTime);
        } catch (TokenCapOverflowException e) {
            assertEquals(0, e.getConvertedTokens().compareTo(overallTomicsAmount()));
            assertEquals(0, e.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
            tt1.assertTier();
            return;
        }
        fail();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testOverflowOverallTokenAmountInSecondTier() {

        final BigInteger tomicsOverflowOverTier1 = TokenUnit.MAIN.getUnitFactor().toBigInteger();
        final Date blockTime = Date.valueOf("1970-01-02");
        final TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"),
                overallTomicsAmount().subtract(tomicsOverflowOverTier1), true, false);
        tt1.mustBeFull();
        tt1.newEndDateMustBe(blockTime);

        final TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.10"),
                overallTomicsAmount(), true, false);
        tt2.tomicsSoldMustBe(tomicsOverflowOverTier1);
        tt2.datesMustBeShiftedBy(tt1.getTier().getEndDate().getTime() - blockTime.getTime());

        // payment setup
        final BigInteger tomicsToTier2 = tomicsOverflowOverTier1.multiply(BigInteger.valueOf(2));
        final BigDecimal paymentToTier1 = TokenUtils.convertTokensToUsd(tt1.getTomicsMax(), usdPerToken(), tt1.getDiscount());
        final BigDecimal paymentToTier2 = TokenUtils.convertTokensToUsd(tomicsToTier2, usdPerToken(), tt2.getDiscount());
        final BigDecimal payment = paymentToTier1.add(paymentToTier2);
        final BigDecimal overflow = paymentToTier2.divide(new BigDecimal(2), new MathContext(6, RoundingMode.HALF_EVEN));

        // test
        try {
            tokenConversionService.convert(payment, blockTime);
        } catch (TokenCapOverflowException e) {
            assertEquals(0, e.getConvertedTokens().compareTo(overallTomicsAmount()));
            assertEquals(0, e.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
            tt1.assertTier();
            tt2.assertTier();
            return;
        }
        fail();
    }

//    @Test
//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
//    public void testOverflowWithFiveTiersAllPossibleConfigs() {
//
//        Date blockTime = Date.valueOf("1970-01-02");
//
//        TestTier tt1 = new TestTier(1, "1970-01-01", "1970-01-03", new BigDecimal("0.25"), new BigInteger("1000").multiply(unitFactor()), false, false);
//        tt1.mustBeFull();
//
//        TestTier tt2 = new TestTier(2, "1970-01-03", "1970-01-05", new BigDecimal("0.1"), new BigInteger("1500").multiply(unitFactor()), false, false);
//        tt2.mustBeFull();
//
//        TestTier tt3 = new TestTier(3, "1970-01-05", "1970-01-07", BigDecimal.ZERO, new BigInteger("2000").multiply(unitFactor()), false, false);
//        tt3.mustBeFull();
//
//        // payment setup
//        final BigDecimal overflow = BigDecimal.TEN;
//        final BigDecimal payment = TokenUtils.convertTokensToUsd(tt1.getTomicsMax(), usdPerToken(), tt1.getDiscount())
//                .add(TokenUtils.convertTokensToUsd(tt2.getTomicsMax(), usdPerToken(), tt2.getDiscount()))
//                .add(TokenUtils.convertTokensToUsd(tt3.getTomicsMax(), usdPerToken(), tt3.getDiscount()))
//                .add(overflow);
//
//        // test
//        BigInteger result = null;
//        try {
//            result = tokenConversionService.convert(payment, blockTime);
//        } catch (TokenCapOverflowException e) {
//            assertEquals(0, e.getConvertedTokens().compareTo(
//                    tt1.getTomicsMax().add(tt2.getTomicsMax()).add(tt3.getTomicsMax())));
//            assertEquals(0, e.getOverflow().round(new MathContext(6, RoundingMode.HALF_EVEN)).compareTo(overflow));
//            tt1.assertTier();
//            tt2.assertTier();
//            tt3.assertTier();
//        }
//        if (result != null) fail();
//
//        // second payment
//        try {
//            tokenConversionService.convert(BigDecimal.TEN, Date.valueOf("1970-01-06"));
//        } catch (TokenCapOverflowException e) {
//            assertEquals(0, e.getConvertedTokens().compareTo(BigInteger.ZERO));
//            assertEquals(0, e.getOverflow().compareTo(BigDecimal.TEN));
//            return;
//        }
//        fail();
//    }

    private BigDecimal usdPerToken() {
        return appConfig.getUsdPerToken();
    }

    private BigInteger unitFactor() {
        return TokenUnit.MAIN.getUnitFactor().toBigInteger();
    }

    private BigInteger overallTomicsAmount() {
        return TokenUnitConverter.convert(appConfig.getOverallTokenAmount(), TokenUnit.MAIN, TokenUnit.SMALLEST)
                .toBigInteger();
    }

    /**
     * Prepares and converts a payment in the amount of tokenMax (from the given tier) divided by the given divisor.
     * Fails if the conversion throws a TokenCapOverflowException. Asserts the returned amount of tokens.
     * Sets the amount of tokens that have to be sold on the given test tier for later assertions.
     */
    private BigInteger makeAndConvertPaymentFailingOnOverflow(TestTier t, Date blockTime, int divisor) {
        BigInteger tomicsToSellFromTier = t.getTomicsMax().divide(BigInteger.valueOf(divisor));
        BigDecimal payment = TokenUtils.convertTokensToUsd(tomicsToSellFromTier, usdPerToken(), t.getDiscount());
        t.tomicsSoldMustBe(t.tomicsSold.add(tomicsToSellFromTier));
        BigInteger tomicsSold = BigInteger.ZERO;
        try {
            tomicsSold = tokenConversionService.convert(payment, blockTime);
        } catch (TokenCapOverflowException e) {
            fail();
        }
        assertEquals(0, tomicsSold.compareTo(tomicsToSellFromTier));
        return tomicsSold;
    }

    private class TestTier {

        private long tierNo;
        private Date initialStartDate;
        private Date initialEndDate;
        private Date newEndDate;
        private Date newStartDate;
        private boolean mustBeFull = false;
        private BigInteger tomicsSold;
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
                assertEquals(0, tomicsSold.compareTo(getTier().getTokensSold()));

            if (mustBeFull) assertTrue(getTier().isFull());
            else assertFalse(getTier().isFull());

            if (newTomicsMax != null)
                assertEquals(0, newTomicsMax.compareTo(getTier().getTokenMax()));
            else assertEquals(0, initialTomicsMax.compareTo(getTier().getTokenMax()));
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
            return getTier().getTokenMax();
        }

        private SaleTier createAndSaveSaleTier(int tierNo, Date startDate, Date endDate, BigDecimal discount, BigInteger tokenMax,
                                               boolean hasDynamicDuration, boolean hasDynamicMax) {
            SaleTier t = new SaleTier(
                    tierNo,
                    "test tier " + tierNo,
                    startDate,
                    endDate,
                    discount,
                    BigInteger.ZERO,
                    tokenMax,
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
