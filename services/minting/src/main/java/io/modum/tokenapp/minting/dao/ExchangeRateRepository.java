package io.modum.tokenapp.minting.dao;

import io.modum.tokenapp.minting.model.ExchangeRate;
import org.springframework.data.repository.CrudRepository;


public interface ExchangeRateRepository extends CrudRepository<ExchangeRate, Long> {
    ExchangeRate findFirstByBlockNrBtcGreaterThanEqualOrderByBlockNrBtcAsc(Long blockNrBtc);
    ExchangeRate findFirstByBlockNrEthGreaterThanEqualOrderByBlockNrEthAsc(Long blockNrEth);
}
