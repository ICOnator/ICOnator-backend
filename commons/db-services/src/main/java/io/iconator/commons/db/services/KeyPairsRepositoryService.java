package io.iconator.commons.db.services;

import io.iconator.commons.model.db.KeyPairs;
import io.iconator.commons.sql.dao.KeyPairsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class KeyPairsRepositoryService {

    private static final Logger LOG = LoggerFactory.getLogger(KeyPairsRepositoryService.class);

    private KeyPairsRepository keyPairsRepository;

    @Autowired
    public KeyPairsRepositoryService(KeyPairsRepository keyPairsRepository) {
        this.keyPairsRepository = keyPairsRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<KeyPairs> getFreshKey() {
        try {
            return this.keyPairsRepository
                    .findFirstOptionalByAvailableOrderByIdAsc(new Boolean(true))
                    .map((k) -> k.setAvailable(new Boolean(false)))
                    .map((k) -> this.keyPairsRepository.save(k));
        } catch (Exception e) {
            LOG.error("Error obtaining a fresh key.", e);
            return Optional.empty();
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean addKeyPairsIfNotPresent(KeyPairs keyPairs) {
        try {
            this.keyPairsRepository.save(keyPairs);
            return true;
        } catch (Exception e) {
            LOG.warn("Not loading key [{}, {}]. Error: {}", keyPairs.getPublicEth(), keyPairs.getPublicBtc(), e);
            return false;
        }
    }

    public Optional<KeyPairs> findById(long id) {
        return this.keyPairsRepository.findById(id);
    }

}
