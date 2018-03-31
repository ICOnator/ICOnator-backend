package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class SaleTierRepositoryTest {

    @Autowired
    private SaleTierRepository tierRepository;

    @After
    public void setUp() {
        tierRepository.deleteAll();
    }

    @Test
    public void testSave() {
        SaleTier tier = createActiveTier();
        tierRepository.save(tier);
    }

    @Test
    public void testFindAllOrderByBeginDate() {
        tierRepository.save(createTier(1, "2018-01-01", "2018-01-10", 0.5, 1000L, true));
        tierRepository.save(createTier(2, "2018-01-11", "2018-01-20", 0.2, 2000L, false));
        tierRepository.save(createTier(3, "2018-01-21", "2018-01-30", 0.1, 3000L, false));
        List<SaleTier> tiers = tierRepository.findAllByOrderByStartDateAsc();
        ListIterator<SaleTier> it = tiers.listIterator();
        SaleTier t1 = it.next();
        while (it.hasNext()) {
            SaleTier t2 = it.next();
            assertTrue(t1.getStartDate().before(t2.getStartDate()));
            t1 = t2;
        }
    }

    @Test
    public void testFindlastTier() {
        tierRepository.save(createTier(1, "2018-01-01", "2018-01-10", 0.5, 1000L, true));
        tierRepository.save(createTier(2, "2018-01-11", "2018-01-20", 0.2, 2000L, false));
        tierRepository.save(createTier(3, "2018-01-21", "2018-01-30", 0.1, 3000L, false));
        Optional<SaleTier> oTier = tierRepository.findFirstByOrderByEndDateDesc();
        if (oTier.isPresent()) {
            assertTrue(oTier.get().getTierNo() == 3);
        } else {
           fail("Retrieved last tier was null but should be");
        }
    }

    private SaleTier createTier(int tierNo, String start, String end, double discount, long tokenMax,
                                boolean active) {

        Date beginDate = java.sql.Date.valueOf(start);
        Date endDate = java.sql.Date.valueOf(end);
        return new SaleTier(
                tierNo,
                "test tier",
                beginDate,
                endDate,
                discount,
                BigInteger.valueOf(tokenMax),
                true);
    }

    private SaleTier createActiveTier() {
        return createTier(1, "2018-01-11", "2018-01-20", 0.1, 3000L, true);
    }
}
