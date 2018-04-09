package io.iconator.monitor;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.BaseMonitor.ConversionResult;
import io.iconator.monitor.config.TestConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class BaseMonitorTest {

    private final static double T1_DISCOUNT = 0.5;
    private final static double T2_DISCOUNT = 0.2;
    private final static double T3_DISCOUNT = 0.1;

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
    private BaseMonitor baseMonitor;

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Before
    public void setUpTiersInDb() {
        saleTierRepository.save(createTier(1, T1_START_DATE, T1_END_DATE, T1_DISCOUNT, 1000L, true));
        saleTierRepository.save(createTier(2, T2_START_DATE, T2_END_DATE, T2_DISCOUNT, 2000L, false));
        saleTierRepository.save(createTier(3, T3_START_DATE, T3_END_DATE, T3_DISCOUNT, 3000L, false));
    }

    @After
    public void tearDownTiers() {
        saleTierRepository.deleteAll();
    }

    @Test
    public void testBuyTokensNotExceedingFirstTier() {
        final BigDecimal currency = new BigDecimal(200);
        final BigInteger expectedTokensSold = BigInteger.valueOf(400);
        final Date blockTime = Date.valueOf("2018-01-02");
        ConversionResult result = baseMonitor.calcTokensAndUpdateTiers(currency, blockTime);
        assertEquals(0, result.getOverflow().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.getTokens().compareTo(expectedTokensSold));

        Optional<SaleTier> oActiveTier = saleTierRepository.findByIsActiveTrue();
        if (oActiveTier.isPresent()) {
            assertTier(oActiveTier.get(), T1_NO, T1_START_DATE, T1_END_DATE, expectedTokensSold, true);
        } else {
            fail("Should have found active tier, but didn't.");
        }

        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T2_NO);
        if (oTier.isPresent()) {
            assertTier(oTier.get(), T2_NO, T2_START_DATE, T2_END_DATE, BigInteger.ZERO, false);
        } else {
            fail(String.format("Should have found tier %d, but didn't.", T2_NO));
        }

        oTier = saleTierRepository.findByTierNo(T3_NO);
        if (oTier.isPresent()) {
            assertTier(oTier.get(), T3_NO, T3_START_DATE, T3_END_DATE, BigInteger.ZERO, false);
        } else {
            fail(String.format("Should have found tier %d, but didn't.", T3_NO));
        }
    }

    @Test
    public void testBuyTokensFillingUpFirstTier() {
        final BigDecimal currency = BigDecimal.valueOf(500L);
        final Date blockTime = Date.valueOf("2018-01-02");
        ConversionResult result = baseMonitor.calcTokensAndUpdateTiers(currency, blockTime);
        assertEquals(0, result.getOverflow().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.getTokens().compareTo(T1_MAX));

        Optional<SaleTier> oActiveTier = saleTierRepository.findByIsActiveTrue();
        if (oActiveTier.isPresent()) {
            assertTier(oActiveTier.get(), T2_NO, blockTime, T2_END_DATE, BigInteger.ZERO, true);
        } else {
            fail("Should have found active tier, but didn't.");
        }

        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T1_NO);
        if (oTier.isPresent()) {
            assertTier(oTier.get(), T1_NO, T1_START_DATE, blockTime, T1_MAX, false);
        } else {
            fail(String.format("Should have found tier %d, but didn't.", T1_NO));
        }

        oTier = saleTierRepository.findByTierNo(T3_NO);
        if (oTier.isPresent()) {
            assertTier(oTier.get(), T3_NO, T3_START_DATE, T3_END_DATE, BigInteger.ZERO, false);
        } else {
            fail(String.format("Should have found tier %d, but didn't.", T3_NO));
        }
    }

    @Test
    public void testBuyTokensReachingIntoSecondTier() {
        final BigDecimal currency = BigDecimal.valueOf(500L + 160L);
        final BigInteger tokensFromT2 = BigInteger.valueOf(200L);
        final Date blockTime = Date.valueOf("2018-01-02");
        ConversionResult result = baseMonitor.calcTokensAndUpdateTiers(currency, blockTime);
        assertEquals(0, result.getOverflow().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.getTokens().compareTo(T1_MAX.add(tokensFromT2)));

        Optional<SaleTier> oActiveTier = saleTierRepository.findByIsActiveTrue();
        if (oActiveTier.isPresent()) {
            assertTier(oActiveTier.get(), T2_NO, blockTime, T2_END_DATE, tokensFromT2, true);
        } else {
            fail("Should have found active tier, but didn't.");
        }

        Optional<SaleTier> oTier = saleTierRepository.findByTierNo(T1_NO);
        if (oTier.isPresent()) {
            assertTier(oTier.get(), T1_NO, T1_START_DATE, blockTime, T1_MAX, false);
        } else {
            fail(String.format("Should have found tier %d, but didn't.", T1_NO));
        }

        oTier = saleTierRepository.findByTierNo(T3_NO);
        if (oTier.isPresent()) {
            assertTier(oTier.get(), T3_NO, T3_START_DATE, T3_END_DATE, BigInteger.ZERO, false);
        } else {
            fail(String.format("Should have found tier %d, but didn't.", T3_NO));
        }
    }

    private void assertTier(SaleTier tier, int tierNo, Date startDate, Date endDate,
                            BigInteger tokensSold, boolean isActive) {

        assertEquals(tierNo, tier.getTierNo());
        assertEquals(startDate, tier.getStartDate());
        assertEquals(endDate, tier.getEndDate());
        assertEquals(0, tokensSold.compareTo(tier.getTokensSold()));
        assertEquals(isActive, tier.isActive());
    }

    private SaleTier createTier(int tierNo, Date startDate, Date endDate, double discount,
                                long tokenMax, boolean active) {

        return new SaleTier(
                tierNo,
                "test tier",
                startDate,
                endDate,
                discount,
                BigInteger.valueOf(tokenMax),
                active);
    }
}
