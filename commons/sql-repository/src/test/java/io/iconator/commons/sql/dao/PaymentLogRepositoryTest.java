package io.iconator.commons.sql.dao;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
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
import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class PaymentLogRepositoryTest {

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private InvestorRepository investorRepository;

    @Test
    public void testSave() {
        Investor i = createInvestor(1);
        PaymentLog p = createPaymentLog(i.getId(), "1");
        paymentLogRepository.save(p);
    }

    @Test
    public void testSaveAndFind() {
        Investor i = createInvestor(1);
        PaymentLog p = createPaymentLog(i.getId(), "1");
        paymentLogRepository.save(p);
        Optional<PaymentLog> oPaymentLog = paymentLogRepository.findOptionalByTxIdentifier("1");
        assertTrue(oPaymentLog.isPresent());
        assertTrue(oPaymentLog.filter((paymentLog) ->
                investorRepository.findById(paymentLog.getInvestorId()).get().getEmail().equals("emailAddress1")
        ).isPresent());
        assertTrue(oPaymentLog.filter((paymentLog) -> paymentLog.equals(p)).isPresent());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testSaveTwoPaymentLogWithSameTransactionIdentifier() {
        Investor i1 = createInvestor(1);
        PaymentLog p1 = createPaymentLog(i1.getId(), "1");
        paymentLogRepository.saveAndFlush(p1);
        Investor i2 = createInvestor(2);
        PaymentLog p2 = createPaymentLog(i2.getId(), "1");
        paymentLogRepository.saveAndFlush(p2);

    }

    private Investor createInvestor(int i) {
        return investorRepository.save(
                new Investor(new Date(),  "emailAddress" + i, "token" + i, "walletAddress",
                        "payInEtherPublicKey" + i, "payInBitcoinPublicKey" + i,
                        "refundEtherAddress", "refundBitcoinAddress", "ipAddress" ));
    }

    private PaymentLog createPaymentLog(long investorId, String txIdentifier) {

        return new PaymentLog(
                txIdentifier,
                new Date(),
                new Date(),
                CurrencyType.BTC,
                new BigInteger("1"),
                new BigDecimal(2),
                new BigDecimal(3),
                investorId,
                BigInteger.valueOf(100L));
    }

}
