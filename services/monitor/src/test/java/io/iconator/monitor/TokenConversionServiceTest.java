package io.iconator.monitor;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.BaseMonitorTestConfig;
import io.iconator.monitor.service.TokenConversionService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseMonitorTestConfig.class, TokenConversionService.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class TokenConversionServiceTest {

    private final static BigDecimal T1_DISCOUNT = new BigDecimal("0.5");
//    private final static BigDecimal T2_DISCOUNT = new BigDecimal("0.2");
//    private final static BigDecimal T3_DISCOUNT = new BigDecimal("0.1");

    private final static int T1_NO = 1;
    private final static int T2_NO = 2;
    private final static int T3_NO = 3;

    private final static Date T1_START_DATE = Date.valueOf("2018-01-01");
    private final static Date T1_END_DATE = Date.valueOf("2018-01-10");
    private final static Date T2_START_DATE = Date.valueOf("2018-01-11");
    private final static Date T2_END_DATE = Date.valueOf("2018-01-20");
    private final static Date T3_START_DATE = Date.valueOf("2018-01-21");
    private final static Date T3_END_DATE = Date.valueOf("2018-01-30");

    private final static BigInteger T1_MAX = BigInteger.valueOf(1000L);
    private final static BigInteger T2_MAX = BigInteger.valueOf(2000L);
    private final static BigInteger T3_MAX = BigInteger.valueOf(3000L);

    @Autowired
    private TokenConversionService tokenConversionService;

    @Autowired
    private SaleTierRepository saleTierRepository;

//    @Test
//    public void testConvertUsdToTokens() {
//        BigDecimal usd = new BigDecimal("1");
//        BigDecimal discount = new BigDecimal("0.250000", new MathContext(6, RoundingMode.HALF_EVEN));
//        BigDecimal tokens = tokenConversionService.convertCurrencyToTokens(usd, discount);
//        BigDecimal expectedResult = new BigDecimal("4")
//                .divide(new BigDecimal("3"), new MathContext(34, RoundingMode.DOWN));
//
//        assertEquals(0, tokens.compareTo(expectedResult));
//    }
//
//    @Test
//    public void testConvertTokensToUsd() {
//        BigDecimal tokens = new BigDecimal("3.333");
//        BigDecimal discount = new BigDecimal("0.333333", new MathContext(6, RoundingMode.HALF_EVEN));
//        BigDecimal usd = tokenConversionService.convertTokensToCurrency(tokens, discount);
//
//        assertEquals(0, usd.compareTo(new BigDecimal("2.222001111")));
//    }
//
//    @Test
//    public void testBuyTokensNotExceedingFirstTier() {
//        final BigDecimal currency = new BigDecimal(200);
//        final BigInteger expectedTokensSold = BigInteger.valueOf(400);
//        final Date blockTime = Date.valueOf("2018-01-02");
//        ConversionResult result = null;
//        try {
//            result = tokenConversionService.convertToTokensAndUpdateTiers(currency, blockTime);
//        } catch (Throwable e) {
//            fail(e.getMessage());
//        }
//        assertEquals(0, result.getOverflow().compareTo(BigDecimal.ZERO));
//        assertEquals(0, result.getTokens().compareTo(expectedTokensSold));
//
//        Optional<SaleTier> oActiveTier = saleTierRepository.findActiveTierByDate(blockTime);
//        if (oActiveTier.isPresent()) {
//            assertTier(oActiveTier.get(), T1_NO, T1_START_DATE, T1_END_DATE, expectedTokensSold);
//        } else {
//            fail("Should have found active tier, but didn't.");
//        }
//
//        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T2_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T2_NO, T2_START_DATE, T2_END_DATE, BigInteger.ZERO);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T2_NO));
//        }
//
//        oTier = saleTierRepository.findByTierNo(T3_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T3_NO, T3_START_DATE, T3_END_DATE, BigInteger.ZERO);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T3_NO));
//        }
//    }
//
//    @Test
//    public void testBuyTokensFillingUpFirstTier() {
//        final BigDecimal currency = BigDecimal.valueOf(500L);
//        final Date blockTime = Date.valueOf("2018-01-02");
//        ConversionResult result = null;
//        try {
//            result = tokenConversionService.convertToTokensAndUpdateTiers(currency, blockTime);
//        } catch (Throwable e) {
//            fail(e.getMessage());
//        }
//        assertEquals(0, result.getOverflow().compareTo(BigDecimal.ZERO));
//        assertEquals(0, result.getTokens().compareTo(T1_MAX));
//
//        Optional<SaleTier> oActiveTier = saleTierRepository.findActiveTierByDate(blockTime);
//        if (oActiveTier.isPresent()) {
//            assertTier(oActiveTier.get(), T2_NO, blockTime, T2_END_DATE, BigInteger.ZERO);
//        } else {
//            fail("Should have found active tier, but didn't.");
//        }
//
//        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T1_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T1_NO, T1_START_DATE, blockTime, T1_MAX);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T1_NO));
//        }
//
//        oTier = saleTierRepository.findByTierNo(T3_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T3_NO, T3_START_DATE, T3_END_DATE, BigInteger.ZERO);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T3_NO));
//        }
//    }
//
//    @Test
//    public void testBuyTokensReachingIntoSecondTier() {
//        final BigDecimal currency = BigDecimal.valueOf(500L + 160L);
//        final BigInteger tokensFromT2 = BigInteger.valueOf(200L);
//        final Date blockTime = Date.valueOf("2018-01-02");
//        ConversionResult result = null;
//        try {
//            result = tokenConversionService.convertToTokensAndUpdateTiers(currency, blockTime);
//        } catch (Throwable e) {
//            fail(e.getMessage());
//        }
//        assertEquals(0, result.getOverflow().compareTo(BigDecimal.ZERO));
//        assertEquals(0, result.getTokens().compareTo(T1_MAX.add(tokensFromT2)));
//
//        Optional<SaleTier> oActiveTier = saleTierRepository.findActiveTierByDate(blockTime);
//
//        if (oActiveTier.isPresent()) {
//            assertTier(oActiveTier.get(), T2_NO, blockTime, T2_END_DATE, tokensFromT2);
//        } else {
//            fail("Should have found active tier, but didn't.");
//        }
//
//        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T1_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T1_NO, T1_START_DATE, blockTime, T1_MAX);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T1_NO));
//        }
//
//        oTier = saleTierRepository.findByTierNo(T3_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T3_NO, T3_START_DATE, T3_END_DATE, BigInteger.ZERO);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T3_NO));
//        }
//    }
//
//    @Test
//    public void testMultipleBuyTokensReachingIntoSecondTier() {
//        final BigDecimal currency1 = BigDecimal.valueOf(400L);
//        final BigDecimal currency2 = BigDecimal.valueOf(100L + 20L);
//        final BigInteger tokensFromT2 = BigInteger.valueOf(25L);
//        final Date blockTime1 = Date.valueOf("2018-01-02");
//        final Date blockTime2 = Date.valueOf("2018-01-03");
//
//        BigDecimal overflow = BigDecimal.ZERO;
//        BigInteger tokens = BigInteger.ZERO;
//        try {
//            ConversionResult result = tokenConversionService.convertToTokensAndUpdateTiers(currency1, blockTime1);
//            overflow = overflow.add(result.getOverflow());
//            tokens = tokens.add(result.getTokens());
//            result = tokenConversionService.convertToTokensAndUpdateTiers(currency2, blockTime2);
//            overflow = overflow.add(result.getOverflow());
//            tokens = tokens.add(result.getTokens());
//        } catch (Throwable e) {
//            fail(e.getMessage());
//        }
//        assertEquals(0, overflow.compareTo(BigDecimal.ZERO));
//        assertEquals(0, tokens.compareTo(T1_MAX.add(tokensFromT2)));
//
//        Optional<SaleTier> oActiveTier = saleTierRepository.findActiveTierByDate(blockTime2);
//        if (oActiveTier.isPresent()) {
//            assertTier(oActiveTier.get(), T2_NO, blockTime2, T2_END_DATE, tokensFromT2);
//        } else {
//            fail("Should have found active tier, but didn't.");
//        }
//
//        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T1_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T1_NO, T1_START_DATE, blockTime2, T1_MAX);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T1_NO));
//        }
//
//        oTier = saleTierRepository.findByTierNo(T3_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T3_NO, T3_START_DATE, T3_END_DATE, BigInteger.ZERO);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T3_NO));
//        }
//    }
//
//    @Test
//    public void testBuyTokensOverflowingThirdTier() {
//        final BigDecimal currency = BigDecimal.valueOf(500L + 1600L + 2790L);
//        final BigDecimal expectedOverflow = BigDecimal.valueOf(90L);
//        final Date blockTime = Date.valueOf("2018-01-02");
//        ConversionResult result = null;
//        try {
//            result = tokenConversionService.convertToTokensAndUpdateTiers(currency, blockTime);
//        } catch (Throwable e) {
//            fail(e.getMessage());
//        }
//        assertEquals(0, result.getOverflow().compareTo(expectedOverflow));
//        assertEquals(0, result.getTokens().compareTo(T1_MAX.add(T2_MAX).add(T3_MAX)));
//
//        Optional<SaleTier> oActiveTier = saleTierRepository.findActiveTierByDate(blockTime);
//
//        if (oActiveTier.isPresent()) {
//            fail("There shouldn't be any active tear anymore, but was.");
//        }
//
//        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T1_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T1_NO, T1_START_DATE, blockTime, T1_MAX);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T1_NO));
//        }
//
//        oTier = saleTierRepository.findByTierNo(T2_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T2_NO, blockTime, blockTime, T2_MAX);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T2_NO));
//        }
//
//        oTier = saleTierRepository.findByTierNo(T3_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T3_NO, blockTime, blockTime, T3_MAX);
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T3_NO));
//        }
//    }
//
//    @Test
//    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
//    public void testSimpleConcurrentConversions() throws InterruptedException {
//        int nrThreads = 10;
//        BigDecimal currency = BigDecimal.valueOf(nrThreads);
//        BigDecimal expectedTokensSold = tokenConversionService.convertCurrencyToTokens(currency, T1_DISCOUNT);
//        final Date blockTime = Date.valueOf("2018-01-02");
//        runConcurrent(() -> {
//            try {
//                tokenConversionService.convertToTokensAndUpdateTiers(BigDecimal.ONE, blockTime);
//            } catch (Throwable throwable) {
//                fail("Error while converting.");
//            }
//        }, nrThreads);
//
//        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T1_NO);
//        if (oTier.isPresent()) {
//            assertTier(oTier.get(), T1_NO, T1_START_DATE, T1_END_DATE, expectedTokensSold.toBigInteger());
//        } else {
//            fail(String.format("Should have found tier %d, but didn't.", T1_NO));
//        }
//    }

    private static void runConcurrent(Runnable runnable, int threadCount) throws InterruptedException {
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            threadList.add(new Thread(runnable));
        }
        for (int i = 0; i < threadCount; i++) {
            threadList.get(i).start();
        }
        for (int i = 0; i < threadCount; i++) {
            threadList.get(i).join();
        }
    }

    private void assertTier(SaleTier tier, int tierNo, Date startDate, Date endDate,
                            BigInteger tokensSold) {

        assertEquals(tierNo, tier.getTierNo());
        assertEquals(startDate, tier.getStartDate());
        assertEquals(endDate, tier.getEndDate());
        assertEquals(0, tokensSold.compareTo(tier.getTokensSold()));
    }
}
