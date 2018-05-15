package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.KeyPairs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface KeyPairsRepository extends JpaRepository<KeyPairs, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<KeyPairs> findFirstOptionalByAvailableOrderByIdAsc(Boolean used);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    KeyPairs save(KeyPairs entity);

}
