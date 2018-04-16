package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.SaleTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EligibleForRefundRepository extends JpaRepository<EligibleForRefund, Long>{

    boolean existsByTxIdentifier(String txIdentifier);

}
