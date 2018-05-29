package io.iconator.monitor;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.monitor.config.TestConfig;
import io.iconator.monitor.service.TokenConversionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Date;

import static junit.framework.TestCase.fail;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class, TokenConversionService.class})
@DataJpaTest
@TestPropertySource({"classpath:monitor.application.properties", "classpath:application-test.properties"})
public class BaseMonitorTest {

    @Autowired
    private BaseMonitor baseMonitor;

    @Test
    public void testSavePaymentLog() {
        PaymentLog paymentLog1 = new PaymentLog(
                "1", // TxIdentifier
                new Date(), // payment log timestamp
                new Date(), // Block Timestamp
                CurrencyType.ETH,
                new BigDecimal(1), // cryptocurrency amount
                new BigDecimal(1), // USD to cryptocurrency rate
                new BigDecimal(1), // USD amount
                1, // investor id
                BigDecimal.ZERO); // token amount

        baseMonitor.savePaymentLog(paymentLog1);

        PaymentLog paymentLog2 = new PaymentLog(
                "1", // TxIdentifier
                new Date(), // payment log timestamp
                new Date(), // Block Timestamp
                CurrencyType.ETH,
                new BigDecimal(1), // cryptocurrency amount
                new BigDecimal(1), // USD to cryptocurrency rate
                new BigDecimal(1), // USD amount
                1, // investor id
                BigDecimal.ZERO); // token amount

        try {
            baseMonitor.savePaymentLog(paymentLog2);
        } catch (Exception e) {
            return;
        }
        fail("Saving a second payment log with tx identifier that already exists in table should " +
                "have failed.");
    }

    @Test
    public void testSavePaymentLog2() {
        PaymentLog paymentLog1 = new PaymentLog(
                null, // TxIdentifier
                new Date(), // payment log timestamp
                new Date(), // Block Timestamp
                CurrencyType.ETH,
                new BigDecimal(1), // cryptocurrency amount
                new BigDecimal(1), // USD to cryptocurrency rate
                new BigDecimal(1), // USD amount
                1, // investor id
                BigDecimal.ZERO); // token amount
        try {
            baseMonitor.savePaymentLog(paymentLog1);
        } catch (Exception e) {
            return;
        }
        fail("Saving a payment log with null tx identifier should fail.");
    }
}
