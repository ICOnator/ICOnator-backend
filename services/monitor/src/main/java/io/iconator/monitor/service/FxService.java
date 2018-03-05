package io.iconator.monitor.service;

import io.iconator.commons.sql.dao.ExchangeAggregateRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class FxService {

    @Autowired
    private ExchangeAggregateRateRepository aggregateRateRepository;

    public BigDecimal getUSDperETH(Long blockHeight) {
        // TODO: 05.03.18 Guil:
        // To be done.
        return null;
    }

    public BigDecimal getUSDPerBTC(Long timestamp) {
        // TODO: 05.03.18 Guil:
        // To be done.
        return null;
    }

    public BigDecimal weiToUSD(BigInteger weiAmount, Long blockHeight) {
        // TODO: 05.03.18 Guil:
        // To be done.
        return null;
    }

}
