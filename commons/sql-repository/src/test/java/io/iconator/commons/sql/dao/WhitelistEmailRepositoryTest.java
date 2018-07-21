package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.WhitelistEmail;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class WhitelistEmailRepositoryTest {

    @Autowired
    private WhitelistEmailRepository whitelistEmailRepository;

    @Test
    public void testSaveAndFind() {
        Date now = Date.from(Instant.now());
        WhitelistEmail wTest1 = new WhitelistEmail("test@test.com", now);
        WhitelistEmail wTestSaved1 = whitelistEmailRepository.saveAndFlush(wTest1);

        WhitelistEmail wTest2 = new WhitelistEmail("test2@test.com", now);
        WhitelistEmail wTestSaved2 = whitelistEmailRepository.saveAndFlush(wTest2);

        assertNotNull(wTestSaved1.getEmail());
        assertNotNull(wTestSaved1.getSubscriptionDate());
        assertEquals(wTestSaved1.getSubscriptionDate(), now);

        assertNotNull(wTestSaved2.getEmail());
        assertNotNull(wTestSaved2.getSubscriptionDate());
        assertEquals(wTestSaved2.getSubscriptionDate(), now);

        Optional<WhitelistEmail> oWTestFound1 = whitelistEmailRepository.findByEmail("test@test.com");
        assertTrue(oWTestFound1.isPresent());

        Optional<WhitelistEmail> oWTestFound2 = whitelistEmailRepository.findByEmail("test2@test.com");
        assertTrue(oWTestFound2.isPresent());

        List<WhitelistEmail> allEmails = whitelistEmailRepository.findAll();
        assertEquals(2, allEmails.size());

    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testSaveSameEmail_UniqueIndexViolation() {
        Date now = Date.from(Instant.now());
        WhitelistEmail wTest1 = new WhitelistEmail("test@test.com", now);
        whitelistEmailRepository.saveAndFlush(wTest1);

        WhitelistEmail wTest2 = new WhitelistEmail("test@test.com", now);
        whitelistEmailRepository.saveAndFlush(wTest2);
    }

}
