package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.ExchangeAggregateRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeAggregateRateRepository extends JpaRepository<ExchangeAggregateRate, Long> {

    Optional<ExchangeAggregateRate> findFirstOptionalByBlockNrBtcGreaterThanEqualOrderByBlockNrBtcAsc(Long blockNrBtc);

    Optional<ExchangeAggregateRate> findFirstOptionalByBlockNrEthGreaterThanEqualOrderByBlockNrEthAsc(Long blockNrEth);

    Optional<ExchangeAggregateRate> findFirstOptionalByOrderByBlockNrBtcDesc();

    Optional<ExchangeAggregateRate> findFirstOptionalByOrderByBlockNrEthDesc();

    Optional<ExchangeAggregateRate> findFirstOptionalByCreationDateGreaterThanEqualOrderByCreationDateAsc(Date creationDate);

    Optional<ExchangeAggregateRate> findFirstOptionalByCreationDateGreaterThanEqualOrderByCreationDateDesc(Date creationDate);

    List<ExchangeAggregateRate> findAllByOrderByCreationDate();

}
