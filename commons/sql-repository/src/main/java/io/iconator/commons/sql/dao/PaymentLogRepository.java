package io.iconator.commons.sql.dao;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    boolean existsByTxIdentifierAndCurrency(String txIdentifier, CurrencyType currency);

    Optional<PaymentLog> findOptionalByTxIdentifierAndCurrency(String txIdentifier, CurrencyType currencyType);
}
