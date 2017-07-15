package io.modum.tokenapp.backend.dao;

import io.modum.tokenapp.backend.model.Investor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@DataJpaTest
public class InvestorRepositoryTest {

    @Autowired
    private InvestorRepository investorRepository;

    @Test
    public void testInsert() {
        Investor i = new Investor().setCreationDate(new Date()).setEmail("test@test.com");
        investorRepository.save(i);
    }


}
