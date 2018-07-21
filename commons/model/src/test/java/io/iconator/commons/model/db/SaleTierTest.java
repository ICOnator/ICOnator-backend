package io.iconator.commons.model.db;

import io.iconator.commons.model.db.SaleTier.StatusType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SaleTierTest {

    @Test
    public void testGetStatusAtDate() {
        // Tier with preset max token amount and not all sold
        SaleTier t = createTier(0, "1970-01-03", "1970-01-06", BigDecimal.ZERO, BigInteger.TEN,
                true, false);

        Date date = Date.valueOf("1970-01-02");
        assertEquals(t.getStatusAtDate(date), StatusType.INCOMING);

        date = Date.valueOf("1970-01-03");
        assertEquals(t.getStatusAtDate(date), StatusType.ACTIVE);

        date = Date.valueOf("1970-01-04");
        assertEquals(t.getStatusAtDate(date), StatusType.ACTIVE);

        date = Date.valueOf("1970-01-06");
        assertEquals(t.getStatusAtDate(date), StatusType.CLOSED);

        date = Date.valueOf("1970-01-07");
        assertEquals(t.getStatusAtDate(date), StatusType.CLOSED);

        t.setTomicsSold(BigInteger.TEN);

        date = Date.valueOf("1970-01-04");
        assertEquals(t.getStatusAtDate(date), StatusType.CLOSED);

        t.setTomicsSold(BigInteger.TEN.add(BigInteger.TEN));

        date = Date.valueOf("1970-01-04");
        assertEquals(t.getStatusAtDate(date), StatusType.CLOSED);


        // Tier without max token amount set (i.e. set to zero)
        t = createTier(0, "1970-01-03", "1970-01-06", BigDecimal.ZERO, BigInteger.ZERO, false, true);
        t.setTomicsSold(BigInteger.TEN);

        date = Date.valueOf("1970-01-02");
        assertEquals(t.getStatusAtDate(date), StatusType.INCOMING);

        date = Date.valueOf("1970-01-03");
        assertEquals(t.getStatusAtDate(date), StatusType.CLOSED);
    }

    private static SaleTier createTier(int tierNo, String startDate, String endDate, BigDecimal discount,
                                BigInteger tomicsMax, boolean hasDynamicDuration,
                                boolean hasDynamicMax) {
        return new SaleTier(
                tierNo,
                "test tier " + tierNo,
                Date.valueOf(startDate),
                Date.valueOf(endDate),
                discount,
                BigInteger.ZERO,
                tomicsMax,
                hasDynamicDuration,
                hasDynamicMax);
    }
}
