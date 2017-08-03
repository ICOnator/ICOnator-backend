package io.modum.tokenapp.minting.dao;

import io.modum.tokenapp.minting.model.Payin;
import org.springframework.data.repository.CrudRepository;

public interface PayinRepository extends CrudRepository<Payin, Long> {
    Iterable<Payin> findAllByOrderByTimeAsc();
}
