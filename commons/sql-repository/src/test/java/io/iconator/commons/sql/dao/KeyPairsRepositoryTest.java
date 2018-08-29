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
    public void testFindOptionalById() {
        Optional<KeyPairs> optionalKeyPairs = keyPairsRepository.findById(1L);
        LOG.info("Id returned: " + optionalKeyPairs.get().getId());
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

    @Test
    public void testFindFirstOptionalByPublicEth() {
        Optional<KeyPairs> optionalKeyPairs = keyPairsRepository.findFirstOptionalByPublicEth("0x0eB5C5de600D088AB0260d068E9765022FD5173b");
        LOG.info("Id returned: " + optionalKeyPairs.get().getId());
        assertTrue(optionalKeyPairs.isPresent());
        assertEquals(1, optionalKeyPairs.get().getId());
        assertEquals("0x0eB5C5de600D088AB0260d068E9765022FD5173b", optionalKeyPairs.get().getPublicEth());
        assertEquals(true, optionalKeyPairs.get().getAvailable());
    }

    @Test
    public void testFindFirstOptionalByPublicBtc() {
        Optional<KeyPairs> optionalKeyPairs = keyPairsRepository.findFirstOptionalByPublicBtc("n4CS3vyACkcvCyVLm2RS5tpvyy6NNvbCBr");
        LOG.info("Id returned: " + optionalKeyPairs.get().getId());
        assertTrue(optionalKeyPairs.isPresent());
        assertEquals(10, optionalKeyPairs.get().getId());
        assertEquals("n4CS3vyACkcvCyVLm2RS5tpvyy6NNvbCBr", optionalKeyPairs.get().getPublicBtc());
        assertEquals(true, optionalKeyPairs.get().getAvailable());
    }

}
