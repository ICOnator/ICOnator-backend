package io.iconator.rates.task;

import io.iconator.rates.config.RatesAppConfig;
import io.iconator.rates.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FetchRatesRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FetchRatesRunnable.class);

    private ExchangeRateService exchangeRateService;

    @Autowired
    public FetchRatesRunnable(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void run() {
        try {
            exchangeRateService.fetchRates();
        } catch (Exception e) {
            LOG.error("Cannot fetch rates. Details: ", e);
        }
    }

}