package io.modum.tokenapp.rates.service;

import io.modum.tokenapp.rates.dao.ExchangeRateRepository;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

@Service
public class ExchangeRate {

    @Autowired
    private Etherscan etherscan;

    @Autowired
    private Blockr blockr;

    @Autowired
    private ExchangeRateRepository repository;

    private final static Logger LOG = LoggerFactory.getLogger(ExchangeRate.class);

    private Exchange kraken = ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName());
    private MarketDataService marketDataService = kraken.getMarketDataService();

    public BigDecimal getBTCUSD() throws IOException {
        Ticker ticker = marketDataService.getTicker(CurrencyPair.BTC_USD);
        return ticker.getLast();
    }

    public BigDecimal getETHUSD() throws IOException {
        Ticker ticker = marketDataService.getTicker(CurrencyPair.ETH_USD);
        return ticker.getLast();
    }

    @Scheduled(fixedRate=60 * 1000)
    @Transactional
    public void fetchRates() throws IOException {
        LOG.debug("called fetchRates");

        BigDecimal rateETH = getETHUSD();
        BigDecimal rateBTC = getBTCUSD();

        long blockNrETH = etherscan.getCurrentBlockNr();
        long blockNrBTC = blockr.getCurrentBlockNr();

        io.modum.tokenapp.rates.model.ExchangeRate rate = new io.modum.tokenapp.rates.model.ExchangeRate();

        rate.setRateBtc(rateBTC);
        rate.setRateEth(rateETH);
        rate.setBlockNrBtc(blockNrBTC);
        rate.setBlockNrEth(blockNrETH);
        rate.setCreationDate(new Date());
        repository.save(rate);
    }
}
