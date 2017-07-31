package io.modum.tokenapp.minting.dao;

import io.modum.tokenapp.minting.MintingApplication;
import io.modum.tokenapp.minting.model.Investor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MintingApplication.class)
@Transactional
public class InvestorRepositoryTest {

    @Autowired
    private InvestorRepository investorRepository;

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


}
