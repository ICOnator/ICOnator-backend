package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.KeyPairs;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyPairsRepository extends CrudRepository<KeyPairs, Long> {

    @Query(value = "select nextval('fresh_key')", nativeQuery = true)
    long getFreshKeyID();

}
