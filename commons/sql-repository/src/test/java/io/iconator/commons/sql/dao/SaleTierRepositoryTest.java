package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class SaleTierRepositoryTest {

    @Autowired
    private SaleTierRepository tierRepository;

    @Test
    public void testSave() {
        SaleTier tier = createActiveTier();
        tierRepository.save(tier);
    }

    @Test
    public void testFindAllOrderByBeginDate() {
        storeThreeSuccessiveTiers();
        List<SaleTier> tiers = tierRepository.findAllByOrderByBeginDateAsc();
        ListIterator<SaleTier> it = tiers.listIterator();
        SaleTier t1 = it.next();
        while (it.hasNext()) {
            SaleTier t2 = it.next();
            assertTrue(t1.getBeginDate().before(t2.getBeginDate()));
            t1 = t2;
        }
    }

    private void storeThreeSuccessiveTiers() {
        tierRepository.save(createTier(1, "2018-01-01", "2018-01-10", 0.5, 1000L, true));
        tierRepository.save(createTier(2, "2018-01-11", "2018-01-20", 0.2, 2000L, false));
        tierRepository.save(createTier(3, "2018-01-21", "2018-01-30", 0.1, 3000L, false));
    }

    private SaleTier createTier(int tierNo, String begin, String end, double discount, long tokenMax,
                                boolean active) {

        Date beginDate = java.sql.Date.valueOf(begin);
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
