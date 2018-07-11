package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class SaleTierRepositoryTest {

    @Autowired
    private SaleTierRepository tierRepository;

    @Autowired
    private EntityManager entityManager;

    @After
    public void deleteTiers() {
        tierRepository.deleteAll();
    }

    @Test
    public void testSave() {
        SaleTier tier = createTier(1, "2018-01-11", "2018-01-20", new BigDecimal("0.1"), 3000L);
        tierRepository.save(tier);
    }

    @Test
    public void testFindActiveTier() {
        tierRepository.save(createTier(1, "2018-01-01", "2018-01-10", new BigDecimal("0.5"), 1000L));
        tierRepository.save(createTier(2, "2018-01-11", "2018-01-20", new BigDecimal("0.2"), 2000L));
        final Date date = java.sql.Date.valueOf("2018-01-02");
        Optional<SaleTier> oTier = tierRepository.findTierAtDate(date);
        assertTrue(oTier.isPresent());
        assertEquals(oTier.get().getTierNo(), 1);
    }

    @Test
    public void testOverlappingActiveDates() {
        tierRepository.save(createTier(1, "2018-01-01", "2018-01-10", new BigDecimal("0.5"), 1000L));
        tierRepository.save(createTier(2, "2018-01-02", "2018-01-11", new BigDecimal("0.2"), 2000L));
        final Date date = java.sql.Date.valueOf("2018-01-02");
        try {
            tierRepository.findTierAtDate(date);
        } catch (IncorrectResultSizeDataAccessException e) {
            return;
        }
        fail("Two tiers that are active at the same time should lead to error.");
    }

    @Test
    public void testFindAllOrderByBeginDate() {
        tierRepository.save(createTier(1, "2018-01-01", "2018-01-10", new BigDecimal("0.5"), 1000L));
        tierRepository.save(createTier(2, "2018-01-11", "2018-01-20", new BigDecimal("0.2"), 2000L));
        tierRepository.save(createTier(3, "2018-01-21", "2018-01-30", new BigDecimal("0.1"), 3000L));
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
        tierRepository.save(createTier(1, "2018-01-01", "2018-01-10", new BigDecimal("0.5"), 1000L));
        tierRepository.save(createTier(2, "2018-01-11", "2018-01-20", new BigDecimal("0.2"), 2000L));
        tierRepository.save(createTier(3, "2018-01-21", "2018-01-30", new BigDecimal("0.1"), 3000L));
        Optional<SaleTier> oTier = tierRepository.findFirstByOrderByEndDateDesc();
        if (oTier.isPresent()) {
            assertEquals(3, oTier.get().getTierNo());
        } else {
           fail("Retrieved last tier was null but should be");
        }
    }

    @Test
    public void testDiscountNumberFormat() {
        SaleTier t = tierRepository.saveAndFlush(createTier(1, "2018-01-01", "2018-01-10", new BigDecimal("0.5239495"), 1000L));
        entityManager.detach(t);
        t = tierRepository.findByTierNo(1).get();
        assertEquals(0, t.getDiscount().compareTo(new BigDecimal("0.523950")));

        t = tierRepository.saveAndFlush(createTier(2, "2018-01-01", "2018-01-10", new BigDecimal("0.0009495"), 1000L));
        entityManager.detach(t);
        t = tierRepository.findByTierNo(2).get();
        assertEquals(0, t.getDiscount().compareTo(new BigDecimal("0.000950")));
    }

    private static SaleTier createTier(int tierNo, String start, String end, BigDecimal discount, long tokenMax) {

        Date beginDate = java.sql.Date.valueOf(start);
        Date endDate = java.sql.Date.valueOf(end);
        return new SaleTier(
                tierNo,
                "test tier",
                beginDate,
                endDate,
                discount,
                BigInteger.valueOf(tokenMax),
                BigInteger.ZERO,
                true,
                false);
    }
}
