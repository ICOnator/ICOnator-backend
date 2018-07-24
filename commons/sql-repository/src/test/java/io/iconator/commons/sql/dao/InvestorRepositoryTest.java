package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class InvestorRepositoryTest {

    @Autowired
    private InvestorRepository investorRepository;

    @Test
    public void testSave() {
        Investor i = new Investor(new Date(),
                "test@test.com",
                "somelongtoken"
        );
        investorRepository.save(i);
    }

    @Test
    public void testFindByEmailConfirmationToken() {
        String randomUUID = UUID.randomUUID().toString();
        Investor i = new Investor(new Date(), "test@test.com", randomUUID);
        investorRepository.save(i);

        Optional<Investor> oInvestor = investorRepository.findOptionalByEmailConfirmationToken(randomUUID);
        assertTrue(oInvestor.isPresent());
        assertTrue(oInvestor.get().getEmailConfirmationToken().equals(randomUUID));
    }

    @Test
    public void testFindByPayInEtherPublicKey() {
        Investor i = createInvestor();
        investorRepository.save(i);

        Optional<Investor> oInvestor = investorRepository.findOptionalByPayInEtherAddressIgnoreCase("payInEther1");
        assertTrue(oInvestor.isPresent());
        assertTrue(oInvestor.filter((investor) -> investor.getPayInEtherAddress().equals("payInEther1")).isPresent());
    }

    @Test
    public void testFindByPayInBitcoinPublicKey() {
        Investor i = createInvestor();
        investorRepository.save(i);

        Optional<Investor> oInvestor = investorRepository.findOptionalByPayInBitcoinAddress("payInBitcoin1");
        assertTrue(oInvestor.isPresent());
        assertTrue(oInvestor.filter((investor) -> investor.getPayInBitcoinAddress().equals("payInBitcoin1")).isPresent());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testEmailConfirmationTokenUniqueConstraint() {
        String randomUUID = UUID.randomUUID().toString();
        Investor i1 = new Investor(new Date(), "test1@test1.com", randomUUID);
        investorRepository.saveAndFlush(i1);
        Investor i2 = new Investor(new Date(), "test2@test2.com", randomUUID);
        investorRepository.saveAndFlush(i2);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testEmailUniqueConstraint() {
        Investor i1 = new Investor(new Date(), "test@test.com", UUID.randomUUID().toString());
        investorRepository.saveAndFlush(i1);
        Investor i2 = new Investor(new Date(), "test@test.com", UUID.randomUUID().toString());
        investorRepository.saveAndFlush(i2);
    }

    private Investor createInvestor() {
        String randomUUID = UUID.randomUUID().toString();
        return new Investor(new Date(),
                "test@test.com",
                randomUUID,
                "wallet-address-12345",
                "payInEther1",
                "payInBitcoin1",
                "refundEther1",
                "refundEther2",
                "127.0.0.1");
    }

}
