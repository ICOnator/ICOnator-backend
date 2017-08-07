package io.modum.tokenapp.backend.dao;

import io.modum.tokenapp.backend.model.KeyPairs;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface KeyPairsRepository extends CrudRepository<KeyPairs, Long> {

    @Query(value = "select nextval('fresh_key')", nativeQuery = true)
    long getFreshKeyID();

}
