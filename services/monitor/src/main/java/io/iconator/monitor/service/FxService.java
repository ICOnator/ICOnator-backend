package io.iconator.monitor.service;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.ExchangeAggregateRate;
import io.iconator.commons.sql.dao.ExchangeAggregateRateRepository;
import io.iconator.monitor.service.exceptions.USDBTCFxException;
import io.iconator.monitor.service.exceptions.USDETHFxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class FxService {

    public static final int BLOCK_DIFF_ETH = 4 * 60 * 2; //2h deviation allowed
    public static final int TIME_DIFF_BTC = 6 * 2;     //2h deviation allowed

    @Autowired
    private ExchangeAggregateRateRepository aggregateRateRepository;

    public BigDecimal getUSDperETH(Long blockHeight) throws USDETHFxException {
        Optional<ExchangeAggregateRate> exchangeAggregateRate =
                aggregateRateRepository.findFirstOptionalByBlockNrEthLessThanEqualOrderByBlockNrEthDesc(blockHeight);

        if(exchangeAggregateRate.isPresent()) {
            if(exchangeAggregateRate.get().getBlockNrEth() + BLOCK_DIFF_ETH < blockHeight) {
                throw new USDETHFxException("No FX aggregation found for USD-ETH.");
            }
        }
        return exchangeAggregateRate.flatMap((aggregateRate) -> aggregateRate.getExchangeAggregateCurrencyRates(CurrencyType.ETH))
                .map((aggCurrencyRate) -> aggCurrencyRate.getAggregateExchangeRate())
                .orElseThrow(() -> new USDETHFxException("No FX aggregation found for USD-ETH."));
    }

    public BigDecimal getUSDPerBTC(Long blockHeight) throws USDBTCFxException {
        Optional<ExchangeAggregateRate> exchangeAggregateRate =
                aggregateRateRepository.findFirstOptionalByBlockNrBtcLessThanEqualOrderByBlockNrBtcDesc(blockHeight);

        if(exchangeAggregateRate.isPresent()) {
            if(exchangeAggregateRate.get().getBlockNrBtc()  + TIME_DIFF_BTC < blockHeight) {
                throw new USDBTCFxException("No FX aggregation found for USD-BTC.");
            }
        }
        return exchangeAggregateRate.flatMap((aggregateRate) -> aggregateRate.getExchangeAggregateCurrencyRates(CurrencyType.BTC))
                .map((aggCurrencyRate) -> aggCurrencyRate.getAggregateExchangeRate())
                .orElseThrow(() -> new USDBTCFxException("No FX aggregation found for USD-BTC."));
    }

}
