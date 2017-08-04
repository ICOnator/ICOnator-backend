package io.modum.tokenapp.backend.dao;

import io.modum.tokenapp.backend.model.Investor;
import javax.persistence.PersistenceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
public class InvestorRepositoryTest {

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testInsert() {
        Investor i = new Investor().setCreationDate(new Date()).setEmail("test@test.com");
        investorRepository.save(i);
    }

    @Test
    public void testSearchByEmailConfirmationToken() {
        String randomUUID = UUID.randomUUID().toString();
        Investor i = new Investor().setCreationDate(new Date()).setEmail("test@test.com").setEmailConfirmationToken(randomUUID);
        investorRepository.save(i);

        Optional<Investor> oInvestor = investorRepository.findOptionalByEmailConfirmationToken(randomUUID);
        assertTrue(oInvestor.isPresent() && oInvestor.get().getEmailConfirmationToken().equals(randomUUID));
    }

    @Test(expected = PersistenceException.class)
    public void testEmailConfirmationTokenUniqueConstraint() {
        String randomUUID = UUID.randomUUID().toString();
        Investor i1 = new Investor().setCreationDate(new Date())
                                    .setEmail("test1@test1.com")
                                    .setEmailConfirmationToken(randomUUID);
        investorRepository.save(i1);
        Investor i2 = new Investor().setCreationDate(new Date())
                                    .setEmail("test2@test2.com")
                                    .setEmailConfirmationToken(randomUUID);
        investorRepository.save(i2);
        entityManager.flush();
    }

    @Test(expected = PersistenceException.class)
    public void testEmailUniqueConstraint() {
        Investor i1 = new Investor().setCreationDate(new Date())
                                    .setEmail("test@test.com")
                                    .setEmailConfirmationToken(UUID.randomUUID().toString());
        investorRepository.save(i1);
        Investor i2 = new Investor().setCreationDate(new Date())
                                    .setEmail("test@test.com")
                                    .setEmailConfirmationToken(UUID.randomUUID().toString());
        investorRepository.save(i2);
        entityManager.flush();
    }



}
