package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.KeyPairs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyPairsRepository extends JpaRepository<KeyPairs, Long> {

    @Query(value = "select nextval('fresh_key')", nativeQuery = true)
    long getFreshKeyID();

}
