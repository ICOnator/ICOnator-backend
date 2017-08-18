package io.modum.tokenapp.rates.service;

import io.modum.tokenapp.rates.dao.ExchangeRateRepository;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;

import org.knowm.xchange.bitfinex.v1.BitfinexExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;

import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

@Service
public class ExchangeRate {

    public static final CurrencyPair IOTA_USD = new CurrencyPair("IOT", "USD");

    @Autowired
    private Etherscan etherscan;

    @Autowired
    private Blockr blockr;

    @Autowired
    private ExchangeRateRepository repository;

    private final static Logger LOG = LoggerFactory.getLogger(ExchangeRate.class);

    private Exchange kraken = ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName());
    private MarketDataService marketDataServiceKraken = kraken.getMarketDataService();

    private Exchange bitfinex = ExchangeFactory.INSTANCE.createExchange(BitfinexExchange.class.getName());
    private MarketDataService marketDataServiceBitfinex = bitfinex.getMarketDataService();

    public BigDecimal getBTCUSD() throws IOException {
        Ticker ticker = marketDataServiceKraken.getTicker(CurrencyPair.BTC_USD);
        return ticker.getLast();
    }

    public BigDecimal getETHUSD() throws IOException {
        Ticker ticker = marketDataServiceKraken.getTicker(CurrencyPair.ETH_USD);
        return ticker.getLast();
    }

    public BigDecimal getBTCUSDBitfinex() throws IOException {
        Ticker ticker = marketDataServiceBitfinex.getTicker(CurrencyPair.BTC_USD);
        return ticker.getLast();
    }

    public BigDecimal getIOTAUSDBitfinex() throws IOException {
        Ticker ticker = marketDataServiceBitfinex.getTicker(IOTA_USD);
        return ticker.getLast();
    }

    public BigDecimal getETHUSDBitfinex() throws IOException {
        Ticker ticker = marketDataServiceBitfinex.getTicker(CurrencyPair.ETH_USD);
        return ticker.getLast();
    }

    @Transactional
    public void fetchRates() throws IOException {
        LOG.info("Fetching rates...");

        BigDecimal rateETH = null;
        try {
            rateETH = getETHUSD();
        } catch (Throwable t) {
            LOG.error("could not fetch ETH from Kraken", t);
        }

        BigDecimal rateBTC = null;
        try {
            rateBTC = getBTCUSD();
        }
        catch (Throwable t) {
            LOG.error("could not fetch BTC from Kraken", t);
        }

        BigDecimal rateETHBitfinex = null;
        try {
            rateETHBitfinex = getETHUSDBitfinex();
        } catch (Throwable t) {
            LOG.error("could not fetch ETH from Bitstamp", t);
        }

        BigDecimal rateBTCBitfinex = null;
        try {
            rateBTCBitfinex = getBTCUSDBitfinex();
        }
        catch (Throwable t) {
            LOG.error("could not fetch BTC from Bitstamp", t);
        }

        BigDecimal rateIotaBitfinex = null;
        try {
            rateIotaBitfinex = getIOTAUSDBitfinex();
        }
        catch (Throwable t) {
            LOG.error("could not fetch IOTA from Bitstamp", t);
        }

        Long blockNrETH = null;
        try {
            blockNrETH = etherscan.getCurrentBlockNr();
        }
        catch (Throwable t) {
            LOG.error("could not fetch current ETH blocknr", t);
        }

        Long blockNrBTC = null;
        try {
            blockNrBTC = blockr.getCurrentBlockNr();
        }
        catch (Throwable t) {
            LOG.error("could not fetch current BTC blocknr", t);
        }


        io.modum.tokenapp.rates.model.ExchangeRate rate = new io.modum.tokenapp.rates.model.ExchangeRate();
        rate.setRateBtc(rateBTC);
        rate.setRateEth(rateETH);
        rate.setRateBtcBitfinex(rateBTCBitfinex);
        rate.setRateEthBitfinex(rateETHBitfinex);
        rate.setRateIotaBitfinex(rateIotaBitfinex);
        rate.setBlockNrBtc(blockNrBTC);
        rate.setBlockNrEth(blockNrETH);
        rate.setCreationDate(new Date());
        repository.save(rate);
    }
}
