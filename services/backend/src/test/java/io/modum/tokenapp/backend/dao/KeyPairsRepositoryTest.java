package io.modum.tokenapp.backend.dao;

import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.backend.model.KeyPairs;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.platform: h2",
        "spring.datasource.url: jdbc:h2:mem:testdb;mv_store=false;DB_CLOSE_ON_EXIT=FALSE;"
})
public class KeyPairsRepositoryTest {

    @Autowired
    private KeyPairsRepository keyPairsRepository;

    @Test
    @Ignore
    public void testGetFresh() {
        long freshKeyID = keyPairsRepository.getFreshKeyID();
        KeyPairs kp = keyPairsRepository.findOne(freshKeyID);
        assertTrue(kp.getPublicBtc() != null);
        assertTrue(kp.getPublicEth() != null);
    }

}
