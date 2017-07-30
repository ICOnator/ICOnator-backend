package io.modum.tokenapp.backend.dao;

import io.modum.tokenapp.backend.model.ExchangeRate;
import org.springframework.data.repository.CrudRepository;


public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, Long> {
    ExchangeRate findFirstByBlockNrBtcGreaterThanEqualOrderByBlockNrBtcAsc(Long blockNrBtc);
    ExchangeRate findFirstByBlockNrEthGreaterThanEqualOrderByBlockNrEthAsc(Long blockNrEth);
}
