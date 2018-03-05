package io.iconator.commons.sql.dao;

import io.iconator.commons.model.db.ExchangeAggregateRate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeAggregateRateRepository extends CrudRepository<ExchangeAggregateRate, Long> {

    Optional<ExchangeAggregateRate> findFirstOptionalByBlockNrBtcGreaterThanEqualOrderByBlockNrBtcAsc(Long blockNrBtc);

    Optional<ExchangeAggregateRate> findFirstOptionalByBlockNrEthGreaterThanEqualOrderByBlockNrEthAsc(Long blockNrEth);

    Optional<ExchangeAggregateRate> findFirstOptionalByOrderByBlockNrBtcDesc();

    Optional<ExchangeAggregateRate> findFirstOptionalByOrderByBlockNrEthDesc();

    List<ExchangeAggregateRate> findAllByOrderByCreationDate();

}
