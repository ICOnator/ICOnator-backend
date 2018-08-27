package io.iconator.commons.sql.dao;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.model.db.PaymentLog.TransactionStatus;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class PaymentLogRepositoryTest {

    private static final CurrencyType CURRENCY_TYPE = CurrencyType.BTC;

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private InvestorRepository investorRepository;

    @Test
    public void testSave() {
        Investor i = createInvestor(1);
        PaymentLog p = createPaymentLog(i, "1");
        paymentLogRepository.save(p);
    }

    @Test
    public void testSaveAndExistsAndFind() {
        Investor i = createInvestor(1);
        PaymentLog p = paymentLogRepository.save(createPaymentLog(i, "1"));
//        assertTrue(paymentLogRepository.existsByTxIdentifierAndCurrency("1", CURRENCY_TYPE));
        Optional<PaymentLog> oPaymentLog = paymentLogRepository.findOptionalByTransactionId("1");
        assertTrue(oPaymentLog.isPresent());
        assertTrue(oPaymentLog.filter((paymentLog) ->
                paymentLog.getInvestor().getEmail().equals("emailAddress1")
        ).isPresent());
        assertTrue(oPaymentLog.filter((paymentLog) -> paymentLog.equals(p)).isPresent());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testSaveTwoPaymentLogWithSameTransactionIdentifier() {
        Investor i1 = createInvestor(1);
        PaymentLog p1 = createPaymentLog(i1, "1");
        paymentLogRepository.saveAndFlush(p1);
        Investor i2 = createInvestor(2);
        PaymentLog p2 = createPaymentLog(i2, "1");
        paymentLogRepository.saveAndFlush(p2);

    }

    private Investor createInvestor(int i) {
        return investorRepository.save(
                new Investor(new Date(),  "emailAddress" + i, "token" + i, "walletAddress",
                        "payInEtherPublicKey" + i, "payInBitcoinPublicKey" + i,
                        "refundEtherAddress", "refundBitcoinAddress", "ipAddress" ));
    }

    private PaymentLog createPaymentLog(Investor investor, String txIdentifier) {

        return new PaymentLog(
                txIdentifier,
                new Date(),
                CURRENCY_TYPE,
                new Date(),
                new BigInteger("1"),
                new BigDecimal(2),
                new BigDecimal(3),
                investor,
                BigInteger.valueOf(100L),
                TransactionStatus.PENDING);
    }

}
