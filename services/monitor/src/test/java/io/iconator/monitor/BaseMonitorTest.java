package io.iconator.monitor;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.BaseMonitor.ConversionResult;
import io.iconator.monitor.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestConfig.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DataJpaTest
@TestPropertySource({"classpath:monitor.application-test.properties"})
public class BaseMonitorTest {

    private final static double TIER_ONE_DISCOUNT = 0.5;
    private final static double TIER_TWO_DISCOUNT = 0.2;
    private final static double TIER_THREE_DISCOUNT = 0.1;

    private final static int TIER_ONE_NO = 1;
    private final static int TIER_TWO_NO = 2;
    private final static int TIER_THREE_NO = 3;

    @Autowired
    private BaseMonitor baseMonitor;

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Before
    public void setUpTiersInDb() {
        saleTierRepository.save(createTier(1, "2018-01-01", "2018-01-10", TIER_ONE_DISCOUNT, 1000L, true));
        saleTierRepository.save(createTier(2, "2018-01-11", "2018-01-20", TIER_TWO_DISCOUNT, 2000L, false));
        saleTierRepository.save(createTier(3, "2018-01-21", "2018-01-30", TIER_THREE_DISCOUNT, 3000L, false));
    }

    @Test
    public void testBuyTokensNotExceedingFirstTier() {
        final BigDecimal currency = new BigDecimal(200);
        final Date dateInTierOne = Date.valueOf("2018-01-02");
        ConversionResult result = baseMonitor.calcTokensAndUpdateTiers(currency, dateInTierOne);
        assertEquals(0, result.getOverflow().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.getTokens().compareTo(BigInteger.valueOf(400)));

        Optional<SaleTier> oActiveTier = saleTierRepository.findByIsActiveTrue();
        if (oActiveTier.isPresent()) {
            SaleTier activeTier = oActiveTier.get();
            assertEquals(1, activeTier.getTierNo());
        } else {
            fail("Should have found active tier, but didn't.");
        }
    }

    private SaleTier createTier(int tierNo, String start, String end, double discount, long tokenMax, boolean active) {

        Date beginDate = java.sql.Date.valueOf(start);
        Date endDate = java.sql.Date.valueOf(end);
        return new SaleTier(
                tierNo,
                "test tier",
                beginDate,
                endDate,
                discount,
                BigInteger.valueOf(tokenMax),
                active);
    }
}
