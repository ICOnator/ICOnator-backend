package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.PaymentLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentLogRepository extends CrudRepository<PaymentLog, Long> {

    Optional<PaymentLog> findOptionalByEmail(String email);

}
