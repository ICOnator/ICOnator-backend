package io.iconator.commons.sql.dao;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class PaymentLogRepositoryTest {

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Test
    public void testSave() {
        PaymentLog p = createPaymentLog();
        paymentLogRepository.save(p);
    }

    @Test
    public void testSaveAndFind() {
        PaymentLog p = createPaymentLog();
        paymentLogRepository.save(p);
        Optional<PaymentLog> oPaymentLog = paymentLogRepository.findOptionalByEmail("test@test.com");
        assertTrue(oPaymentLog.isPresent());
        assertTrue(oPaymentLog.filter((paymentLog) -> paymentLog.getEmail().equals("test@test.com")).isPresent());
        assertTrue(oPaymentLog.filter((paymentLog) -> paymentLog.equals(p)).isPresent());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testSaveTwoPaymentLogWithSameTransactionIdentifier() {
        PaymentLog p1 = createPaymentLog();
        paymentLogRepository.saveAndFlush(p1);
        PaymentLog p2 = createPaymentLog();
        paymentLogRepository.saveAndFlush(p2);
    }

    private PaymentLog createPaymentLog() {
        return new PaymentLog("identifier",
                new Date(),
                new Date(),
                CurrencyType.BTC,
                new BigDecimal(1),
                new BigDecimal(2),
                new BigDecimal(3),
                "test@test.com",
                new BigDecimal(100));
    }

}
