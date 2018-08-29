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

    Optional<PaymentLog> findOptionalByTransactionId(String transactionId);

    boolean existsByInvestorId(long investorId);
}