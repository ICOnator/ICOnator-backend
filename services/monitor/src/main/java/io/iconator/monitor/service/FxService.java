package io.iconator.monitor.service;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.ExchangeAggregateRate;
import io.iconator.commons.sql.dao.ExchangeAggregateRateRepository;
import io.iconator.monitor.service.exceptions.USDBTCFxException;
import io.iconator.monitor.service.exceptions.USDETHFxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Service
public class FxService {

    @Autowired
    private ExchangeAggregateRateRepository aggregateRateRepository;

    public BigDecimal getUSDperETH(Long blockHeight) throws USDETHFxException {
        Optional<ExchangeAggregateRate> exchangeAggregateRate =
                aggregateRateRepository.findFirstOptionalByBlockNrEthGreaterThanEqualOrderByBlockNrEthAsc(blockHeight);

        return exchangeAggregateRate.flatMap((aggregateRate) -> aggregateRate.getExchangeAggregateCurrencyRates(CurrencyType.ETH))
                .map((aggCurrencyRate) -> aggCurrencyRate.getAggregateExchangeRate())
                .orElseThrow(() -> new USDETHFxException("No FX aggregation found for USD-ETH."));
    }

    public BigDecimal getUSDPerBTC(Date timestamp) throws USDBTCFxException {
        Optional<ExchangeAggregateRate> exchangeAggregateRate =
                aggregateRateRepository.findFirstOptionalByCreationDateGreaterThanEqualOrderByCreationDateAsc(timestamp);

        return exchangeAggregateRate.flatMap((aggregateRate) -> aggregateRate.getExchangeAggregateCurrencyRates(CurrencyType.BTC))
                .map((aggCurrencyRate) -> aggCurrencyRate.getAggregateExchangeRate())
                .orElseThrow(() -> new USDBTCFxException("No FX aggregation found for USD-BTC."));
    }

}
