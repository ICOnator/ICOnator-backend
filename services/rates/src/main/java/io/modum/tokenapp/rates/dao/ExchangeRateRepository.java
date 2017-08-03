package io.modum.tokenapp.rates.dao;

import io.modum.tokenapp.rates.model.ExchangeRate;
import org.springframework.data.repository.CrudRepository;


public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, Long> {
    ExchangeRate findFirstByBlockNrBtcGreaterThanEqualOrderByBlockNrBtcAsc(Long blockNrBtc);
    ExchangeRate findFirstByBlockNrEthGreaterThanEqualOrderByBlockNrEthAsc(Long blockNrEth);

    ExchangeRate findFirstByOrderByBlockNrBtcDesc();
    ExchangeRate findFirstByOrderByBlockNrEthDesc();
}
