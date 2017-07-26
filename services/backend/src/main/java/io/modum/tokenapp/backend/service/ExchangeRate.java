package io.modum.tokenapp.backend.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class ExchangeRate {
    private Exchange kraken = ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName());
    private MarketDataService marketDataService = kraken.getMarketDataService();

    public BigDecimal getBTCUSD() throws IOException {
        Ticker ticker = marketDataService.getTicker(CurrencyPair.BTC_USD);
        return ticker.getAsk(); //TODO: ask or bid?
        //TODO:caching!
    }

    public BigDecimal getETHUSD() throws IOException {
        Ticker ticker = marketDataService.getTicker(CurrencyPair.ETH_USD);
        return ticker.getAsk(); //TODO: ask or bid?
        //TODO:caching!
    }
}
