package io.iconator.kyc.service;

import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.kyc.config.BaseKycTestConfig;
import io.iconator.kyc.service.exception.InvestorNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseKycTestConfig.class})
@DataJpaTest
@TestPropertySource({"classpath:kyc.application.properties", "classpath:application-test.properties"})
public class InvestorServiceTest {

    @Autowired
    private InvestorService investorService;

    @Autowired
    private InvestorRepository investorRepository;

    private Investor investor;

    @Before
    public void setUp() {
        investor = new Investor(new Date(), "test@test.com", "1234");
        investorRepository.save(investor);
    }

    @Test
    public void testGetInvestorByInvestorId() {
        try {
            Investor investorFromDb = investorService.getInvestorByInvestorId(investor.getId());
            assertEquals(investor, investorFromDb);
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetInvestorByEmail() {
        try {
            Investor investorFromDb = investorService.getInvestorByEmail("test@test.com");
            assertEquals(investor, investorFromDb);
        } catch(InvestorNotFoundException e) {
            fail(e.getMessage());
        }
    }
}
