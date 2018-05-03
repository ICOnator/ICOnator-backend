package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.KeyPairs;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class KeyPairsRepositoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(KeyPairsRepositoryTest.class);

    @Autowired
    private KeyPairsRepository keyPairsRepository;

    @Test
    public void testGetFresh() {
        long freshKeyID = keyPairsRepository.getFreshKeyID();
        Optional<KeyPairs> kp = keyPairsRepository.findById(freshKeyID);
        assertTrue(kp.isPresent());
        assertTrue(kp.get().getPublicBtc() != null);
        assertTrue(kp.get().getPublicEth() != null);
    }

    @Test
    public void testFindOptionalById() {
        Optional<KeyPairs> optionalKeyPairs = keyPairsRepository.findById(1L);
        System.out.println("Id returned: " + optionalKeyPairs.get().getId());
        assertTrue(optionalKeyPairs.isPresent());
        assertEquals(1, optionalKeyPairs.get().getId());
        assertEquals(true, optionalKeyPairs.get().getAvailable());
    }

    @Test
    public void testFindFirstOptionalByAvailableOrderByIdDesc() {
        Optional<KeyPairs> optionalFindFirstAvailable = keyPairsRepository.findFirstOptionalByAvailableOrderByIdAsc(new Boolean(true));
        assertTrue(optionalFindFirstAvailable.isPresent());
        assertEquals(1, optionalFindFirstAvailable.get().getId());
        assertEquals(true, optionalFindFirstAvailable.get().getAvailable());
    }

}
