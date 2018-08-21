package io.iconator.commons.sql.dao;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    boolean existsByTxIdentifierAndCurrency(String txIdentifier, CurrencyType currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PaymentLog> findOptionalByTxIdentifierAndCurrency(String txIdentifier, CurrencyType currencyType);

    @Lock(LockModeType.READ)
    Optional<PaymentLog> readOptionalByTxIdentifierAndCurrency(String txIdentifier, CurrencyType currencyType);
}
