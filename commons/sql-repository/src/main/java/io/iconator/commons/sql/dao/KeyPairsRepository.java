package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.KeyPairs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface KeyPairsRepository extends JpaRepository<KeyPairs, Long> {

    @Transactional
    @Modifying
    @Query(value = "CREATE SEQUENCE fresh_key START WITH 1 INCREMENT BY 1", nativeQuery = true)
    void createFreshKeySequence();

    @Query(value = "SELECT nextval('fresh_key')", nativeQuery = true)
    long getFreshKeyID();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<KeyPairs> findFirstOptionalByAvailableOrderByIdDesc(Boolean used);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    KeyPairs save(KeyPairs entity);

}
